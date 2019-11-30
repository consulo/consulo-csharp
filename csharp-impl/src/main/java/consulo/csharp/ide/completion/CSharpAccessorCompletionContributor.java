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

package consulo.csharp.ide.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler;
import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.codeInsight.completion.CompletionProvider;
import consulo.csharp.lang.psi.CSharpEventDeclaration;
import consulo.csharp.lang.psi.CSharpSoftTokens;
import consulo.csharp.lang.psi.CSharpXAccessorOwner;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetXAccessor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.intellij.patterns.StandardPatterns.psiElement;

/**
 * @author VISTALL
 * @since 24.12.14
 */
class CSharpAccessorCompletionContributor
{
	static void extend(CompletionContributor contributor)
	{
		contributor.extend(CompletionType.BASIC, psiElement().andNot(psiElement().inside(DotNetXAccessor.class)), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			public void addCompletions(@Nonnull CompletionParameters completionParameters, ProcessingContext processingContext, @Nonnull CompletionResultSet resultSet)
			{
				PsiElement position = completionParameters.getPosition();
				final CSharpXAccessorOwner accessorOwner = PsiTreeUtil.getParentOfType(position, CSharpXAccessorOwner.class);
				if(accessorOwner == null)
				{
					return;
				}
				PsiElement leftBrace = accessorOwner.getLeftBrace();
				if(leftBrace == null)
				{
					return;
				}

				int textOffset = position.getTextOffset();
				PsiElement rightBrace = accessorOwner.getRightBrace();
				int rightTextRange = rightBrace == null ? -1 : rightBrace.getTextOffset();

				if((rightTextRange == -1 || textOffset < rightTextRange) && textOffset > leftBrace.getTextOffset())
				{
					if(accessorOwner.hasModifier(DotNetModifier.ABSTRACT))
					{
						buildAccessorKeywordsCompletion(resultSet, accessorOwner, null);
					}
					else
					{
						buildAccessorKeywordsCompletion(resultSet, accessorOwner, new InsertHandler<LookupElement>()
						{
							@Override
							@RequiredWriteAction
							public void handleInsert(InsertionContext context, LookupElement item)
							{
								if(context.getCompletionChar() == '{')
								{
									context.setAddCompletionChar(false);

									Editor editor = context.getEditor();

									CaretModel caretModel = editor.getCaretModel();
									int offset = caretModel.getOffset();

									context.getDocument().insertString(offset, "{\n}");
									caretModel.moveToOffset(offset + 1);

									PsiElement elementAt = context.getFile().findElementAt(offset - 1);

									context.commitDocument();

									DotNetXAccessor accessor = PsiTreeUtil.getParentOfType(elementAt, DotNetXAccessor.class);
									if(accessor != null)
									{
										CodeStyleManager.getInstance(context.getProject()).reformat(accessor);
									}

									EditorWriteActionHandler actionHandler = (EditorWriteActionHandler) EditorActionManager.getInstance().getActionHandler(IdeActions.ACTION_EDITOR_ENTER);
									actionHandler.executeWriteAction(editor, DataManager.getInstance().getDataContext(editor.getContentComponent()));
								}
							}
						});
					}
				}
			}
		});
	}

	private static void buildAccessorKeywordsCompletion(CompletionResultSet resultSet, final CSharpXAccessorOwner accessorOwner, @Nullable InsertHandler<LookupElement> insertHandler)
	{
		TokenSet tokenSet = accessorOwner instanceof CSharpEventDeclaration ? TokenSet.create(CSharpSoftTokens.ADD_KEYWORD, CSharpSoftTokens.REMOVE_KEYWORD) : TokenSet.create(CSharpSoftTokens
				.GET_KEYWORD, CSharpSoftTokens.SET_KEYWORD);

		for(IElementType elementType : tokenSet.getTypes())
		{
			if(!isCanShowAccessorKeyword(elementType, accessorOwner))
			{
				continue;
			}
			LookupElementBuilder builder = LookupElementBuilder.create(CSharpCompletionUtil.textOfKeyword(elementType));
			builder = builder.bold();

			if(insertHandler != null)
			{
				builder = builder.withInsertHandler(insertHandler);
			}

			builder.putUserData(CSharpCompletionUtil.KEYWORD_ELEMENT_TYPE, elementType);

			resultSet.addElement(builder);
		}
	}

	private static boolean isCanShowAccessorKeyword(IElementType elementType, CSharpXAccessorOwner accessorOwner)
	{
		DotNetXAccessor[] accessors = accessorOwner.getAccessors();
		for(DotNetXAccessor accessor : accessors)
		{
			DotNetXAccessor.Kind accessorKind = accessor.getAccessorKind();
			if(accessorKind == null)
			{
				continue;
			}
			IElementType expectedElementType;
			switch(accessorKind)
			{
				case GET:
					expectedElementType = CSharpSoftTokens.GET_KEYWORD;
					break;
				case SET:
					expectedElementType = CSharpSoftTokens.SET_KEYWORD;
					break;
				case ADD:
					expectedElementType = CSharpSoftTokens.ADD_KEYWORD;
					break;
				case REMOVE:
					expectedElementType = CSharpSoftTokens.REMOVE_KEYWORD;
					break;
				default:
					continue;
			}
			if(expectedElementType == elementType)
			{
				return false;
			}
		}
		return true;
	}
}
