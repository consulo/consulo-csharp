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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredDispatchThread;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.RequiredWriteAction;
import org.mustbe.consulo.codeInsight.completion.CompletionProvider;
import org.mustbe.consulo.csharp.ide.CSharpLookupElementBuilder;
import org.mustbe.consulo.csharp.ide.codeInsight.actions.MethodGenerateUtil;
import org.mustbe.consulo.csharp.ide.codeStyle.CSharpCodeGenerationSettings;
import org.mustbe.consulo.csharp.ide.completion.expected.ExpectedTypeInfo;
import org.mustbe.consulo.csharp.ide.completion.expected.ExpectedTypeVisitor;
import org.mustbe.consulo.csharp.ide.completion.insertHandler.CSharpTailInsertHandler;
import org.mustbe.consulo.csharp.ide.completion.item.CSharpTypeLikeLookupElement;
import org.mustbe.consulo.csharp.ide.completion.patterns.CSharpPatterns;
import org.mustbe.consulo.csharp.ide.completion.util.SpaceInsertHandler;
import org.mustbe.consulo.csharp.lang.psi.*;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.*;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.CSharpResolveOptions;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpArrayTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpGenericExtractor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaResolveResult;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpRefTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpMethodImplUtil;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.csharp.module.extension.CSharpModuleUtil;
import org.mustbe.consulo.dotnet.DotNetRunUtil;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.ide.DotNetElementPresentationUtil;
import org.mustbe.consulo.dotnet.psi.*;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.codeInsight.TailType;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.completion.util.ParenthesesInsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.ide.IconDescriptorUpdaters;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.util.Function;
import com.intellij.util.NotNullPairFunction;
import com.intellij.util.ProcessingContext;
import com.intellij.util.Processor;

/**
 * @author VISTALL
 * @since 23.11.14
 */
public class CSharpExpressionCompletionContributor extends CompletionContributor
{
	private static final TokenSet ourExpressionLiterals = TokenSet.create(CSharpTokens.NULL_LITERAL, CSharpTokens.FALSE_KEYWORD, CSharpTokens.TRUE_KEYWORD, CSharpTokens.DEFAULT_KEYWORD,
			CSharpTokens.TYPEOF_KEYWORD, CSharpTokens.SIZEOF_KEYWORD, CSharpTokens.THIS_KEYWORD, CSharpTokens.BASE_KEYWORD, CSharpSoftTokens.AWAIT_KEYWORD, CSharpTokens.NEW_KEYWORD,
			CSharpTokens.__MAKEREF_KEYWORD, CSharpTokens.__REFTYPE_KEYWORD, CSharpTokens.__REFVALUE_KEYWORD, CSharpSoftTokens.NAMEOF_KEYWORD);

