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

package consulo.csharp.lang.impl.psi.stub.elementTypes;

import consulo.language.ast.ILazyParseableElementType;
import consulo.language.parser.ParserDefinition;
import consulo.language.parser.PsiBuilder;
import consulo.language.parser.PsiBuilderFactory;
import consulo.language.psi.PsiElement;
import consulo.language.psi.stub.*;
import consulo.language.version.LanguageVersion;
import consulo.module.Module;
import consulo.language.util.ModuleUtilCore;
import consulo.module.content.ProjectFileIndex;
import consulo.module.content.layer.orderEntry.OrderEntry;
import consulo.language.file.FileViewProvider;
import consulo.language.psi.PsiFile;
import consulo.language.file.light.LightVirtualFile;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.impl.CSharpLanguageVersionWrapper;
import consulo.csharp.lang.impl.psi.source.CSharpFileImpl;
import consulo.csharp.lang.impl.psi.stub.CSharpFileStub;
import consulo.dotnet.module.extension.DotNetSimpleModuleExtension;
import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.parser.PsiParser;
import consulo.project.Project;
import consulo.util.dataholder.Key;
import consulo.virtualFileSystem.VirtualFile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;

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
			@Nonnull
			@Override
			protected StubElement createStubForFile(@Nonnull PsiFile file)
			{
				if(file instanceof CSharpFileImpl)
				{
					return new CSharpFileStub((CSharpFileImpl) file);
				}
				return super.createStubForFile(file);
			}

			@Override
			public boolean skipChildProcessingWhenBuildingStubs(@Nonnull ASTNode parent, @Nonnull ASTNode node)
			{
				// skip any lazy parseable elements, like preprocessors or code blocks etc
				if(node.getElementType() instanceof ILazyParseableElementType)
				{
					return true;
				}
				return false;
			}
		};
	}

	@RequiredReadAction
	@Override
	protected ASTNode doParseContents(@Nonnull ASTNode chameleon, @Nonnull PsiElement psi)
	{
		final Project project = psi.getProject();
		final Language languageForParser = getLanguageForParser(psi);
		final LanguageVersion tempLanguageVersion = chameleon.getUserData(LanguageVersion.KEY);
		final CSharpLanguageVersionWrapper languageVersion = (CSharpLanguageVersionWrapper) (tempLanguageVersion == null ? psi.getLanguageVersion() : tempLanguageVersion);

		Set<String> defVariables = getStableDefines((PsiFile) psi);

		final PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, null, languageForParser, languageVersion, chameleon.getChars());
		builder.putUserData(PREPROCESSOR_VARIABLES, defVariables);

		final PsiParser parser = ParserDefinition.forLanguage(languageForParser).createParser(languageVersion);
		return parser.parse(this, builder, languageVersion).getFirstChildNode();
	}

	@Nonnull
	@RequiredReadAction
	public static Set<String> getStableDefines(@Nonnull PsiFile psi)
	{
		FileViewProvider viewProvider = psi.getViewProvider();
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

		Set<String> defVariables = Set.of();
		if(virtualFile != null)
		{
			Collection<String> variables = findVariables(virtualFile, psi.getProject());

			if(variables != null)
			{
				defVariables = new HashSet<>(variables);
			}
		}
		return defVariables;
	}

	@Nullable
	@RequiredReadAction
	private static Collection<String> findVariables(@Nonnull VirtualFile virtualFile, @Nonnull Project project)
	{
		Module module = ModuleUtilCore.findModuleForFile(virtualFile, project);
		if(module == null)
		{
			List<OrderEntry> orderEntriesForFile = ProjectFileIndex.getInstance(project).getOrderEntriesForFile(virtualFile);

			Set<String> variables = new HashSet<>();

			for(OrderEntry orderEntry : orderEntriesForFile)
			{
				Module ownerModule = orderEntry.getOwnerModule();

				DotNetSimpleModuleExtension<?> extension = ModuleUtilCore.getExtension(ownerModule, DotNetSimpleModuleExtension.class);
				if(extension != null)
				{
					variables.addAll(extension.getVariables());
				}
			}
			return variables;
		}
		else
		{
			DotNetSimpleModuleExtension<?> extension = ModuleUtilCore.getExtension(module, DotNetSimpleModuleExtension.class);
			if(extension != null)
			{
				return extension.getVariables();
			}
		}
		return null;
	}

	@Nonnull
	@Override
	public CSharpFileStub deserialize(@Nonnull StubInputStream dataStream, StubElement parentStub) throws IOException
	{
		return new CSharpFileStub(null);
	}

	@Override
	public int getStubVersion()
	{
		return 121;
	}

	@Nonnull
	@Override
	public String getExternalId()
	{
		return "csharp.file";
	}
}
