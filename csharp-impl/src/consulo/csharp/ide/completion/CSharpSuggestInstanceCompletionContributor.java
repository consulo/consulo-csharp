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
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import consulo.annotations.RequiredReadAction;
import consulo.codeInsight.completion.CompletionProvider;
import consulo.csharp.ide.codeStyle.CSharpCodeGenerationSettings;
import consulo.csharp.ide.completion.expected.ExpectedTypeInfo;
import consulo.csharp.ide.completion.expected.ExpectedTypeVisitor;
import consulo.csharp.ide.completion.insertHandler.CSharpParenthesesWithSemicolonInsertHandler;
import consulo.csharp.ide.completion.patterns.CSharpPatterns;
import consulo.csharp.ide.completion.util.SpaceInsertHandler;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpSimpleParameterInfo;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.psi.CSharpTypeRefPresentationUtil;
import consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import consulo.csharp.lang.psi.impl.CSharpVisibilityUtil;
import consulo.csharp.lang.psi.impl.source.CSharpNewExpressionImpl;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpArrayTypeRef;
import consulo.dotnet.ide.DotNetElementPresentationUtil;
import consulo.dotnet.psi.DotNetGenericParameterListOwner;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.ide.IconDescriptorUpdaters;

/**
 * @author VISTALL
 * @since 28-Oct-17
 */
class CSharpSuggestInstanceCompletionContributor
{
	static void extend(CompletionContributor contributor)
	{
		// suggesting factory methods from expected types like Test test = Test.create()
		contributor.extend(CompletionType.BASIC, CSharpPatterns.referenceExpression(), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			public void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result)
			{
				CSharpReferenceExpression expression = PsiTreeUtil.getParentOfType(parameters.getPosition(), CSharpReferenceExpression.class);

				assert expression != null;

				// do not allow method calls inside new expression
				if(expression.getParent() instanceof DotNetType)
				{
					return;
				}

				List<ExpectedTypeInfo> expectedTypeRefs = ExpectedTypeVisitor.findExpectedTypeRefs(expression);

				if(expectedTypeRefs.isEmpty())
				{
					return;
				}

				CSharpCompletionUtil.elementToLookup(result, CSharpTokens.NEW_KEYWORD, (b, t) ->
				{
					b = b.withInsertHandler(SpaceInsertHandler.INSTANCE);
					CSharpCompletionSorting.force(b, CSharpCompletionSorting.KindSorter.Type.top);
					return b;
				}, null);

				for(ExpectedTypeInfo expectedTypeRef : expectedTypeRefs)
				{
					Pair<String, DotNetTypeDeclaration> element = CSharpTypeUtil.resolveTypeElement(expectedTypeRef.getTypeRef());
					if(element == null)
					{
						continue;
					}

					DotNetTypeDeclaration declaration = element.getSecond();
					DotNetNamedElement[] members = declaration.getMembers();
					for(DotNetNamedElement member : members)
					{
						if(member instanceof CSharpMethodDeclaration && ((CSharpMethodDeclaration) member).hasModifier(CSharpModifier.STATIC))
						{
							DotNetTypeRef returnTypeRef = ((CSharpMethodDeclaration) member).getReturnTypeRef();

							if(CSharpTypeUtil.isInheritable(expectedTypeRef.getTypeRef(), returnTypeRef, expression) && CSharpVisibilityUtil.isVisible((DotNetModifierListOwner) member, expression))
							{
								LookupElementBuilder builder = LookupElementBuilder.create(member, declaration.getName() + "." + member.getName());
								builder = builder.withLookupString(declaration.getName());
								builder = builder.withLookupString(member.getName());
								builder = builder.withTypeText(CSharpTypeRefPresentationUtil.buildShortText(returnTypeRef, expression));
								builder = builder.withIcon(IconDescriptorUpdaters.getIcon(member, Iconable.ICON_FLAG_VISIBILITY));
								builder = builder.withInsertHandler(new CSharpParenthesesWithSemicolonInsertHandler((CSharpMethodDeclaration) member));

								final CSharpSimpleParameterInfo[] parameterInfos = ((CSharpMethodDeclaration) member).getParameterInfos();

								String genericText = DotNetElementPresentationUtil.formatGenericParameters((DotNetGenericParameterListOwner) member);

								String parameterText = genericText + "(" + StringUtil.join(parameterInfos, parameter -> CSharpTypeRefPresentationUtil.buildShortText(parameter.getTypeRef(), member) +
										" " + parameter.getNotNullName(), ", ") + ")";

								builder = builder.withTailText(parameterText, false);

								CSharpCompletionSorting.force(builder, CSharpCompletionSorting.KindSorter.Type.top);

								result.addElement(builder);
							}
						}
					}
				}
			}
		});

		// Test test = new <caret>
		contributor.extend(CompletionType.BASIC, StandardPatterns.psiElement().afterLeaf(StandardPatterns.psiElement().withElementType(CSharpTokens.NEW_KEYWORD)), new CompletionProvider()
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
							builder = builder.withInsertHandler((context1, item) ->
							{
								if(context1.getCompletionChar() != '{')
								{
									int offset = context1.getEditor().getCaretModel().getOffset();
									TailType.insertChar(context1.getEditor(), offset, '{');
									TailType.insertChar(context1.getEditor(), offset + 1, '}');
									context1.getEditor().getCaretModel().moveToOffset(offset + 1);
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
