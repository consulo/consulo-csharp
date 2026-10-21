/*
 * Copyright 2013-2026 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package consulo.csharp.it.resolve;

import consulo.application.ReadAction;
import consulo.application.WriteAction;
import consulo.content.base.BinariesOrderRootType;
import consulo.content.library.Library;
import consulo.it.HeadlessModules;
import consulo.it.HeadlessProjects;
import consulo.language.psi.*;
import consulo.module.Module;
import consulo.module.content.ModuleRootManager;
import consulo.module.content.layer.ModifiableRootModel;
import consulo.module.extension.MutableModuleExtension;
import consulo.project.Project;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.archive.ArchiveVfsUtil;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Port of the old {@code ResolvingTestCase}: every reference of {@code <name>.cs} is resolved and printed as
 * {@code text(start,end): results}, which must match {@code <name>.cs.txt} next to it.
 * <p>
 * The module is a mono .NET module with the libraries of a {@link MockSdk}, plus {@code <name>.dll} when the test data has one.
 *
 * @author VISTALL
 */
public final class CSharpResolveTesting
{
	private CSharpResolveTesting()
	{
	}

	public static void doTest(HeadlessProjects projects, String dataDirectory, String testName) throws Exception
	{
		Path dataPath = resource("resolve/" + dataDirectory);
		Path expected = dataPath.resolve(testName + ".cs.txt");

		PsiFile file = openFile(projects, MockSdk.MONO_4_5, dataPath, testName);

		String actual = ReadAction.compute(() -> buildResolveText(file));
		assertThat(actual).isEqualTo(loadExpected(expected));
	}

	/**
	 * Every operator expression of {@code <name>.cs} printed with its type and the operator it resolves to, which must
	 * match {@code <name>.cs.types.txt} - the same file for every SDK, since the C# semantics do not depend on how the
	 * base class library declares its operators.
	 */
	public static void doOperatorTypesTest(HeadlessProjects projects, MockSdk sdk, String dataDirectory, String testName) throws Exception
	{
		Path dataPath = resource("resolve/" + dataDirectory);
		Path expected = dataPath.resolve(testName + ".cs.types.txt");

		PsiFile file = openFile(projects, sdk, dataPath, testName);

		String actual = ReadAction.compute(() -> OperatorTypesPrinter.print(file, false));
		String detailed = ReadAction.compute(() -> OperatorTypesPrinter.print(file, true));
		assertThat(actual).as("%s, with operator owners:%n%s%n", sdk, detailed).isEqualTo(loadExpected(expected));
	}

	private static PsiFile openFile(HeadlessProjects projects, MockSdk sdk, Path dataPath, String testName) throws Exception
	{
		Path source = dataPath.resolve(testName + ".cs");
		assertThat(source).exists();

		List<Path> testLibraries = new ArrayList<>();
		Path testLibrary = dataPath.resolve(testName + ".dll");
		if(Files.exists(testLibrary))
		{
			testLibraries.add(testLibrary);
		}
		return openSource(projects, sdk, source.getFileName().toString(), Files.readString(source, StandardCharsets.UTF_8), testLibraries);
	}

	/**
	 * A project with one mono .NET module over {@code text} written as {@code src/<fileName>}, the libraries of
	 * {@code sdk} and {@code testLibraries}, indexed and ready to resolve in.
	 */
	public static PsiFile openSource(HeadlessProjects projects, MockSdk sdk, String fileName, String text, List<Path> testLibraries) throws Exception
	{
		Path directory = projects.newDirectory();
		Path srcDirectory = Files.createDirectories(directory.resolve("src"));
		Files.writeString(srcDirectory.resolve(fileName), text, StandardCharsets.UTF_8);

		Project project = projects.open(directory);

		VirtualFile root = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(directory);
		assertThat(root).isNotNull();
		VirtualFile srcRoot = root.findChild("src");
		assertThat(srcRoot).isNotNull();

		Module module = HeadlessModules.createModule(project, "main", root);
		HeadlessModules.addSourceRoot(module, srcRoot);

		List<Path> libraries = new ArrayList<>(sdk.getLibraries());
		libraries.addAll(testLibraries);
		setupModule(module, libraries);

		HeadlessProjects.awaitIdle(project);

		VirtualFile file = srcRoot.findChild(fileName);
		assertThat(file).isNotNull();

		PsiFile psiFile = ReadAction.compute(() -> PsiManager.getInstance(project).findFile(file));
		assertThat(psiFile).isNotNull();
		return psiFile;
	}

