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

import java.util.List;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.codeInsight.TailType;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.Iconable;
import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import consulo.annotations.RequiredReadAction;
import consulo.codeInsight.completion.CompletionProvider;
import consulo.csharp.ide.codeStyle.CSharpCodeGenerationSettings;
import consulo.csharp.ide.completion.expected.ExpectedTypeInfo;
import consulo.csharp.ide.completion.expected.ExpectedTypeVisitor;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.psi.CSharpTypeRefPresentationUtil;
import consulo.csharp.lang.psi.impl.source.CSharpNewExpressionImpl;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpArrayTypeRef;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.ide.IconDescriptorUpdaters;

/**
 * @author VISTALL
 * @since 28-Oct-17
 */
public class CSharpSuggestInstanceCompletionContributor extends CompletionContributor
{
	public CSharpSuggestInstanceCompletionContributor()
	{
		extend(CompletionType.BASIC, StandardPatterns.psiElement().afterLeaf(StandardPatterns.psiElement().withElementType(CSharpTokens.NEW_KEYWORD)), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			public void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result)
			{
				PsiElement position = parameters.getPosition();
				CSharpNewExpressionImpl newExpression = PsiTreeUtil.getParentOfType(position, CSharpNewExpressionImpl.class);
				if(newExpression == null)
				{
					return;
				}

				List<ExpectedTypeInfo> expectedTypeRefs = ExpectedTypeVisitor.findExpectedTypeRefs(newExpression);

				if(!expectedTypeRefs.isEmpty())
				{
					for(ExpectedTypeInfo expectedTypeInfo : expectedTypeRefs)
					{
						DotNetTypeRef typeRef = expectedTypeInfo.getTypeRef();
						if(typeRef instanceof CSharpArrayTypeRef)
						{
							if(((CSharpArrayTypeRef) typeRef).getDimensions() != 0)
							{
								continue;
							}
							String typeText = CSharpTypeRefPresentationUtil.buildShortText(typeRef, position);

							LookupElementBuilder builder = LookupElementBuilder.create(typeRef, typeText);
							builder = builder.withIcon(getIconForInnerTypeRef((CSharpArrayTypeRef) typeRef, position));
							// add without {...}
							result.addElement(PrioritizedLookupElement.withPriority(builder, 1));

							builder = builder.withTailText("{...}", true);
							builder = builder.withInsertHandler(new InsertHandler<LookupElement>()
							{
								@Override
								public void handleInsert(InsertionContext context, LookupElement item)
								{
									if(context.getCompletionChar() != '{')
									{
										int offset = context.getEditor().getCaretModel().getOffset();
										TailType.insertChar(context.getEditor(), offset, '{');
										TailType.insertChar(context.getEditor(), offset + 1, '}');
										context.getEditor().getCaretModel().moveToOffset(offset + 1);
									}
								}
							});

							result.addElement(PrioritizedLookupElement.withPriority(builder, 1));
						}
					}
				}
			}

			@Nullable
			@RequiredReadAction
			private Icon getIconForInnerTypeRef(@NotNull CSharpArrayTypeRef typeRef, @NotNull PsiElement scope)
			{
				DotNetTypeRef innerTypeRef = typeRef.getInnerTypeRef();
				if(innerTypeRef instanceof CSharpArrayTypeRef)
				{
					DotNetTypeRef innerNext = ((CSharpArrayTypeRef) innerTypeRef).getInnerTypeRef();
					if(innerNext instanceof CSharpArrayTypeRef)
					{
						return getIconForInnerTypeRef((CSharpArrayTypeRef) innerNext, scope);
					}
					return getIconForInnerTypeRef(innerTypeRef, scope);
				}
				else
				{
					return getIconForInnerTypeRef(innerTypeRef, scope);
				}
			}

			@Nullable
			@RequiredReadAction
			private Icon getIconForInnerTypeRef(DotNetTypeRef innerTypeRef, @NotNull PsiElement scope)
			{
				PsiElement element = innerTypeRef.resolve().getElement();
				if(element != null)
				{
					if(element instanceof DotNetTypeDeclaration)
					{
						String vmQName = ((DotNetTypeDeclaration) element).getVmQName();
						String keyword = CSharpTypeRefPresentationUtil.ourTypesAsKeywords.get(vmQName);
						if(keyword != null && CSharpCodeGenerationSettings.getInstance(scope.getProject()).USE_LANGUAGE_DATA_TYPES)
						{
							return null;
						}
					}
					return IconDescriptorUpdaters.getIcon(element, Iconable.ICON_FLAG_VISIBILITY);
				}
				else
				{
					return AllIcons.Nodes.Class;
				}
			}
		});
	}
}