	public CSharpExpressionCompletionContributor()
	{
		extend(CompletionType.BASIC, psiElement().afterLeaf(psiElement().withElementType(CSharpTokens.NEW_KEYWORD)), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result)
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
				if(!(innerTypeRef instanceof CSharpArrayTypeRef))
				{
					PsiElement element = innerTypeRef.resolve(scope).getElement();
					if(element != null)
					{
						if(element instanceof DotNetTypeDeclaration)
						{
							String vmQName = ((DotNetTypeDeclaration) element).getVmQName();
							String keyword = CSharpTypeRefPresentationUtil.ourTypesAsKeywords.get(vmQName);
							if(keyword != null && CSharpCodeGenerationSettings.getInstance(scope.getProject()).USE_LANGUAGE_DATA_TYPES )
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
				else
				{
					return getIconForInnerTypeRef((CSharpArrayTypeRef) ((CSharpArrayTypeRef) innerTypeRef).getInnerTypeRef(), scope);
				}
			}
		});

		extend(CompletionType.BASIC, CSharpPatterns.referenceExpression(), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			protected void addCompletions(@NotNull final CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result)
			{
				final CSharpReferenceExpressionEx parent = (CSharpReferenceExpressionEx) parameters.getPosition().getParent();
				if(parent.getQualifier() == null && (parent.kind() == CSharpReferenceExpression.ResolveToKind.ANY_MEMBER || parent.kind() == CSharpReferenceExpression.ResolveToKind
						.EXPRESSION_OR_TYPE_LIKE))
				{
					CSharpCompletionUtil.tokenSetToLookup(result, ourExpressionLiterals, new NotNullPairFunction<LookupElementBuilder, IElementType, LookupElement>()
							{
								@NotNull
								@Override
								public LookupElement fun(LookupElementBuilder t, IElementType elementType)
								{
									if(elementType == CSharpTokens.DEFAULT_KEYWORD ||
											elementType == CSharpTokens.TYPEOF_KEYWORD ||
											elementType == CSharpSoftTokens.NAMEOF_KEYWORD ||
											elementType == CSharpTokens.__MAKEREF_KEYWORD ||
											elementType == CSharpTokens.__REFTYPE_KEYWORD ||
											elementType == CSharpTokens.__REFVALUE_KEYWORD ||
											elementType == CSharpTokens.SIZEOF_KEYWORD)
									{
										t = t.withTailText("(...)", true);
										t = t.withInsertHandler(ParenthesesInsertHandler.getInstance(true));
									}
									else if(elementType == CSharpTokens.NEW_KEYWORD)
									{
										t = t.withInsertHandler(SpaceInsertHandler.INSTANCE);
									}
									else if(elementType == CSharpSoftTokens.AWAIT_KEYWORD)
									{
										t = t.withInsertHandler(new InsertHandler<LookupElement>()
										{
											@Override
											@RequiredDispatchThread
											public void handleInsert(InsertionContext context, LookupElement item)
											{
												CSharpSimpleLikeMethodAsElement methodAsElement = PsiTreeUtil.getParentOfType(parameters.getOriginalPosition(), CSharpSimpleLikeMethodAsElement.class);
												if(methodAsElement != null && !methodAsElement.hasModifier(CSharpModifier.ASYNC))
												{
													DotNetModifierList modifierList = methodAsElement.getModifierList();
													assert modifierList != null;
													modifierList.addModifier(CSharpModifier.ASYNC);
													PsiDocumentManager.getInstance(context.getProject()).doPostponedOperationsAndUnblockDocument(context.getDocument());
												}

												SpaceInsertHandler.INSTANCE.handleInsert(context, item);
											}
										});
									}
									return t;
								}
							}, new Condition<IElementType>()
							{
								@Override
								@RequiredReadAction
								public boolean value(IElementType elementType)
								{
									if(elementType == CSharpTokens.BASE_KEYWORD || elementType == CSharpTokens.THIS_KEYWORD)
									{
										DotNetModifierListOwner owner = (DotNetModifierListOwner) PsiTreeUtil.getContextOfType(parent, DotNetQualifiedElement.class);
										if(owner == null || owner.hasModifier(DotNetModifier.STATIC))
										{
											return false;
										}
										return true;
									}
									if(elementType == CSharpSoftTokens.AWAIT_KEYWORD)
									{
										CSharpSimpleLikeMethodAsElement methodAsElement = PsiTreeUtil.getParentOfType(parameters.getOriginalPosition(), CSharpSimpleLikeMethodAsElement.class);

										if(methodAsElement == null || methodAsElement instanceof DotNetMethodDeclaration && DotNetRunUtil.isEntryPoint((DotNetMethodDeclaration) methodAsElement))
										{
											return false;
										}

										return CSharpModuleUtil.findLanguageVersion(parent).isAtLeast(CSharpLanguageVersion._4_0);
									}
									if(elementType == CSharpSoftTokens.NAMEOF_KEYWORD)
									{
										return CSharpModuleUtil.findLanguageVersion(parent).isAtLeast(CSharpLanguageVersion._6_0);
									}
									return true;
								}
							}
					);
				}
			}
		});

		extend(CompletionType.BASIC, CSharpPatterns.referenceExpression(), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result)
			{
				CSharpReferenceExpressionEx parent = (CSharpReferenceExpressionEx) parameters.getPosition().getParent();
				if(parent.getQualifier() != null || parent.kind() != CSharpReferenceExpression.ResolveToKind.ANY_MEMBER)
				{
					return;
				}

				boolean allowAsync = CSharpModuleUtil.findLanguageVersion(parent).isAtLeast(CSharpLanguageVersion._4_0);
				List<ExpectedTypeInfo> expectedTypeRefs = getExpectedTypeInfosForExpression(parameters, context);
				for(ExpectedTypeInfo expectedTypeRef : expectedTypeRefs)
				{
					DotNetTypeRef typeRef = expectedTypeRef.getTypeRef();
					DotNetTypeResolveResult typeResolveResult = typeRef.resolve(parent);
					if(typeResolveResult instanceof CSharpLambdaResolveResult)
					{
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
					}
				}
			}

