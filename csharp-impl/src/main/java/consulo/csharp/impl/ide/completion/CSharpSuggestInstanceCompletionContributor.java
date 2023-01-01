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

package consulo.csharp.impl.ide.completion;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.AllIcons;
import consulo.component.util.Iconable;
import consulo.csharp.impl.ide.codeInsight.actions.CreateUnresolvedMethodByLambdaTypeFix;
import consulo.csharp.impl.ide.codeInsight.actions.MethodGenerateUtil;
import consulo.csharp.impl.ide.completion.expected.ExpectedTypeInfo;
import consulo.csharp.impl.ide.completion.expected.ExpectedTypeVisitor;
import consulo.csharp.impl.ide.completion.insertHandler.CSharpParenthesesWithSemicolonInsertHandler;
import consulo.csharp.impl.ide.completion.patterns.CSharpPatterns;
import consulo.csharp.impl.ide.completion.util.SpaceInsertHandler;
import consulo.csharp.impl.ide.completion.weigher.CSharpInheritCompletionWeighter;
import consulo.csharp.impl.ide.refactoring.util.CSharpNameSuggesterUtil;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.impl.ide.codeStyle.CSharpCodeGenerationSettings;
import consulo.csharp.lang.impl.psi.CSharpTypeRefPresentationUtil;
import consulo.csharp.lang.impl.psi.CSharpTypeUtil;
import consulo.csharp.lang.impl.psi.CSharpVisibilityUtil;
import consulo.csharp.lang.impl.psi.source.CSharpAnonymousMethodExpression;
import consulo.csharp.lang.impl.psi.source.CSharpAssignmentExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpNativeTypeImplUtil;
import consulo.csharp.lang.impl.psi.source.CSharpNewExpressionImpl;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpArrayTypeRef;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpLambdaResolveResult;
import consulo.csharp.lang.psi.*;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.csharp.module.extension.CSharpModuleUtil;
import consulo.dotnet.psi.*;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeResolveResult;
import consulo.language.Language;
import consulo.language.codeStyle.CodeStyleManager;
import consulo.language.editor.completion.*;
import consulo.language.editor.completion.lookup.*;
import consulo.language.icon.IconDescriptorUpdaters;
import consulo.language.pattern.StandardPatterns;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.ProcessingContext;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.image.Image;
import consulo.ui.image.ImageEffects;
import consulo.util.collection.ContainerUtil;
import consulo.util.dataholder.Key;
import consulo.util.lang.Pair;
import consulo.util.lang.StringUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author VISTALL
 * @since 28-Oct-17
 */
@ExtensionImpl
public class CSharpSuggestInstanceCompletionContributor extends CompletionContributor
{
	public static final Key<Boolean> ourDefaultNewKeyword = Key.create("ourDefaultNewKeyword");

	public CSharpSuggestInstanceCompletionContributor()
	{
		extend(this);
	}

