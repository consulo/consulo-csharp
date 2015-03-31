/*
 * Copyright 2000-2009 JetBrains s.r.o.
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
package org.mustbe.consulo.csharp.lang.doc.ide.codeInsight.editorActions;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.doc.psi.CSharpDocAttributeValue;
import org.mustbe.consulo.csharp.lang.doc.psi.CSharpDocTag;
import org.mustbe.consulo.csharp.lang.doc.psi.CSharpDocText;
import org.mustbe.consulo.csharp.lang.doc.psi.CSharpDocTokenType;
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.codeInsight.highlighting.BraceMatchingUtil;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.TokenType;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.RoleFinder;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Function;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.ContainerUtil;

@org.consulo.lombok.annotations.Logger
public class CSharpDocGtTypedHandler extends TypedHandlerDelegate
{
	private static final RoleFinder CLOSING_TAG_NAME_FINDER = new RoleFinder()
	{
		@Override
		@Nullable
		public ASTNode findChild(@NotNull ASTNode parent)
		{
			final PsiElement element = getEndTagNameElement((CSharpDocTag) parent.getPsi());
			return element == null ? null : element.getNode();
		}
	};

	@Override
	public Result beforeCharTyped(final char c, final Project project, final Editor editor, final PsiFile editedFile, final FileType fileType)
	{
		if(c == '>')
		{
			PsiDocumentManager.getInstance(project).commitAllDocuments();

			PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
			FileViewProvider provider = editedFile.getViewProvider();
			int offset = editor.getCaretModel().getOffset();

			PsiElement element, elementAtCaret = null;

			if(offset < editor.getDocument().getTextLength())
			{
				elementAtCaret = element = provider.findElementAt(offset);

				if(!(element instanceof PsiWhiteSpace))
				{
					boolean nonAcceptableDelimiter = true;

					if(element != null)
					{
						IElementType tokenType = getElementType(element);

						if(tokenType == CSharpDocTokenType.XML_START_TAG_START || tokenType == CSharpDocTokenType.XML_END_TAG_START)
						{
							if(offset > 0)
							{
								PsiElement previousElement = provider.findElementAt(offset - 1);

								if(previousElement != null)
								{
									tokenType = getElementType(previousElement);
									element = previousElement;
									nonAcceptableDelimiter = false;
								}
							}
						}
						else if(tokenType == CSharpDocTokenType.XML_NAME)
						{
							if(element.getNextSibling() instanceof PsiErrorElement)
							{
								nonAcceptableDelimiter = false;
							}
						}

						if(tokenType == CSharpDocTokenType.XML_TAG_END || tokenType == CSharpDocTokenType.XML_EMPTY_ELEMENT_END && element
								.getTextOffset() == offset - 1)
						{
							editor.getCaretModel().moveToOffset(offset + 1);
							editor.getScrollingModel().scrollToCaret(ScrollType.RELATIVE);
							return Result.STOP;
						}
					}
					if(nonAcceptableDelimiter)
					{
						return Result.CONTINUE;
					}
				}
				else
				{
					// check if right after empty end
					PsiElement previousElement = provider.findElementAt(offset - 1);
					if(previousElement != null)
					{
						final IElementType tokenType = getElementType(previousElement);

						if(tokenType == CSharpDocTokenType.XML_EMPTY_ELEMENT_END)
						{
							return Result.STOP;
						}
					}
				}

				PsiElement parent = element.getParent();
				if(parent instanceof CSharpDocText)
				{
					final String text = parent.getText();
					// check /
					final int index = offset - parent.getTextOffset() - 1;

					if(index >= 0 && text.charAt(index) == '/')
					{
						return Result.CONTINUE; // already seen /
					}
					element = parent.getPrevSibling();
				}
				else if(parent instanceof CSharpDocTag && !(element.getPrevSibling() instanceof CSharpDocTag))
				{
					element = parent;
				}
				else if(parent instanceof CSharpDocAttributeValue)
				{
					element = parent;
				}
			}
			else
			{
				element = provider.findElementAt(editor.getDocument().getTextLength() - 1);
				if(element == null)
				{
					return Result.CONTINUE;
				}
				element = element.getParent();
			}

			if(offset > 0 && offset <= editor.getDocument().getTextLength())
			{
				if(editor.getDocument().getCharsSequence().charAt(offset - 1) == '/')
				{ // Some languages (e.g. GSP) allow character '/' in tag name.
					return Result.CONTINUE;
				}
			}

			if(element instanceof CSharpDocAttributeValue)
			{
				element = element.getParent().getParent();
			}

			while(element instanceof PsiWhiteSpace)
			{
				element = element.getPrevSibling();
			}

			if(element == null)
			{
				return Result.CONTINUE;
			}
			if(!(element instanceof CSharpDocTag))
			{
				if(element.getPrevSibling() != null &&
						element.getPrevSibling().getText().equals("<"))
				{
					// tag is started and there is another text in the end
					editor.getDocument().insertString(offset, "</" + element.getText() + ">");
				}
				return Result.CONTINUE;
			}

			CSharpDocTag tag = (CSharpDocTag) element;
			if(getTokenOfType(tag, CSharpDocTokenType.XML_TAG_END) != null)
			{
				return Result.CONTINUE;
			}
			if(getTokenOfType(tag, CSharpDocTokenType.XML_EMPTY_ELEMENT_END) != null)
			{
				return Result.CONTINUE;
			}
			final PsiElement startToken = getTokenOfType(tag, CSharpDocTokenType.XML_START_TAG_START);
			if(startToken == null || !startToken.getText().equals("<"))
			{
				return Result.CONTINUE;
			}

			String name = tag.getName();
			if(elementAtCaret != null && getElementType(elementAtCaret) == CSharpDocTokenType.XML_NAME)
			{
				name = name.substring(0, offset - elementAtCaret.getTextOffset());
			}

			if("".equals(name))
			{
				return Result.CONTINUE;
			}

			int tagOffset = tag.getTextRange().getStartOffset();

			final PsiElement nameToken = getTokenOfType(tag, CSharpDocTokenType.XML_NAME);
			if(nameToken != null && nameToken.getTextRange().getStartOffset() > offset)
			{
				return Result.CONTINUE;
			}

			HighlighterIterator iterator = ((EditorEx) editor).getHighlighter().createIterator(tagOffset);
			if(BraceMatchingUtil.matchBrace(editor.getDocument().getCharsSequence(), editedFile.getFileType(), iterator, true, true))
			{
				PsiElement parent = tag.getParent();
				boolean hasBalance = true;

				while(parent instanceof CSharpDocTag && name.equals(((CSharpDocTag) parent).getName()))
				{
					ASTNode astNode = CLOSING_TAG_NAME_FINDER.findChild(parent.getNode());
					if(astNode == null)
					{
						hasBalance = false;
						break;
					}

					parent = parent.getParent();
				}

				if(hasBalance)
				{
					hasBalance = false;
					for(ASTNode node = parent.getNode().getLastChildNode(); node != null; node = node.getTreePrev())
					{
						ASTNode leaf = node;
						if(leaf.getElementType() == TokenType.ERROR_ELEMENT)
						{
							ASTNode firstChild = leaf.getFirstChildNode();
							if(firstChild != null)
							{
								leaf = firstChild;
							}
							else
							{
								PsiElement psiElement = PsiTreeUtil.nextLeaf(leaf.getPsi());
								leaf = psiElement != null ? psiElement.getNode() : null;
							}
							if(leaf != null && leaf.getElementType() == TokenType.WHITE_SPACE)
							{
								PsiElement psiElement = PsiTreeUtil.nextLeaf(leaf.getPsi());
								if(psiElement != null)
								{
									leaf = psiElement.getNode();
								}
							}
						}

						if(leaf != null && leaf.getElementType() == CSharpDocTokenType.XML_END_TAG_START)
						{
							ASTNode treeNext = leaf.getTreeNext();
							IElementType treeNextType;
							if(treeNext != null && ((treeNextType = treeNext.getElementType()) == CSharpDocTokenType.XML_NAME || treeNextType ==
									CSharpDocTokenType.XML_TAG_NAME))
							{
								if(name.equals(treeNext.getText()))
								{
									ASTNode parentEndName = parent instanceof CSharpDocTag ? CLOSING_TAG_NAME_FINDER.findChild(parent.getNode()) :
											null;
									hasBalance = !(parent instanceof CSharpDocTag) || parentEndName != null && !parentEndName.getText().equals(name);
									break;
								}
							}
						}
					}
				}

				if(hasBalance)
				{
					return Result.CONTINUE;
				}
			}

			TextRange cdataReformatRange = null;

			editor.getDocument().insertString(offset, "</" + name + ">");

			if(cdataReformatRange != null)
			{
				PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());
				try
				{
					CodeStyleManager.getInstance(project).reformatText(file, cdataReformatRange.getStartOffset(), cdataReformatRange.getEndOffset());
				}
				catch(IncorrectOperationException e)
				{
					LOGGER.error(e);
				}
			}
			return cdataReformatRange != null ? Result.STOP : Result.CONTINUE;
		}
		return Result.CONTINUE;
	}

	@Nullable
	public static PsiElement getEndTagNameElement(@NotNull CSharpDocTag tag)
	{
		final ASTNode node = tag.getNode();
		if(node == null)
		{
			return null;
		}

		ASTNode current = node.getLastChildNode();
		ASTNode prev = current;

		while(current != null)
		{
			final IElementType elementType = prev.getElementType();
			if((elementType == CSharpDocTokenType.XML_NAME || elementType == CSharpDocTokenType.XML_TAG_NAME) && current.getElementType() ==
					CSharpDocTokenType.XML_END_TAG_START)
			{
				return prev.getPsi();
			}

			prev = current;
			current = current.getTreePrev();

		}
		return null;
	}

	public static IElementType getElementType(PsiElement element)
	{
		ASTNode node = element.getNode();
		return node == null ? null : node.getElementType();
	}

	@Nullable
	public static PsiElement getTokenOfType(PsiElement element, IElementType type)
	{
		if(element == null)
		{
			return null;
		}

		List<PsiElement> map = ContainerUtil.map(element.getNode().getChildren(TokenSet.create(type)), new Function<ASTNode, PsiElement>()
		{
			@Override
			public PsiElement fun(ASTNode astNode)
			{
				return astNode.getPsi();
			}
		});
		return ContainerUtil.getFirstItem(map);
	}
}