			@RequiredReadAction
			private void addLambdaExpressionLookup(CSharpLambdaResolveResult typeResolveResult, CompletionResultSet result, PsiElement parent, boolean async)
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
				for(int i = 0; i < parameterInfos.length; i++)
				{
					CSharpSimpleParameterInfo parameterInfo = parameterInfos[i];
					if(i != 0)
					{
						builder.append(", ");
					}
					builder.append(parameterInfo.getNotNullName());
				}
				if(parameterInfos.length == 0 || parameterInfos.length > 1)
				{
					builder.append(")");
				}
				builder.append(" => ");

				LookupElementBuilder lookupElementBuilder = LookupElementBuilder.create(builder.toString());
				lookupElementBuilder = lookupElementBuilder.withPresentableText(builder.append("{ }").toString());
				lookupElementBuilder = lookupElementBuilder.withIcon(AllIcons.Nodes.Lambda);

				CSharpCompletionSorting.force(lookupElementBuilder, CSharpCompletionSorting.KindSorter.Type.lambda);
				result.addElement(lookupElementBuilder);
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
				for(int i = 0; i < parameterInfos.length; i++)
				{
					CSharpSimpleParameterInfo parameterInfo = parameterInfos[i];
					if(i != 0)
					{
						builder.append(", ");
					}
					builder.append(CSharpTypeRefPresentationUtil.buildShortText(parameterInfo.getTypeRef(), parent));
					builder.append(" ");
					builder.append(parameterInfo.getNotNullName());
				}
				if(parameterInfos.length > 0)
				{
					builder.append(")");
				}

				DotNetTypeRef returnTypeRef = typeResolveResult.getReturnTypeRef();
				String defaultValueForType = MethodGenerateUtil.getDefaultValueForType(returnTypeRef, parent);
				builder.append(" { ");
				if(defaultValueForType != null)
				{
					builder.append("return ").append(defaultValueForType).append(";");
				}
				builder.append(" }");

				LookupElementBuilder lookupElementBuilder = LookupElementBuilder.create(builder.toString());
				lookupElementBuilder = lookupElementBuilder.withIcon(AllIcons.Nodes.Lambda);
				lookupElementBuilder = reformatInsertHandler(lookupElementBuilder);

				CSharpCompletionSorting.force(lookupElementBuilder, CSharpCompletionSorting.KindSorter.Type.delegate);
				result.addElement(lookupElementBuilder);
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

