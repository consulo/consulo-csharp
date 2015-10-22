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

import gnu.trove.THashSet;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import org.mustbe.consulo.csharp.lang.CSharpLanguageVersionWrapper;
import org.mustbe.consulo.csharp.lang.psi.CSharpPreprocessorDefineDirective;
import org.mustbe.consulo.csharp.lang.psi.CSharpPreprocessorFileType;
import org.mustbe.consulo.csharp.lang.psi.CSharpPreprocessorRecursiveElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpPreprocessorTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpFileImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpPreprocessorConditionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpPreprocessorExpression;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpPreprocessorIfElseBlockImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpPreprocessorOpenTagImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpPreprocessorUndefDirectiveImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpFileStub;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.elementTypes.macro.MacroEvaluator;
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
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.StubBuilder;
import com.intellij.psi.stubs.DefaultStubBuilder;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IStubFileElementType;
import com.intellij.psi.util.PsiUtilCore;
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
					textRanges = collectDisabledBlocks(project, chars, extension);
				}
			}
		}

		Lexer lexer = languageVersion.createLexer(textRanges);
		final PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, lexer, languageForParser, languageVersion, chars);
		final PsiParser parser = LanguageParserDefinitions.INSTANCE.forLanguage(languageForParser).createParser(project, languageVersion);
		return parser.parse(this, builder, languageVersion).getFirstChildNode();
	}

	@NotNull
	public static List<TextRange> collectDisabledBlocks(@NotNull Project project, @NotNull CharSequence text, @NotNull DotNetSimpleModuleExtension<?> extension)
	{
		return collectDisabledBlocks(project, text, extension.getVariables());
	}

	@NotNull
	private static List<TextRange> collectDisabledBlocks(@NotNull Project project, @NotNull final CharSequence text, @NotNull final List<String> baseVariables)
	{
		PsiFile templateFile = PsiFileFactory.getInstance(project).createFileFromText("temp", CSharpPreprocessorFileType.INSTANCE, text);

		final Ref<List<TextRange>> listRef = Ref.create();
		final Ref<Set<String>> redefined = Ref.create();
		templateFile.accept(new CSharpPreprocessorRecursiveElementVisitor()
		{
			@Override
			@RequiredReadAction
			public void visitDefineDirective(CSharpPreprocessorDefineDirective define)
			{
				String name = define.getName();
				if(name != null)
				{
					Set<String> redefs = redefined.get();
					if(redefs == null)
					{
						redefined.set(redefs = new THashSet<String>(baseVariables));
					}

					redefs.add(name);
				}
			}

			@Override
			@RequiredReadAction
			public void visitUndefDirective(CSharpPreprocessorUndefDirectiveImpl undefDirective)
			{
				String name = undefDirective.getVariable();
				if(name != null)
				{
					Set<String> redefs = redefined.get();
					if(redefs == null)
					{
						redefined.set(redefs = new THashSet<String>(baseVariables));
					}

					redefs.remove(name);
				}
			}

			@Override
			@RequiredReadAction
			public void visitConditionBlock(CSharpPreprocessorConditionImpl element)
			{
				List<TextRange> textRanges = listRef.get();
				if(textRanges == null)
				{
					listRef.set(textRanges = new ArrayList<TextRange>());
				}

				CSharpPreprocessorIfElseBlockImpl[] conditionBlocks = element.getIfElseBlocks();
				Queue<CSharpPreprocessorIfElseBlockImpl> queue = new ArrayDeque<CSharpPreprocessorIfElseBlockImpl>(conditionBlocks.length);
				Collections.addAll(queue, conditionBlocks);

				CSharpPreprocessorIfElseBlockImpl activeBlock = null;

				boolean forceDisable = false;
				CSharpPreprocessorIfElseBlockImpl block;
				while((block = queue.poll()) != null)
				{
					CSharpPreprocessorOpenTagImpl declarationTag = block.getDeclarationTag();
					if(forceDisable)
					{
						addTextRange(declarationTag, block, textRanges);
						continue;
					}

					IElementType elementType = PsiUtilCore.getElementType(declarationTag.getKeywordElement());
					assert  elementType != null;
					if(elementType != CSharpPreprocessorTokens.ELSE_KEYWORD) // if / elif
					{
						CSharpPreprocessorExpression value = declarationTag.getValue();
						if(value == null) //if not expression - disabled
						{
							addTextRange(declarationTag, block, textRanges);
						}
						else
						{
							String text = value.getText();
							if(evaluateExpression(text))
							{
								forceDisable = true;
								activeBlock = block;
							}
							else
							{
								addTextRange(declarationTag, block, textRanges);
							}
						}
					}
					else
					{
						activeBlock = block;
					}
				}

				if(activeBlock != null)
				{
					activeBlock.accept(this);
				}
			}

			@RequiredReadAction
			private void addTextRange(CSharpPreprocessorOpenTagImpl start, CSharpPreprocessorIfElseBlockImpl block, List<TextRange> textRanges)
			{
				textRanges.add(new TextRange(start.getTextRange().getEndOffset(), block.getTextRange().getEndOffset()));
			}

			private boolean evaluateExpression(String text)
			{
				Collection<String> defs = redefined.get();
				if(defs == null)
				{
					defs = baseVariables;
				}

				return MacroEvaluator.evaluate(text, defs);
			}
		});
		List<TextRange> list = listRef.get();
		return list == null ? Collections.<TextRange>emptyList() : list;
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
