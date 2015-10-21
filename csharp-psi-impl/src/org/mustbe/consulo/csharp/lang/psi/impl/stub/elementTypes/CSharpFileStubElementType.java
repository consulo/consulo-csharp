/*
 * Copyright 2013-2014 must-be.org
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

package org.mustbe.consulo.csharp.lang.psi.impl.stub.elementTypes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import org.mustbe.consulo.csharp.lang.CSharpLanguageVersionWrapper;
import org.mustbe.consulo.csharp.lang.lexer.CSharpPreprocessorLexer;
import org.mustbe.consulo.csharp.lang.psi.CSharpPreprocessorTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpFileImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpFileStub;
import org.mustbe.consulo.dotnet.module.extension.DotNetSimpleModuleExtension;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.lang.LanguageVersion;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilderFactory;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.StubBuilder;
import com.intellij.psi.stubs.DefaultStubBuilder;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IStubFileElementType;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.util.indexing.IndexingDataKeys;

/**
 * @author VISTALL
 * @since 15.12.13.
 */
public class CSharpFileStubElementType extends IStubFileElementType<CSharpFileStub>
{
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

	@Override
	@RequiredReadAction
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
		}

		if(virtualFile == null)
		{
			virtualFile = psi.getUserData(IndexingDataKeys.VIRTUAL_FILE);
		}

		List<TextRange> textRanges = Collections.emptyList();
		CharSequence chars = chameleon.getChars();
		if(virtualFile != null)
		{
			Module moduleForFile = ModuleUtilCore.findModuleForFile(virtualFile, project);
			if(moduleForFile != null)
			{
				DotNetSimpleModuleExtension extension = ModuleUtilCore.getExtension(moduleForFile, DotNetSimpleModuleExtension.class);
				if(extension != null)
				{
					textRanges = collectDisabledBlocks(chars, extension);
				}
			}
		}

		Lexer lexer = languageVersion.createLexer(textRanges);
		final PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, lexer, languageForParser, languageVersion, chars);
		final PsiParser parser = LanguageParserDefinitions.INSTANCE.forLanguage(languageForParser).createParser(project, languageVersion);
		return parser.parse(this, builder, languageVersion).getFirstChildNode();
	}

	@NotNull
	public static List<TextRange> collectDisabledBlocks(@NotNull CharSequence text, @NotNull DotNetSimpleModuleExtension<?> extension)
	{
		return collectDisabledBlocks(text, extension.getVariables());
	}

	static enum State
	{
		unknown,
		define,
		undef
	}

	@NotNull
	private static List<TextRange> collectDisabledBlocks(@NotNull CharSequence text, @NotNull final List<String> baseVariables)
	{
		CSharpPreprocessorLexer lexer = new CSharpPreprocessorLexer();
		lexer.start(text);

		List<TextRange> textRanges = null;
		List<String> variables = null;

		State state = State.unknown;
		IElementType elementType = null;
		while((elementType = lexer.getTokenType()) != null)
		{
			loop:
			{
				if(elementType == CSharpPreprocessorTokens.WHITE_SPACE || elementType == CSharpPreprocessorTokens.COMMENT)
				{
					break loop;
				}

				switch(state)
				{
					case unknown:
						if(elementType == CSharpPreprocessorTokens.DEFINE_KEYWORD)
						{
							if(variables == null)
							{
								variables = new ArrayList<String>(baseVariables);
							}
							state = State.define;
						}
						else if(elementType == CSharpPreprocessorTokens.UNDEF_KEYWORD)
						{
							if(variables == null)
							{
								variables = new ArrayList<String>(baseVariables);
							}
							state = State.undef;
						}
						else if(elementType == CSharpPreprocessorTokens.CSHARP_FRAGMENT)
						{
							state = State.unknown;
						}
						break;
					case define:
						if(elementType == CSharpPreprocessorTokens.SIMPLE_VALUE)
						{
							String tokenText = lexer.getTokenText();
							variables.add(tokenText);

							state = State.unknown;
						}
						break;
					case undef:
						if(elementType == CSharpPreprocessorTokens.IDENTIFIER)
						{
							String tokenText = lexer.getTokenText();
							variables.remove(tokenText);

							state = State.unknown;
						}
						break;
				}
			}

			lexer.advance();
		}
		return textRanges == null ? Collections.<TextRange>emptyList() : textRanges;
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
		return 70;
	}

	@NotNull
	@Override
	public String getExternalId()
	{
		return "csharp.file";
	}
}
