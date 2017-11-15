/*
 * Copyright 2013-2017 consulo.io
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

package consulo.csharp.lang.psi.impl.stub.elementTypes;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilderFactory;
import com.intellij.lang.PsiParser;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.StubBuilder;
import com.intellij.psi.stubs.DefaultStubBuilder;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.tree.IStubFileElementType;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.util.indexing.IndexingDataKeys;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.CSharpLanguageVersionWrapper;
import consulo.csharp.lang.psi.impl.source.CSharpFileImpl;
import consulo.csharp.lang.psi.impl.stub.CSharpFileStub;
import consulo.dotnet.module.extension.DotNetSimpleModuleExtension;
import consulo.lang.LanguageVersion;

/**
 * @author VISTALL
 * @since 15.12.13.
 */
public class CSharpFileStubElementType extends IStubFileElementType<CSharpFileStub>
{
	public static final Key<Set<String>> PREPROCESSOR_VARIABLES = Key.create("csharp.preprocessor.variables");

	public CSharpFileStubElementType()
	{
		super("CSHARP_FILE", CSharpLanguage.INSTANCE);
	}

	@Override
	public StubBuilder getBuilder()
	{
		return new DefaultStubBuilder()
		{
			@NotNull
			@Override
			protected StubElement createStubForFile(@NotNull PsiFile file)
			{
				if(file instanceof CSharpFileImpl)
				{
					return new CSharpFileStub((CSharpFileImpl) file);
				}
				return super.createStubForFile(file);
			}
		};
	}

	@RequiredReadAction
	@Override
	protected ASTNode doParseContents(@NotNull ASTNode chameleon, @NotNull PsiElement psi)
	{
		final Project project = psi.getProject();
		final Language languageForParser = getLanguageForParser(psi);
		final LanguageVersion tempLanguageVersion = chameleon.getUserData(LanguageVersion.KEY);
		final CSharpLanguageVersionWrapper languageVersion = (CSharpLanguageVersionWrapper) (tempLanguageVersion == null ? psi.getLanguageVersion() : tempLanguageVersion);

		FileViewProvider viewProvider = ((PsiFile) psi).getViewProvider();
		VirtualFile virtualFile = viewProvider.getVirtualFile();
		if(virtualFile instanceof LightVirtualFile)
		{
			virtualFile = ((LightVirtualFile) virtualFile).getOriginalFile();
			// we need call second time, due second original file will be light
			if(virtualFile instanceof LightVirtualFile)
			{
				virtualFile = ((LightVirtualFile) virtualFile).getOriginalFile();
			}
		}
		if(virtualFile == null)
		{
			virtualFile = psi.getUserData(IndexingDataKeys.VIRTUAL_FILE);
		}

		Set<String> defVariables = Collections.emptySet();
		if(virtualFile != null)
		{
			List<String> variables = findVariables(virtualFile, project);

			if(variables != null)
			{
				defVariables = new HashSet<>(variables);
			}
		}

		final PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, null, languageForParser, languageVersion, chameleon.getChars());
		builder.putUserData(PREPROCESSOR_VARIABLES, defVariables);

		final PsiParser parser = LanguageParserDefinitions.INSTANCE.forLanguage(languageForParser).createParser(languageVersion);
		return parser.parse(this, builder, languageVersion).getFirstChildNode();
	}

	@Nullable
	@RequiredReadAction
	private static List<String> findVariables(@NotNull VirtualFile virtualFile, @NotNull Project project)
	{
		Module module = ModuleUtilCore.findModuleForFile(virtualFile, project);
		if(module == null)
		{
			return null;
		}
		DotNetSimpleModuleExtension<?> extension = ModuleUtilCore.getExtension(module, DotNetSimpleModuleExtension.class);
		if(extension != null)
		{
			return extension.getVariables();
		}
		return null;
	}

	@NotNull
	@Override
	public CSharpFileStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException
	{
		return new CSharpFileStub(null);
	}

	@Override
	public int getStubVersion()
	{
		return 89;
	}

	@NotNull
	@Override
	public String getExternalId()
	{
		return "csharp.file";
	}
}
