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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import org.mustbe.consulo.csharp.lang.CSharpLanguageVersionWrapper;
import org.mustbe.consulo.csharp.lang.CSharpMacroLanguage;
import org.mustbe.consulo.csharp.lang.psi.CSharpMacroDefine;
import org.mustbe.consulo.csharp.lang.psi.CSharpMacroRecursiveElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpFileImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpMacroBlockStartImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpMacroBlockStopImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpMacroExpression;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpMacroIfConditionBlockImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpMacroIfImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpFileStub;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.elementTypes.macro.MacroEvaluator;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.module.MainConfigurationLayer;
import org.mustbe.consulo.dotnet.module.extension.DotNetModuleExtension;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.lang.LanguageVersion;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilderFactory;
import com.intellij.lang.PsiParser;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.StubBuilder;
import com.intellij.psi.impl.source.CharTableImpl;
import com.intellij.psi.stubs.DefaultStubBuilder;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.tree.IStubFileElementType;

/**
 * @author VISTALL
 * @since 15.12.13.
 */
public class CSharpFileStubElementType extends IStubFileElementType<CSharpFileStub>
{
	static
	{
		CharTableImpl.addStringsFromClassToStatics(DotNetTypes.class);
	}

	public CSharpFileStubElementType()
	{
		super("CSHARP_FILE", CSharpLanguage.INSTANCE);
	}

	@Override
	public StubBuilder getBuilder()
	{
		return new DefaultStubBuilder()
		{
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
	protected ASTNode doParseContents(@NotNull ASTNode chameleon, @NotNull PsiElement psi)
	{
		final Project project = psi.getProject();
		final Language languageForParser = getLanguageForParser(psi);
		final LanguageVersion tempLanguageVersion = chameleon.getUserData(LanguageVersion.KEY);
		final CSharpLanguageVersionWrapper languageVersion = (CSharpLanguageVersionWrapper) (tempLanguageVersion == null ? psi.getLanguageVersion() :
				tempLanguageVersion);

		FileViewProvider viewProvider = ((PsiFile) psi).getViewProvider();
		PsiFile macroFile = viewProvider.getPsi(CSharpMacroLanguage.INSTANCE);
		List<TextRange> textRanges = Collections.emptyList();
		if(macroFile != null)
		{
			DotNetModuleExtension<?> extension = ModuleUtilCore.getExtension(((PsiFile) psi).getOriginalFile(), DotNetModuleExtension.class);
			if(extension != null)
			{
				textRanges = collectDisabledBlocks(macroFile, extension);
			}
		}

		final PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, languageVersion.createLexer(textRanges),
				languageForParser,
				languageVersion, chameleon.getChars());
		final PsiParser parser = LanguageParserDefinitions.INSTANCE.forLanguage(languageForParser).createParser(project, languageVersion);
		return parser.parse(this, builder, languageVersion).getFirstChildNode();
	}

	@NotNull
	public static List<TextRange> collectDisabledBlocks(PsiFile macroFile, DotNetModuleExtension extension)
	{
		MainConfigurationLayer currentProfileEx = (MainConfigurationLayer) extension.getCurrentLayer();
		return collectDisabledBlocks(macroFile, currentProfileEx.getVariables());
	}

	private static List<TextRange> collectDisabledBlocks(PsiFile templateFile, final List<String> baseVariables)
	{
		final Ref<List<TextRange>> listRef = Ref.create();
		final Ref<List<String>> redefined = Ref.create();
		templateFile.accept(new CSharpMacroRecursiveElementVisitor()
		{
			@Override
			public void visitMacroDefine(CSharpMacroDefine def)
			{
				List<String> redefs = redefined.get();
				if(redefs == null)
				{
					redefined.set(redefs = new ArrayList<String>(baseVariables));
				}
				String name = def.getName();
				if(name != null)
				{
					if(def.isUnDef())
					{
						redefs.remove(name) ;
					}
					else
					{
						redefs.add(name);
					}
				}
			}

			@Override
			public void visitMacroIf(CSharpMacroIfImpl element)
			{
				List<TextRange> textRanges = listRef.get();
				if(textRanges == null)
				{
					listRef.set(textRanges = new ArrayList<TextRange>());
				}

				CSharpMacroIfConditionBlockImpl[] conditionBlocks = element.getConditionBlocks();
				Queue<CSharpMacroIfConditionBlockImpl> queue = new ArrayDeque<CSharpMacroIfConditionBlockImpl>(conditionBlocks.length);
				Collections.addAll(queue, conditionBlocks);

				CSharpMacroIfConditionBlockImpl activeBlock = null;

				boolean forceDisable = false;
				CSharpMacroIfConditionBlockImpl block;
				while((block = queue.poll()) != null)
				{
					CSharpMacroBlockStartImpl declarationTag = block.getDeclarationTag();
					if(forceDisable)
					{
						addTextRange(declarationTag, queue, element, textRanges);
						continue;
					}

					if(!declarationTag.isElse()) // if / elif
					{
						CSharpMacroExpression value = declarationTag.getValue();
						if(value == null) //if not expression - disabled
						{
							addTextRange(declarationTag, queue, element, textRanges);
						}
						else
						{
							String text = value.getText();
							if(isDefined(text))
							{
								forceDisable = true;
								activeBlock = block;
							}
							else
							{
								addTextRange(declarationTag, queue, element, textRanges);
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

			private void addTextRange(CSharpMacroBlockStartImpl start, Queue<CSharpMacroIfConditionBlockImpl> queue, CSharpMacroIfImpl macroIf,
					List<TextRange> textRanges)
			{
				// find next element
				CSharpMacroIfConditionBlockImpl element = queue.peek();

				int endOffset;
				if(element == null)
				{
					CSharpMacroBlockStopImpl closeTag = macroIf.getCloseTag();
					if(closeTag == null)
					{
						endOffset = macroIf.getContainingFile().getTextLength();
					}
					else
					{
						endOffset = closeTag.getKeywordElement().getTextRange().getStartOffset();
					}
				}
				else
				{
					endOffset = element.getDeclarationTag().getKeywordElement().getTextRange().getStartOffset();
				}

				PsiElement stopElement = start.getStopElement();
				if(stopElement == null)
				{
					textRanges.add(new TextRange(start.getTextRange().getEndOffset(), endOffset));
				}
				else
				{
					textRanges.add(new TextRange(stopElement.getTextRange().getEndOffset(), endOffset));
				}
			}

			private boolean isDefined(String text)
			{
				List<String> defs = redefined.get();
				if(defs == null)
				{
					defs = baseVariables;
				}

				return !defs.isEmpty() && MacroEvaluator.evaluate(text, defs);
			}
		}); List<TextRange> list = listRef.get();
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
		return 27;
	}

	@NotNull
	@Override
	public String getExternalId()
	{
		return "csharp.file";
	}
}
