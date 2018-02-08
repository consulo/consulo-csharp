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

package consulo.csharp.lang;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import com.intellij.codeInsight.folding.CodeFoldingSettings;
import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.CustomFoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.containers.ContainerUtil;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.doc.psi.CSharpDocRoot;
import consulo.csharp.lang.parser.preprocessor.EndRegionPreprocessorDirective;
import consulo.csharp.lang.parser.preprocessor.PreprocessorDirective;
import consulo.csharp.lang.parser.preprocessor.PreprocessorLightParser;
import consulo.csharp.lang.parser.preprocessor.RegionPreprocessorDirective;
import consulo.csharp.lang.psi.*;
import consulo.csharp.lang.psi.impl.source.CSharpBlockStatementImpl;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;

/**
 * @author VISTALL
 * @since 30.11.13.
 */
public class CSharpFoldingBuilder extends CustomFoldingBuilder
{
	@RequiredReadAction
	@Override
	protected void buildLanguageFoldRegions(@NotNull final List<FoldingDescriptor> descriptors, @NotNull PsiElement root, @NotNull Document document, boolean quick)
	{
		final Deque<PsiElement> regions = new ArrayDeque<>();
		final Set<CSharpUsingListChild> processedUsingStatements = new LinkedHashSet<>();

		root.accept(new CSharpRecursiveElementVisitor()
		{
			@Override
			@RequiredReadAction
			public void visitElement(PsiElement element)
			{
				super.visitElement(element);
				IElementType elementType = PsiUtilCore.getElementType(element);
				if(elementType == CSharpPreprocessorElements.PREPROCESSOR_DIRECTIVE)
				{
					PreprocessorDirective directive = PreprocessorLightParser.parse(element.getText());
					if(directive instanceof RegionPreprocessorDirective)
					{
						regions.addLast(element);
					}
					else if(directive instanceof EndRegionPreprocessorDirective)
					{
						PsiElement lastRegion = regions.pollLast();
						if(lastRegion == null)
						{
							return;
						}

						int startOffset = lastRegion.getTextRange().getStartOffset();
						String text = lastRegion.getText();
						for(int i = 0; i < text.length(); i++)
						{
							if(Character.isWhitespace(text.charAt(i)))
							{
								startOffset++;
							}
							else
							{
								break;
							}
						}

						descriptors.add(new FoldingDescriptor(lastRegion, new TextRange(startOffset, element.getTextRange().getEndOffset())));
					}
				}
				else if(element instanceof CSharpDocRoot)
				{
					// view CSharpDocFoldingBuilder for placeholder and expand configurable
					descriptors.add(new FoldingDescriptor(element, element.getTextRange()));
				}
			}

			@Override
			@RequiredReadAction
			public void visitPropertyDeclaration(CSharpPropertyDeclaration declaration)
			{
				super.visitPropertyDeclaration(declaration);

				addBodyWithBraces(descriptors, declaration);
			}

			@Override
			@RequiredReadAction
			public void visitEventDeclaration(CSharpEventDeclaration declaration)
			{
				super.visitEventDeclaration(declaration);

				addBodyWithBraces(descriptors, declaration);
			}

			@Override
			@RequiredReadAction
			public void visitUsingChild(@NotNull CSharpUsingListChild child)
			{
				super.visitUsingChild(child);

				if(processedUsingStatements.contains(child))
				{
					return;
				}

				PsiElement referenceElement = child.getReferenceElement();
				if(referenceElement == null)
				{
					return;
				}

				List<CSharpUsingListChild> children = new ArrayList<>(5);

				for(ASTNode node = child.getNode(); node != null; node = node.getTreeNext())
				{
					IElementType elementType = node.getElementType();
					if(elementType == TokenType.WHITE_SPACE)
					{
						CharSequence chars = node.getChars();
						if(StringUtil.countNewLines(chars) > 2)
						{
							break;
						}
					}
					else if(elementType == CSharpPreprocessorElements.PREPROCESSOR_DIRECTIVE)
					{
						break;
					}
					else if(CSharpStubElements.USING_CHILDREN.contains(elementType))
					{
						children.add(node.getPsi(CSharpUsingListChild.class));
					}
				}

				if(children.size() <= 1)
				{
					return;
				}

				PsiElement usingKeyword = child.getUsingKeywordElement();
				int startOffset = usingKeyword.getTextRange().getEndOffset() + 1;
				int endOffset = ContainerUtil.getLastItem(children).getLastChild().getTextRange().getEndOffset();

				processedUsingStatements.addAll(children);
				descriptors.add(new FoldingDescriptor(child, new TextRange(startOffset, endOffset)));
			}

			@Override
			@RequiredReadAction
			public void visitBlockStatement(CSharpBlockStatementImpl statement)
			{
				super.visitBlockStatement(statement);

				if(!(statement.getParent() instanceof DotNetLikeMethodDeclaration))
				{
					return;
				}
				addBodyWithBraces(descriptors, statement);
			}

			@Override
			@RequiredReadAction
			public void visitComment(PsiComment comment)
			{
				PsiFile containingFile = comment.getContainingFile();

				if(containingFile == null)
				{
					return;
				}

				if(containingFile.getFirstChild() == comment)
				{
					TextRange textRange = comment.getTextRange();
					int startOffset = textRange.getStartOffset();

					PsiElement lastComment = findLastComment(comment, comment.getTokenType());

					descriptors.add(new FoldingDescriptor(comment, new TextRange(startOffset, lastComment.getTextRange().getEndOffset())));
				}
			}
		});
	}