	private static String loadExpected(Path expected) throws Exception
	{
		return Files.readString(expected, StandardCharsets.UTF_8).replace("\r\n", "\n").trim();
	}

	private static void setupModule(Module module, List<Path> libraries)
	{
		WriteAction.run(() ->
		{
			ModifiableRootModel rootModel = ModuleRootManager.getInstance(module).getModifiableModel();

			MutableModuleExtension<?> dotnet = rootModel.getExtensionWithoutCheck("mono-dotnet");
			assertThat(dotnet).as("mono-dotnet extension").isNotNull();
			dotnet.setEnabled(true);

			for(Path path : libraries)
			{
				VirtualFile localFile = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(path);
				assertThat(localFile).as(path.toString()).isNotNull();
				// a zip of decompiled msil files, or a dll - which consulo.dotnet opens as an archive too
				VirtualFile archiveRoot = ArchiveVfsUtil.getArchiveRootForLocalFile(localFile);
				assertThat(archiveRoot).as("archive root of " + path).isNotNull();

				Library library = rootModel.getModuleLibraryTable().createLibrary(path.getFileName().toString());
				Library.ModifiableModel libraryModel = library.getModifiableModel();
				libraryModel.addRoot(archiveRoot, BinariesOrderRootType.ID);
				libraryModel.commit();
			}

			MutableModuleExtension<?> csharp = rootModel.getExtensionWithoutCheck("mono-csharp");
			assertThat(csharp).as("mono-csharp extension").isNotNull();
			csharp.setEnabled(true);

			rootModel.commit();
		});
	}

	private static String buildResolveText(PsiFile psiFile)
	{
		Map<PsiReference, List<ResolveResult>> refs = new LinkedHashMap<>();
		psiFile.accept(new PsiRecursiveElementVisitor()
		{
			@Override
			public void visitElement(PsiElement element)
			{
				super.visitElement(element);
				for(PsiReference reference : element.getReferences())
				{
					ResolveResult[] results;
					if(reference instanceof PsiPolyVariantReference polyVariantReference)
					{
						// incomplete code - all candidates, invalid ones included, which is what the expected data records
						results = polyVariantReference.multiResolve(true);
					}
					else
					{
						PsiElement resolved = reference.resolve();
						results = resolved == null ? ResolveResult.EMPTY_ARRAY : new ResolveResult[]{new PsiElementResolveResult(resolved)};
					}
					refs.computeIfAbsent(reference, it -> new ArrayList<>()).addAll(List.of(results));
				}
			}
		});

		StringBuilder builder = new StringBuilder();
		for(Map.Entry<PsiReference, List<ResolveResult>> entry : refs.entrySet())
		{
			PsiElement element = entry.getKey().getElement();
			List<ResolveResult> results = entry.getValue();

			builder.append(element.getText()).append(element.getTextRange()).append(": ");
			if(results.isEmpty())
			{
				builder.append("empty");
			}
			else
			{
				builder.append(results.stream().map(CSharpResolveTesting::buildResultText).collect(Collectors.joining(", ")));
			}
			builder.append("\n");
		}
		return builder.toString().trim();
	}

	private static String buildResultText(ResolveResult result)
	{
		return result.getElement() == null ? "null-element" : ResultElementBuilder.build(result);
	}

	static Path resource(String path) throws URISyntaxException
	{
		URL url = CSharpResolveTesting.class.getClassLoader().getResource(path);
		assertThat(url).as(path).isNotNull();
		return Path.of(url.toURI());
	}
}
