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

import com.intellij.lang.*;
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
import com.intellij.psi.tree.ILazyParseableElementType;
import com.intellij.psi.tree.IStubFileElementType;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.util.indexing.IndexingDataKeys;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.CSharpLanguageVersionWrapper;
import consulo.csharp.lang.psi.impl.source.CSharpFileImpl;
import consulo.csharp.lang.psi.impl.stub.CSharpFileStub;
import consulo.dotnet.module.extension.DotNetSimpleModuleExtension;
import consulo.lang.LanguageVersion;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

		final PsiParser parser = LanguageParserDefinitions.INSTANCE.forLanguage(languageForParser).createParser(languageVersion);
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

		Set<String> defVariables = Collections.emptySet();
		if(virtualFile != null)
		{
			List<String> variables = findVariables(virtualFile, psi.getProject());

			if(variables != null)
			{
				defVariables = new HashSet<>(variables);
			}
		}
		return defVariables;
	}

	@Nullable
	@RequiredReadAction
	private static List<String> findVariables(@Nonnull VirtualFile virtualFile, @Nonnull Project project)
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

	@Nonnull
	@Override
	public CSharpFileStub deserialize(@Nonnull StubInputStream dataStream, StubElement parentStub) throws IOException
	{
		return new CSharpFileStub(null);
	}

	@Override
	public int getStubVersion()
	{
		return 103;
	}

	@Nonnull
	@Override
	public String getExternalId()
	{
		return "csharp.file";
	}
}
