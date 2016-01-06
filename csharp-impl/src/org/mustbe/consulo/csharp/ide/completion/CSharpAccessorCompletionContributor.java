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

package org.mustbe.consulo.csharp.ide.completion;

import static com.intellij.patterns.StandardPatterns.psiElement;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.codeInsight.completion.CompletionProvider;
import org.mustbe.consulo.csharp.lang.psi.CSharpEventDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpSoftTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpXXXAccessorOwner;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetXXXAccessor;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;

/**
 * @author VISTALL
 * @since 24.12.14
 */
public class CSharpAccessorCompletionContributor extends CompletionContributor
{
	public CSharpAccessorCompletionContributor()
	{
		extend(CompletionType.BASIC, psiElement().andNot(psiElement().inside(DotNetXXXAccessor.class)), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet resultSet)
			{
				PsiElement position = completionParameters.getPosition();
				final CSharpXXXAccessorOwner accessorOwner = PsiTreeUtil.getParentOfType(position, CSharpXXXAccessorOwner.class);
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
					if(accessorOwner instanceof DotNetModifierListOwner && ((DotNetModifierListOwner) accessorOwner).hasModifier(DotNetModifier.ABSTRACT))
					{
						buildAccessorKeywordsCompletion(resultSet, accessorOwner, null, null);
					}
					else
					{
						buildAccessorKeywordsCompletion(resultSet, accessorOwner, " {}", new InsertHandler<LookupElement>()
						{
							@Override
							public void handleInsert(InsertionContext context, LookupElement item)
							{
								Editor editor = context.getEditor();

								CaretModel caretModel = editor.getCaretModel();
								int offset = caretModel.getOffset();
								caretModel.moveToOffset(offset - 1);
							}
						});

						if(accessorOwner instanceof CSharpPropertyDeclaration)
						{
							buildAccessorKeywordsCompletion(resultSet, accessorOwner, null, null);
						}
					}
				}
			}
		});
	}

	private void buildAccessorKeywordsCompletion(CompletionResultSet resultSet, final CSharpXXXAccessorOwner accessorOwner, @Nullable String tail,
			@Nullable InsertHandler<LookupElement> insertHandler)
	{
		TokenSet tokenSet = accessorOwner instanceof CSharpEventDeclaration ? TokenSet.create(CSharpSoftTokens.ADD_KEYWORD, CSharpSoftTokens.REMOVE_KEYWORD) : TokenSet.create(CSharpSoftTokens
				.GET_KEYWORD, CSharpSoftTokens.SET_KEYWORD);

		for(IElementType elementType : tokenSet.getTypes())
		{
			if(!isCanShowAccessorKeyword(elementType, accessorOwner))
			{
				continue;
			}
			String textOfKeyword = CSharpCompletionUtil.textOfKeyword(elementType);
			LookupElementBuilder builder = LookupElementBuilder.create(tail != null ? textOfKeyword + tail : textOfKeyword);
			if(tail == null)
			{
				builder = builder.bold();
			}
			if(insertHandler != null)
			{
				builder = builder.withInsertHandler(insertHandler);
			}

			resultSet.addElement(builder);
		}
	}

	private static boolean isCanShowAccessorKeyword(IElementType elementType, CSharpXXXAccessorOwner accessorOwner)
	{
		DotNetXXXAccessor[] accessors = accessorOwner.getAccessors();
		for(DotNetXXXAccessor accessor : accessors)
		{
			DotNetXXXAccessor.Kind accessorKind = accessor.getAccessorKind();
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