		extend(CompletionType.BASIC, CSharpPatterns.referenceExpression(), new CompletionProvider()
		{
			@Override
			@RequiredReadAction
			protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result)
			{
				List<ExpectedTypeInfo> expectedTypeInfos = getExpectedTypeInfosForExpression(parameters, context);

				for(ExpectedTypeInfo expectedTypeInfo : expectedTypeInfos)
				{
					DotNetTypeRef typeRef = expectedTypeInfo.getTypeRef();
					if(typeRef instanceof CSharpRefTypeRef)
					{
						CSharpRefTypeRef.Type type = ((CSharpRefTypeRef) typeRef).getType();

						IElementType elementType = null;
						switch(type)
						{
							case out:
								elementType = CSharpTokens.OUT_KEYWORD;
								break;
							case ref:
								elementType = CSharpTokens.REF_KEYWORD;
								break;
						}

						assert elementType != null;

						CSharpCompletionUtil.elementToLookup(result, elementType, new NotNullPairFunction<LookupElementBuilder, IElementType, LookupElement>()
						{
							@NotNull
							@Override
							public LookupElement fun(LookupElementBuilder lookupElementBuilder, IElementType iElementType)
							{
								lookupElementBuilder = lookupElementBuilder.withInsertHandler(SpaceInsertHandler.INSTANCE);
								return lookupElementBuilder;
							}
						}, null);
					}
				}
			}
		});

		extend(CompletionType.BASIC, CSharpPatterns.referenceExpression(), new CompletionProvider()
		{
			@Override
			@RequiredReadAction
			protected void addCompletions(@NotNull final CompletionParameters parameters, ProcessingContext context, @NotNull final CompletionResultSet result)
			{
				final CSharpReferenceExpressionEx expression = (CSharpReferenceExpressionEx) parameters.getPosition().getParent();
				CSharpReferenceExpression.ResolveToKind kind = expression.kind();
				if(needRemapToAnyResolving(kind, expression))
				{
					kind = CSharpReferenceExpression.ResolveToKind.ANY_MEMBER;
				}

				if(kind == CSharpReferenceExpression.ResolveToKind.CONSTRUCTOR)
				{
					kind = CSharpReferenceExpression.ResolveToKind.TYPE_LIKE;
				}

				final List<ExpectedTypeInfo> expectedTypeRefs = getExpectedTypeInfosForExpression(parameters, context);

				final CSharpNewExpression newExpression = getNewExpression(expression);

				final List<ExpectedTypeInfo> newExpectedTypeRefs = newExpression != null ? ExpectedTypeVisitor.findExpectedTypeRefs(newExpression) : Collections.<ExpectedTypeInfo>emptyList();

				final CSharpTypeDeclaration contextType = getContextType(expression);
				CSharpCallArgumentListOwner callArgumentListOwner = CSharpReferenceExpressionImplUtil.findCallArgumentListOwner(kind, expression);
				CSharpReferenceExpressionImplUtil.collectResults(new CSharpResolveOptions(kind, null, expression, callArgumentListOwner, true, true), new Processor<ResolveResult>()
				{
					@Override
					@RequiredReadAction
					public boolean process(ResolveResult resolveResult)
					{
						ProgressManager.checkCanceled();

						PsiElement element = resolveResult.getElement();
						if(element == null)
						{
							return true;
						}

						DotNetGenericExtractor extractor = DotNetGenericExtractor.EMPTY;
						if(newExpression != null && CSharpPsiUtilImpl.isTypeLikeElement(element))
						{
							if(element instanceof CSharpTypeDeclaration && ((CSharpTypeDeclaration) element).hasModifier(DotNetModifier.ABSTRACT))
							{
								return true;
							}

							if(!newExpectedTypeRefs.isEmpty())
							{
								for(ExpectedTypeInfo newExpectedTypeRef : newExpectedTypeRefs)
								{
									DotNetTypeResolveResult expectedTypeResult = newExpectedTypeRef.getTypeRef().resolve(expression);

									PsiElement expectedTypeResultElement = expectedTypeResult.getElement();
									if(expectedTypeResult instanceof CSharpLambdaResolveResult)
									{
										expectedTypeResultElement = ((CSharpLambdaResolveResult) expectedTypeResult).getTarget();
									}

									if(element instanceof CSharpTypeDeclaration && expectedTypeResultElement instanceof CSharpTypeDeclaration)
									{
										DotNetGenericParameter[] genericParameters = ((CSharpTypeDeclaration) element).getGenericParameters();

										Map<DotNetGenericParameter, DotNetTypeRef> map = new HashMap<DotNetGenericParameter, DotNetTypeRef>(genericParameters.length);
										resolveGenericParameterValues((CSharpTypeDeclaration) element, (DotNetTypeDeclaration) expectedTypeResultElement, expectedTypeResult.getGenericExtractor(),
												map, expression);

										if(!map.isEmpty())
										{
											extractor = CSharpGenericExtractor.create(map);
										}
									}
									else if(element instanceof CSharpMethodDeclaration && element.isEquivalentTo(expectedTypeResultElement))
									{
										extractor = expectedTypeResult.getGenericExtractor();
									}
								}
							}
						}

						LookupElement lookupElement = CSharpLookupElementBuilder.buildLookupElementWithContextType(element, contextType, extractor, expression);
						if(lookupElement == null)
						{
							return true;
						}

						if(element instanceof DotNetGenericParameter && expression.getParent() instanceof CSharpGenericConstraint && lookupElement instanceof LookupElementBuilder)
						{
							lookupElement = ((LookupElementBuilder) lookupElement).withInsertHandler(new CSharpTailInsertHandler(TailType.COND_EXPR_COLON));
						}

						if(element instanceof CSharpMethodDeclaration && !((CSharpMethodDeclaration) element).isDelegate())
						{
							CSharpMethodDeclaration methodDeclaration = (CSharpMethodDeclaration) element;
							DotNetTypeRef typeOfElement = methodDeclaration.getReturnTypeRef();

							for(ExpectedTypeInfo expectedTypeInfo : expectedTypeRefs)
							{
								if(expectedTypeInfo.getTypeProvider() == element)
								{
									continue;
								}

								if(!CSharpTypeUtil.isInheritable(expectedTypeInfo.getTypeRef(), typeOfElement, expression))
								{
									DotNetTypeResolveResult typeResolveResult = expectedTypeInfo.getTypeRef().resolve(expression);
									if(typeResolveResult instanceof CSharpLambdaResolveResult)
									{
										if(CSharpTypeUtil.isInheritable(expectedTypeInfo.getTypeRef(), new CSharpLambdaTypeRef(methodDeclaration), expression))
										{
											result.consume(buildForMethodReference(methodDeclaration, expression));
											return true;
										}
									}
								}
							}
						}
						result.consume(lookupElement);
						return true;
					}
				});
			}

			@RequiredReadAction
			private CSharpTypeDeclaration getContextType(CSharpReferenceExpression referenceExpression)
			{
				PsiElement qualifier = referenceExpression.getQualifier();
				if(qualifier != null)
				{
					PsiElement element = ((DotNetExpression) qualifier).toTypeRef(true).resolve(referenceExpression).getElement();
					return element instanceof CSharpTypeDeclaration ? (CSharpTypeDeclaration) element : null;
				}
				else
				{
					return PsiTreeUtil.getContextOfType(referenceExpression, CSharpTypeDeclaration.class);
				}
			}
		});

		extend(CompletionType.BASIC, CSharpPatterns.referenceExpression(), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result)
			{
				if(isCorrectPosition(parameters.getPosition()))
				{
					TokenSet set = TokenSet.create(CSharpTokens.AS_KEYWORD, CSharpTokens.IS_KEYWORD);
					CSharpCompletionUtil.tokenSetToLookup(result, set, new NotNullPairFunction<LookupElementBuilder, IElementType, LookupElement>()
					{
						@NotNull
						@Override
						public LookupElement fun(LookupElementBuilder lookupElementBuilder, IElementType iElementType)
						{
							lookupElementBuilder = lookupElementBuilder.withInsertHandler(SpaceInsertHandler.INSTANCE);
							return lookupElementBuilder;
						}
					}, null);
				}
			}

			@RequiredReadAction
			private boolean isCorrectPosition(PsiElement position)
			{
				PsiElement prev = PsiTreeUtil.prevVisibleLeaf(position);
				if(prev == null)
				{
					return false;
				}

				PsiElement expr = PsiTreeUtil.getParentOfType(prev, DotNetExpression.class);
				if(expr != null && expr.getTextRange().getEndOffset() == prev.getTextRange().getEndOffset())
				{
					return true;
				}
				return false;
			}
		});

		extend(CompletionType.BASIC, psiElement(CSharpTokens.IDENTIFIER).withParent(CSharpReferenceExpression.class).withSuperParent(2, CSharpArrayInitializerImpl.class).withSuperParent(3,
				CSharpNewExpressionImpl.class), new CompletionProvider()
		{
			@Override
			@RequiredReadAction
			protected void addCompletions(@NotNull final CompletionParameters parameters, ProcessingContext context, @NotNull final CompletionResultSet result)
			{
				final CSharpReferenceExpressionEx expression = (CSharpReferenceExpressionEx) parameters.getPosition().getParent();

				CSharpArrayInitializerImpl arrayInitializationExpression = PsiTreeUtil.getParentOfType(expression, CSharpArrayInitializerImpl.class);

				assert arrayInitializationExpression != null;
				CSharpArrayInitializerValue[] arrayInitializerValues = arrayInitializationExpression.getValues();
				if(arrayInitializerValues.length != 1 || !(arrayInitializerValues[0] instanceof CSharpArrayInitializerSingleValueImpl) ||
						((CSharpArrayInitializerSingleValueImpl) arrayInitializerValues[0]).getArgumentExpression() != expression)
				{
					return;
				}

				CSharpResolveOptions options = CSharpResolveOptions.build().element(expression).resolveFromParent();
				options.kind(CSharpReferenceExpression.ResolveToKind.FIELD_OR_PROPERTY);
				options.completion(CSharpContextUtil.ContextType.INSTANCE);

				CSharpReferenceExpressionImplUtil.collectResults(options, new Processor<ResolveResult>()
				{
					@Override
					@RequiredReadAction
					public boolean process(ResolveResult resolveResult)
					{
						ProgressManager.checkCanceled();

						PsiElement element = resolveResult.getElement();
						if(element == null)
						{
							return true;
						}
						LookupElementBuilder lookupElementBuilder = CSharpLookupElementBuilder.createLookupElementBuilder(element, DotNetGenericExtractor.EMPTY, expression);
						if(lookupElementBuilder != null)
						{
							lookupElementBuilder = lookupElementBuilder.withTailText(" = ", true);
							lookupElementBuilder = lookupElementBuilder.withInsertHandler(new InsertHandler<LookupElement>()
							{
								@Override
								public void handleInsert(InsertionContext context, LookupElement item)
								{
									if(context.getCompletionChar() != '=')
									{
										Editor editor = context.getEditor();
										int offset = context.getTailOffset();
										TailType.insertChar(editor, offset, ' ');
										TailType.insertChar(editor, offset + 1, '=');
										TailType.insertChar(editor, offset + 2, ' ');
										editor.getCaretModel().moveToOffset(offset + 3);
									}
								}
							});

							if(CSharpPsiUtilImpl.isTypeLikeElement(element))
							{
								result.consume(CSharpTypeLikeLookupElement.create(lookupElementBuilder, DotNetGenericExtractor.EMPTY, expression));
							}
							else
							{
								result.consume(lookupElementBuilder);
							}
						}
						return true;
					}
				});
			}
		});

		extend(CompletionType.BASIC, psiElement(CSharpTokens.IDENTIFIER).withParent(CSharpReferenceExpression.class), new CompletionProvider()
		{

			@RequiredReadAction
			@Override
			protected void addCompletions(@NotNull final CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet)
			{
				CSharpCodeGenerationSettings codeGenerationSettings = CSharpCodeGenerationSettings.getInstance(completionParameters.getPosition().getProject());
				if(!codeGenerationSettings.USE_LANGUAGE_DATA_TYPES)
				{
					return;
				}

				final CSharpReferenceExpression parent = (CSharpReferenceExpression) completionParameters.getPosition().getParent();
				if(parent.getQualifier() != null)
				{
					return;
				}

				CSharpReferenceExpression.ResolveToKind kind = parent.kind();
				if(kind == CSharpReferenceExpression.ResolveToKind.TYPE_LIKE)
				{
					DotNetType type = PsiTreeUtil.getParentOfType(parent, DotNetType.class);
					if(type != null)
					{
						// disable native type completion, due they dont extends System.Exception
						if(type.getParent() instanceof CSharpLocalVariable && type.getParent().getParent() instanceof CSharpCatchStatementImpl)
						{
							return;
						}

						if(type.getParent() instanceof CSharpNewExpression)
						{
							return;
						}
					}
				}

				if(kind == CSharpReferenceExpression.ResolveToKind.TYPE_LIKE ||
						kind == CSharpReferenceExpression.ResolveToKind.EXPRESSION_OR_TYPE_LIKE ||
						kind == CSharpReferenceExpression.ResolveToKind.ANY_MEMBER)
				{
					CSharpCompletionUtil.tokenSetToLookup(completionResultSet, CSharpTokenSets.NATIVE_TYPES, new NotNullPairFunction<LookupElementBuilder, IElementType, LookupElement>()
							{
								@NotNull
								@Override
								public LookupElement fun(LookupElementBuilder lookupElementBuilder, IElementType elementType)
								{
									if(elementType == CSharpTokens.VOID_KEYWORD)
									{
										DotNetType userType = PsiTreeUtil.getParentOfType(parent, DotNetType.class);
										if(userType == null)
										{
											return lookupElementBuilder;
										}
										PsiElement userTypeParent = userType.getParent();
										if(userTypeParent instanceof CSharpTypeOfExpressionImpl)
										{
											return lookupElementBuilder;
										}
										return lookupElementBuilder.withInsertHandler(SpaceInsertHandler.INSTANCE);
									}
									return lookupElementBuilder;
								}
							}, new Condition<IElementType>()
							{
								@Override
								@RequiredReadAction
								public boolean value(IElementType elementType)
								{
									if(elementType == CSharpTokens.EXPLICIT_KEYWORD || elementType == CSharpTokens.IMPLICIT_KEYWORD)
									{
										PsiElement invalidParent = PsiTreeUtil.getParentOfType(parent, DotNetStatement.class, DotNetParameterList
												.class);
										return invalidParent == null;
									}
									else if(elementType == CSharpTokens.VOID_KEYWORD)
									{
										DotNetType userType = PsiTreeUtil.getParentOfType(parent, DotNetType.class);
										if(userType == null)
										{
											return false;
										}
										PsiElement userTypeParent = userType.getParent();
										if(userTypeParent instanceof DotNetLikeMethodDeclaration)
										{
											DotNetLikeMethodDeclaration methodDeclaration = (DotNetLikeMethodDeclaration) userType.getParent();
											return methodDeclaration.getReturnType() == userType;
										}
										else if(userTypeParent instanceof DotNetFieldDeclaration)
										{
											DotNetFieldDeclaration fieldDeclaration = (DotNetFieldDeclaration) userTypeParent;
											if(fieldDeclaration.isConstant() || fieldDeclaration.getInitializer() != null)
											{
												return false;
											}
											return fieldDeclaration.getType() == userType;
										}
										else if(userTypeParent instanceof CSharpTypeOfExpressionImpl)
										{
											return true;
										}
										return false;
									}
									else if(elementType == CSharpTokens.__ARGLIST_KEYWORD)
									{
										DotNetParameter parameter = PsiTreeUtil.getParentOfType(parent, DotNetParameter.class);
										return parameter != null;
									}
									else if(elementType == CSharpSoftTokens.VAR_KEYWORD)
									{
										if(PsiTreeUtil.getParentOfType(parent, DotNetStatement.class) == null)
										{
											return false;
										}
										if(!CSharpModuleUtil.findLanguageVersion(parent).isAtLeast(CSharpLanguageVersion._2_0))
										{
											return false;
										}
									}
									return true;
								}
							}
					);
				}
			}
		});
	}

	@RequiredReadAction
	@Override
	public void fillCompletionVariants(CompletionParameters parameters, CompletionResultSet result)
	{
		super.fillCompletionVariants(parameters, CSharpCompletionSorting.modifyResultSet(parameters, result));
	}

	private static boolean needRemapToAnyResolving(CSharpReferenceExpression.ResolveToKind kind, CSharpReferenceExpression expression)
	{
		if(kind == CSharpReferenceExpression.ResolveToKind.PARAMETER || kind == CSharpReferenceExpression.ResolveToKind.PARAMETER_FROM_PARENT)
		{
			return false;
		}
		if(kind == CSharpReferenceExpression.ResolveToKind.TYPE_LIKE)
		{
			PsiElement parent = expression.getParent();
			if(parent instanceof CSharpUserType)
			{
				PsiElement nextParent = parent.getParent();
				if(nextParent instanceof CSharpIsExpressionImpl ||
						nextParent instanceof CSharpAsExpressionImpl ||
						nextParent instanceof CSharpNewExpression ||
						nextParent instanceof CSharpTypeOfExpressionImpl ||
						nextParent instanceof CSharpSizeOfExpressionImpl ||
						nextParent instanceof CSharpTypeCastExpressionImpl ||
						nextParent instanceof CSharpLocalVariable && nextParent.getParent() instanceof CSharpCatchStatementImpl)
				{
					return false;
				}
			}
		}
		if(kind != CSharpReferenceExpression.ResolveToKind.LABEL &&
				kind != CSharpReferenceExpression.ResolveToKind.QUALIFIED_NAMESPACE &&
				kind != CSharpReferenceExpression.ResolveToKind.FIELD_OR_PROPERTY &&
				kind != CSharpReferenceExpression.ResolveToKind.SOFT_QUALIFIED_NAMESPACE)
		{
			if(PsiTreeUtil.getParentOfType(expression, DotNetStatement.class) != null)
			{
				return true;
			}
		}
		return false;
	}

	@NotNull
	@RequiredReadAction
	private static LookupElement buildForMethodReference(final CSharpMethodDeclaration methodDeclaration, final CSharpReferenceExpressionEx expression)
	{
		LookupElementBuilder builder = LookupElementBuilder.create(methodDeclaration.getName());
		builder = builder.withIcon(AllIcons.Nodes.MethodReference);

		final DotNetTypeRef[] parameterTypes = methodDeclaration.getParameterTypeRefs();

		String genericText = DotNetElementPresentationUtil.formatGenericParameters(methodDeclaration);

		String parameterText = genericText + "(" + StringUtil.join(parameterTypes, new Function<DotNetTypeRef, String>()
		{
			@Override
			@RequiredReadAction
			public String fun(DotNetTypeRef parameter)
			{
				return CSharpTypeRefPresentationUtil.buildShortText(parameter, methodDeclaration);
			}
		}, ", ") + ")";

		if(CSharpMethodImplUtil.isExtensionWrapper(methodDeclaration))
		{
			builder = builder.withItemTextUnderlined(true);
		}
		builder = builder.withTypeText(CSharpTypeRefPresentationUtil.buildShortText(methodDeclaration.getReturnTypeRef(), methodDeclaration), true);
		builder = builder.withTailText(parameterText, true);
		if(DotNetAttributeUtil.hasAttribute(methodDeclaration, DotNetTypes.System.ObsoleteAttribute))
		{
			builder = builder.withStrikeoutness(true);
		}
		builder = builder.withInsertHandler(new InsertHandler<LookupElement>()
		{
			@Override
			@RequiredWriteAction
			public void handleInsert(InsertionContext context, LookupElement item)
			{
				char completionChar = context.getCompletionChar();
				switch(completionChar)
				{
					case ',':
						if(expression != null && expression.getParent() instanceof CSharpCallArgument)
						{
							context.setAddCompletionChar(false);
							TailType.COMMA.processTail(context.getEditor(), context.getTailOffset());
						}
						break;
				}
			}
		});
		CSharpCompletionSorting.force(builder, CSharpCompletionSorting.KindSorter.Type.member);
		return builder;
	}

	@RequiredReadAction
	private static void resolveGenericParameterValues(CSharpTypeDeclaration targetType,
			DotNetTypeDeclaration expectedType,
			DotNetGenericExtractor expectedGenericExtractor,
			Map<DotNetGenericParameter, DotNetTypeRef> map,
			PsiElement scope)
	{
		if(targetType.isEquivalentTo(expectedType))
		{
			for(DotNetGenericParameter genericParameter : targetType.getGenericParameters())
			{
				DotNetTypeRef typeRef = map.get(genericParameter);
				if(typeRef == null)
				{
					map.put(genericParameter, expectedGenericExtractor.extract(genericParameter));
				}
			}
			return;
		}

		DotNetTypeRef[] extendTypeRefs = targetType.getExtendTypeRefs();

		for(DotNetTypeRef extendTypeRef : extendTypeRefs)
		{
			DotNetTypeResolveResult typeResolveResult = extendTypeRef.resolve(scope);

			PsiElement element = typeResolveResult.getElement();
			if(element instanceof DotNetTypeDeclaration && element.isEquivalentTo(expectedType))
			{
				DotNetGenericExtractor genericExtractor = typeResolveResult.getGenericExtractor();
				for(DotNetGenericParameter genericParameter : ((DotNetTypeDeclaration) element).getGenericParameters())
				{
					DotNetTypeRef tempTypeRef = genericExtractor.extract(genericParameter);
					PsiElement tempElement = tempTypeRef == null ? null : tempTypeRef.resolve(scope).getElement();
					if(tempElement != null)
					{
						for(DotNetGenericParameter targetParameter : targetType.getGenericParameters())
						{
							if(targetParameter.isEquivalentTo(tempElement))
							{
								DotNetTypeRef typeRef = map.get(targetParameter);
								if(typeRef == null)
								{
									map.put(targetParameter, expectedGenericExtractor.extract(genericParameter));
								}
							}
						}
					}
				}
			}
		}
	}

	@Nullable
	public static CSharpNewExpression getNewExpression(PsiElement expression)
	{
		PsiElement parent = expression.getParent();
		if(parent instanceof CSharpUserType)
		{
			PsiElement typeParent = parent.getParent();
			if(typeParent instanceof CSharpNewExpression)
			{
				return (CSharpNewExpression) typeParent;
			}
		}

		return null;
	}

	@NotNull
	@RequiredReadAction
	public static List<ExpectedTypeInfo> getExpectedTypeInfosForExpression(CompletionParameters parameters, @Nullable ProcessingContext context)
	{
		PsiElement position = parameters.getPosition();
		if(PsiUtilBase.getElementType(position) != CSharpTokens.IDENTIFIER)
		{
			return Collections.emptyList();
		}

		PsiElement parent = position.getParent();
		if(!(parent instanceof CSharpReferenceExpressionEx))
		{
			return Collections.emptyList();
		}

		List<ExpectedTypeInfo> expectedTypeInfos = context == null ? null : context.get(ExpectedTypeVisitor.EXPECTED_TYPE_INFOS);
		if(expectedTypeInfos != null)
		{
			return expectedTypeInfos;
		}

		expectedTypeInfos = ExpectedTypeVisitor.findExpectedTypeRefs(parent);
		if(context != null)
		{
			context.put(ExpectedTypeVisitor.EXPECTED_TYPE_INFOS, expectedTypeInfos);
		}
		return expectedTypeInfos;
	}
}