	@Override
	@RequiredReadAction
	protected String getLanguagePlaceholderText(@NotNull ASTNode node, @NotNull TextRange range)
	{
		PsiElement psi = node.getPsi();
		if(psi instanceof CSharpUsingListChild)
		{
			return "...";
		}
		else if(psi instanceof CSharpBlockStatementImpl || psi instanceof CSharpPropertyDeclaration || psi instanceof CSharpTypeDeclaration || psi instanceof CSharpEventDeclaration)
		{
			return "{...}";
		}
		else if(psi instanceof PsiComment)
		{
			IElementType tokenType = ((PsiComment) psi).getTokenType();
			if(tokenType == CSharpTokens.LINE_COMMENT)
			{
				return "// ...";
			}
			else if(tokenType == CSharpTokens.BLOCK_COMMENT)
			{
				return "/** ... */";
			}
		}

		IElementType elementType = PsiUtilCore.getElementType(psi);
		if(elementType == CSharpPreprocessorElements.PREPROCESSOR_DIRECTIVE)
		{
			return psi.getText().trim();
		}
		return null;
	}

	@RequiredReadAction
	private static PsiElement findLastComment(PsiElement element, IElementType elementType)
	{
		PsiElement target = element;
		PsiElement nextSibling = element.getNextSibling();
		while(nextSibling != null)
		{
			if(isAcceptableComment(nextSibling, elementType))
			{
				if(nextSibling instanceof PsiWhiteSpace)
				{
					target = nextSibling.getPrevSibling();
				}
				else
				{
					target = nextSibling;
				}
				nextSibling = nextSibling.getNextSibling();
			}
			else
			{
				return target;
			}
		}
		return element;
	}

	private static boolean isAcceptableComment(PsiElement nextSibling, IElementType elementType)
	{
		if(nextSibling == null)
		{
			return false;
		}
		return nextSibling instanceof PsiWhiteSpace || (nextSibling instanceof PsiComment && ((PsiComment) nextSibling).getTokenType() == elementType);
	}

	@RequiredReadAction
	private static void addBodyWithBraces(List<FoldingDescriptor> list, CSharpBodyWithBraces bodyWithBraces)
	{
		PsiElement leftBrace = bodyWithBraces.getLeftBrace();
		PsiElement rightBrace = bodyWithBraces.getRightBrace();
		if(leftBrace == null || rightBrace == null)
		{
			return;
		}

		list.add(new FoldingDescriptor(bodyWithBraces, new TextRange(leftBrace.getTextRange().getStartOffset(), rightBrace.getTextRange().getStartOffset() + rightBrace.getTextLength())));
	}

	@Override
	protected boolean isRegionCollapsedByDefault(@NotNull ASTNode node)
	{
		PsiElement psi = node.getPsi();
		if(psi instanceof CSharpUsingListChild)
		{
			return true;
		}
		else if(psi instanceof PsiComment)
		{
			return CodeFoldingSettings.getInstance().COLLAPSE_FILE_HEADER;
		}

		return false;
	}
}