	static void extend(CompletionContributor contributor)
	{
		// Action test = obj => ...
		contributor.extend(CompletionType.BASIC, CSharpPatterns.expressionStart(), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			public void addCompletions(@Nonnull CompletionParameters parameters, ProcessingContext context, @Nonnull CompletionResultSet result)
			{
				CSharpReferenceExpressionEx parent = (CSharpReferenceExpressionEx) parameters.getPosition().getParent();
				if(parent.getQualifier() != null || parent.kind() != CSharpReferenceExpression.ResolveToKind.ANY_MEMBER)
				{
					return;
				}

				boolean allowAsync = CSharpModuleUtil.findLanguageVersion(parent).isAtLeast(CSharpLanguageVersion._4_0);
				List<ExpectedTypeInfo> expectedTypeRefs = CSharpInheritCompletionWeighter.getExpectedTypeInfosForExpression(parameters, context);
				for(ExpectedTypeInfo expectedTypeRef : expectedTypeRefs)
				{
					DotNetTypeRef typeRef = expectedTypeRef.getTypeRef();
					DotNetTypeResolveResult typeResolveResult = typeRef.resolve();
					if(typeResolveResult instanceof CSharpLambdaResolveResult)
					{
						context.getSharedContext().put(ourDefaultNewKeyword, Boolean.FALSE);

						addLambdaExpressionLookup((CSharpLambdaResolveResult) typeResolveResult, result, parent, false);

						if(allowAsync)
						{
							addLambdaExpressionLookup((CSharpLambdaResolveResult) typeResolveResult, result, parent, true);
						}

						addDelegateExpressionLookup((CSharpLambdaResolveResult) typeResolveResult, result, parent, false);

						if(allowAsync)
						{
							addDelegateExpressionLookup((CSharpLambdaResolveResult) typeResolveResult, result, parent, true);
						}

						addPrivatMethodImpl((CSharpLambdaResolveResult) typeResolveResult, parameters, result, parent);
					}
				}
			}

			private void addPrivatMethodImpl(CSharpLambdaResolveResult typeResolveResult, @Nonnull CompletionParameters parameters, CompletionResultSet result, CSharpReferenceExpressionEx parent)
			{
				PsiElement maybeAssign = parent.getParent();
				if(!(maybeAssign instanceof CSharpAssignmentExpressionImpl))
				{
					return;
				}


				String methodName = ((CSharpAssignmentExpressionImpl) maybeAssign).getLeftExpression().getText().replace(".", "_");
				LookupElementBuilder builder = LookupElementBuilder.create(methodName);
				builder = builder.withIcon(ImageEffects.layered(PlatformIconGroup.nodesMethod(), PlatformIconGroup.nodesTabpin()));
				builder = builder.withInsertHandler((insertionContext, lookupElement) ->
				{
					insertionContext.commitDocument();
					insertionContext.setLaterRunnable(() -> {
						PsiElement originalPosition = parameters.getOriginalPosition();

						CSharpReferenceExpression ref = PsiTreeUtil.getParentOfType(originalPosition, CSharpReferenceExpression.class);

						if(ref != null)
						{
							new CreateUnresolvedMethodByLambdaTypeFix(ref, typeResolveResult).invoke(ref.getProject(), insertionContext.getEditor(), ref.getContainingFile());
						}
					});
				});
				result.addElement(builder);
			}

			@RequiredReadAction
			private void addLambdaExpressionLookup(CSharpLambdaResolveResult typeResolveResult, CompletionResultSet result, CSharpReferenceExpressionEx parent, boolean async)
			{
				CSharpSimpleParameterInfo[] parameterInfos = typeResolveResult.getParameterInfos();

				StringBuilder builder = new StringBuilder();
				if(async)
				{
					builder.append("async ");
				}
				if(parameterInfos.length == 0 || parameterInfos.length > 1)
				{
					builder.append("(");
				}

				Set<String> alreadyUsedNames = new HashSet<>();
				for(int i = 0; i < parameterInfos.length; i++)
				{
					CSharpSimpleParameterInfo parameterInfo = parameterInfos[i];
					if(i != 0)
					{
						builder.append(", ");
					}
					Set<String> suggestedNames = CSharpNameSuggesterUtil.getSuggestedNames(parameterInfo.getTypeRef(), parent, alreadyUsedNames);
					if(suggestedNames.isEmpty())
					{
						String parameterName = parameterInfo.getNotNullName();
						alreadyUsedNames.add(parameterName);
						builder.append(parameterName);
					}
					else
					{
						String str = ContainerUtil.iterateAndGetLastItem(suggestedNames);
						alreadyUsedNames.add(str);
						builder.append(str);
					}
				}
				if(parameterInfos.length == 0 || parameterInfos.length > 1)
				{
					builder.append(")");
				}
				builder.append(" => ");

				LookupElementBuilder lookupElementBuilder = LookupElementBuilder.create(builder.toString());
				lookupElementBuilder = lookupElementBuilder.withPresentableText(builder.append("{ }").toString());
				lookupElementBuilder = lookupElementBuilder.withIcon((Image) AllIcons.Nodes.Lambda);

				result.addElement(PrioritizedLookupElement.withPriority(lookupElementBuilder, async ? 1.5 : 2));
			}

			@RequiredReadAction
			private void addDelegateExpressionLookup(CSharpLambdaResolveResult typeResolveResult, CompletionResultSet result, PsiElement parent, boolean async)
			{
				CSharpSimpleParameterInfo[] parameterInfos = typeResolveResult.getParameterInfos();

				StringBuilder builder = new StringBuilder();
				if(async)
				{
					builder.append("async ");
				}
				builder.append("delegate ");
				if(parameterInfos.length > 0)
				{
					builder.append("(");
				}
				Set<String> alreadyUsedNames = new HashSet<>();
				for(int i = 0; i < parameterInfos.length; i++)
				{
					CSharpSimpleParameterInfo parameterInfo = parameterInfos[i];
					if(i != 0)
					{
						builder.append(", ");
					}
					builder.append(CSharpTypeRefPresentationUtil.buildShortText(parameterInfo.getTypeRef()));
					builder.append(" ");
					Set<String> suggestedNames = CSharpNameSuggesterUtil.getSuggestedNames(parameterInfo.getTypeRef(), parent, alreadyUsedNames);
					if(suggestedNames.isEmpty())
					{
						String name = parameterInfo.getNotNullName();
						alreadyUsedNames.add(name);
						builder.append(name);
					}
					else
					{
						String name = ContainerUtil.iterateAndGetLastItem(suggestedNames);
						alreadyUsedNames.add(name);
						builder.append(name);
					}
				}
				if(parameterInfos.length > 0)
				{
					builder.append(")");
				}

				DotNetTypeRef returnTypeRef = typeResolveResult.getReturnTypeRef();
				String defaultValueForType = MethodGenerateUtil.getDefaultValueForType(returnTypeRef);
				builder.append(" { ");
				if(defaultValueForType != null)
				{
					builder.append("return ").append(defaultValueForType).append(";");
				}
				builder.append(" }");

				LookupElementBuilder lookupElementBuilder = LookupElementBuilder.create(builder.toString());
				lookupElementBuilder = lookupElementBuilder.withIcon((Image) AllIcons.Nodes.Lambda);
				lookupElementBuilder = reformatInsertHandler(lookupElementBuilder);

				result.addElement(PrioritizedLookupElement.withPriority(lookupElementBuilder, async ? 0.5 : 1));
			}

			private LookupElementBuilder reformatInsertHandler(LookupElementBuilder lookupElementBuilder)
			{
				lookupElementBuilder = lookupElementBuilder.withInsertHandler(new InsertHandler<LookupElement>()
				{
					@Override
					@RequiredReadAction
					public void handleInsert(InsertionContext context, LookupElement item)
					{
						PsiElement elementAt = context.getFile().findElementAt(context.getEditor().getCaretModel().getOffset() - 1);
						if(elementAt != null)
						{
							CSharpAnonymousMethodExpression methodExpression = PsiTreeUtil.getParentOfType(elementAt, CSharpAnonymousMethodExpression.class);
							if(methodExpression != null)
							{
								CodeStyleManager.getInstance(context.getProject()).reformat(methodExpression);
							}
						}
					}
				});
				return lookupElementBuilder;
			}

		});

		// suggesting factory methods from expected types like Test test = Test.create()
		contributor.extend(CompletionType.BASIC, CSharpPatterns.expressionStart(), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			public void addCompletions(@Nonnull CompletionParameters parameters, ProcessingContext context, @Nonnull CompletionResultSet result)
			{
				CSharpReferenceExpression expression = PsiTreeUtil.getParentOfType(parameters.getPosition(), CSharpReferenceExpression.class);

				assert expression != null;

				// do not allow method calls inside new expression & do not allow with qualified expression
				if(expression.getParent() instanceof DotNetType || expression.getQualifier() != null)
				{
					return;
				}

				List<ExpectedTypeInfo> expectedTypeRefs = ExpectedTypeVisitor.findExpectedTypeRefs(expression);

				if(expectedTypeRefs.isEmpty())
				{
					return;
				}

				// do not force new keyword if lambda
				if(context.get(ourDefaultNewKeyword) == null)
				{
					context.getSharedContext().put(ourDefaultNewKeyword, Boolean.FALSE);

					CSharpCompletionUtil.elementToLookup(result, CSharpTokens.NEW_KEYWORD, (b, t) ->
					{
						b = b.withInsertHandler(SpaceInsertHandler.INSTANCE);
						return b;
					}, null);
				}

				for(ExpectedTypeInfo expectedTypeRef : expectedTypeRefs)
				{
					Pair<String, DotNetTypeDeclaration> element = CSharpTypeUtil.resolveTypeElement(expectedTypeRef.getTypeRef());
					if(element == null)
					{
						continue;
					}

					if(CSharpNativeTypeImplUtil.ourElementToQTypes.containsValue(element.getFirst()))
					{
						continue;
					}

					DotNetTypeDeclaration declaration = element.getSecond();
					DotNetNamedElement[] members = declaration.getMembers();
					for(DotNetNamedElement member : members)
					{
						if((member instanceof CSharpMethodDeclaration || member instanceof CSharpPropertyDeclaration) && ((DotNetModifierListOwner) member).hasModifier(CSharpModifier.STATIC))
						{
							DotNetTypeRef returnTypeRef;
							if(member instanceof CSharpMethodDeclaration)
							{
								returnTypeRef = ((CSharpMethodDeclaration) member).getReturnTypeRef();
							}
							else if(member instanceof CSharpPropertyDeclaration)
							{
								returnTypeRef = ((CSharpPropertyDeclaration) member).toTypeRef(true);
							}
							else
							{
								throw new UnsupportedOperationException();
							}

							if(CSharpTypeUtil.isInheritable(expectedTypeRef.getTypeRef(), returnTypeRef) && CSharpVisibilityUtil.isVisible((DotNetModifierListOwner) member, expression))
							{
								LookupElementBuilder builder = LookupElementBuilder.create(member, declaration.getName() + "." + member.getName());
								builder = builder.withLookupString(declaration.getName());
								builder = builder.withLookupString(member.getName());
								builder = builder.withTypeText(CSharpTypeRefPresentationUtil.buildShortText(returnTypeRef));
								builder = builder.withIcon(IconDescriptorUpdaters.getIcon(member, Iconable.ICON_FLAG_VISIBILITY));
								builder = builder.withInsertHandler(new CSharpParenthesesWithSemicolonInsertHandler(member));

								if(member instanceof CSharpMethodDeclaration)
								{
									final CSharpSimpleParameterInfo[] parameterInfos = ((CSharpMethodDeclaration) member).getParameterInfos();

									String genericText = DotNetElementPresentationUtil.formatGenericParameters((DotNetGenericParameterListOwner) member);

									String parameterText = genericText + "(" + StringUtil.join(parameterInfos, parameter -> CSharpTypeRefPresentationUtil.buildShortText(parameter.getTypeRef()
									) + " " + parameter.getNotNullName(), ", ") + ")";

									builder = builder.withTailText(parameterText, false);
								}

								result.addElement(PrioritizedLookupElement.withPriority(builder, 1));
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
			public void addCompletions(@Nonnull CompletionParameters parameters, ProcessingContext context, @Nonnull CompletionResultSet result)
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
							String typeText = CSharpTypeRefPresentationUtil.buildShortText(typeRef);

							LookupElementBuilder builder = LookupElementBuilder.create(typeRef, typeText);
							builder = builder.withIcon(getIconForInnerTypeRef((CSharpArrayTypeRef) typeRef, position));
							builder = builder.withInsertHandler((c, item) -> c.getEditor().getCaretModel().moveToOffset(c.getTailOffset() - 1));
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
			private Image getIconForInnerTypeRef(@Nonnull CSharpArrayTypeRef typeRef, @Nonnull PsiElement scope)
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
			private Image getIconForInnerTypeRef(DotNetTypeRef innerTypeRef, @Nonnull PsiElement scope)
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

	@Nonnull
	@Override
	public Language getLanguage()
	{
		return CSharpLanguage.INSTANCE;
	}
}
