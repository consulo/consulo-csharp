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

import static com.intellij.patterns.StandardPatterns.psiElement;

import gnu.trove.THashSet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Function;
import com.intellij.util.ProcessingContext;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;
import consulo.ui.RequiredUIAccess;
import consulo.annotations.RequiredReadAction;
import consulo.annotations.RequiredWriteAction;
import consulo.codeInsight.completion.CompletionProvider;
import consulo.csharp.ide.CSharpLookupElementBuilder;
import consulo.csharp.ide.codeStyle.CSharpCodeGenerationSettings;
import consulo.csharp.ide.completion.expected.ExpectedTypeInfo;
import consulo.csharp.ide.completion.insertHandler.CSharpTailInsertHandler;
import consulo.csharp.ide.completion.item.CSharpTypeLikeLookupElement;
import consulo.csharp.ide.completion.patterns.CSharpPatterns;
import consulo.csharp.ide.completion.util.SpaceInsertHandler;
import consulo.csharp.ide.completion.weigher.CSharpInheritCompletionWeighter;
import consulo.csharp.lang.psi.*;
import consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import consulo.csharp.lang.psi.impl.source.*;
import consulo.csharp.lang.psi.impl.source.resolve.CSharpResolveOptions;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpGenericExtractor;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaResolveResult;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaTypeRef;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpRefTypeRef;
import consulo.csharp.lang.psi.impl.source.resolve.util.CSharpMethodImplUtil;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.csharp.module.extension.CSharpModuleUtil;
import consulo.dotnet.DotNetRunUtil;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.ide.DotNetElementPresentationUtil;
import consulo.dotnet.psi.*;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.resolve.DotNetTypeResolveResult;
import consulo.ide.IconDescriptorUpdaters;
import consulo.ui.image.Image;

/**
 * @author VISTALL
 * @since 23.11.14
 */
class CSharpExpressionCompletionContributor
{
	private static final TokenSet ourExpressionLiterals = TokenSet.create(CSharpTokens.NULL_LITERAL, CSharpTokens.FALSE_KEYWORD, CSharpTokens.TRUE_KEYWORD, CSharpTokens.DEFAULT_KEYWORD, CSharpTokens
			.TYPEOF_KEYWORD, CSharpTokens.SIZEOF_KEYWORD, CSharpTokens.THIS_KEYWORD, CSharpTokens.BASE_KEYWORD, CSharpSoftTokens.AWAIT_KEYWORD, CSharpTokens.NEW_KEYWORD, CSharpTokens
			.__MAKEREF_KEYWORD, CSharpTokens.__REFTYPE_KEYWORD, CSharpTokens.__REFVALUE_KEYWORD, CSharpSoftTokens.NAMEOF_KEYWORD);

	static void extend(CompletionContributor contributor)
	{
		contributor.extend(CompletionType.BASIC, CSharpPatterns.expressionStart(), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			public void addCompletions(@Nonnull final CompletionParameters parameters, ProcessingContext context, @Nonnull CompletionResultSet result)
			{
				final CSharpReferenceExpressionEx parent = (CSharpReferenceExpressionEx) parameters.getPosition().getParent();
				if(parent.getQualifier() == null && (parent.kind() == CSharpReferenceExpression.ResolveToKind.ANY_MEMBER || parent.kind() == CSharpReferenceExpression.ResolveToKind
						.EXPRESSION_OR_TYPE_LIKE))
				{
					CSharpCompletionUtil.tokenSetToLookup(result, ourExpressionLiterals, (t, elementType) ->
					{
						if(elementType == CSharpTokens.DEFAULT_KEYWORD || elementType == CSharpTokens.TYPEOF_KEYWORD || elementType == CSharpSoftTokens.NAMEOF_KEYWORD || elementType == CSharpTokens
								.__MAKEREF_KEYWORD || elementType == CSharpTokens.__REFTYPE_KEYWORD || elementType == CSharpTokens.__REFVALUE_KEYWORD || elementType == CSharpTokens.SIZEOF_KEYWORD)
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
								@RequiredUIAccess
								public void handleInsert(InsertionContext context1, LookupElement item)
								{
									CSharpSimpleLikeMethodAsElement methodAsElement = PsiTreeUtil.getParentOfType(parameters.getOriginalPosition(), CSharpSimpleLikeMethodAsElement.class);
									if(methodAsElement != null && !methodAsElement.hasModifier(CSharpModifier.ASYNC))
									{
										DotNetModifierList modifierList = methodAsElement.getModifierList();
										assert modifierList != null;
										modifierList.addModifier(CSharpModifier.ASYNC);
										PsiDocumentManager.getInstance(context1.getProject()).doPostponedOperationsAndUnblockDocument(context1.getDocument());
									}

									SpaceInsertHandler.INSTANCE.handleInsert(context1, item);
								}
							});
						}
						return t;
					}, new Condition<IElementType>()
					{
						@Override
						@RequiredReadAction
						public boolean value(IElementType elementType)
						{
							if(elementType == CSharpTokens.NEW_KEYWORD)
							{
								return context.getSharedContext().get(CSharpSuggestInstanceCompletionContributor.ourDefaultNewKeyword) == null;
							}
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
					});
				}
			}
		});

		contributor.extend(CompletionType.BASIC, CSharpPatterns.expressionStart(), new CompletionProvider()
		{
			@Override
			@RequiredReadAction
			public void addCompletions(@Nonnull CompletionParameters parameters, ProcessingContext context, @Nonnull CompletionResultSet result)
			{
				List<ExpectedTypeInfo> expectedTypeInfos = CSharpInheritCompletionWeighter.getExpectedTypeInfosForExpression(parameters, context);

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

						CSharpCompletionUtil.elementToLookup(result, elementType, (lookupElementBuilder, iElementType) ->
						{
							lookupElementBuilder = lookupElementBuilder.withInsertHandler(SpaceInsertHandler.INSTANCE);
							return lookupElementBuilder;
						}, null);
					}
				}
			}
		});

		contributor.extend(CompletionType.BASIC, CSharpPatterns.expressionStart().withSuperParent(2, CSharpCallArgument.class), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			public void addCompletions(@Nonnull CompletionParameters parameters, ProcessingContext context, @Nonnull CompletionResultSet result)
			{
				CSharpReferenceExpression referenceExpression = (CSharpReferenceExpression) parameters.getPosition().getParent();
				if(referenceExpression.getQualifier() != null)
				{
					return;
				}

				CSharpCallArgument callArgument = (CSharpCallArgument) referenceExpression.getParent();
				if(callArgument instanceof CSharpNamedCallArgument)
				{
					return;
				}

				CSharpCallArgumentListOwner argumentListOwner = PsiTreeUtil.getParentOfType(referenceExpression, CSharpCallArgumentListOwner.class);

				assert argumentListOwner != null;
				ResolveResult[] resolveResults = argumentListOwner.multiResolve(false);

				boolean visitedNotNamed = false;
				Set<String> alreadyDeclared = new THashSet<>(5);
				CSharpCallArgument[] callArguments = argumentListOwner.getCallArguments();
				for(CSharpCallArgument c : callArguments)
				{
					if(c == callArgument)
					{
						continue;
					}
					if(c instanceof CSharpNamedCallArgument)
					{
						alreadyDeclared.add(((CSharpNamedCallArgument) c).getName());
					}
					else
					{
						visitedNotNamed = true;
					}
				}
				int thisCallArgumentPosition = visitedNotNamed ? ArrayUtil.indexOf(callArguments, callArgument) : -1;

				Set<String> wantToCompleteParameters = new THashSet<>();
				for(ResolveResult resolveResult : resolveResults)
				{
					PsiElement element = resolveResult.getElement();
					if(element instanceof CSharpSimpleLikeMethodAsElement)
					{
						CSharpSimpleParameterInfo[] parameterInfos = ((CSharpSimpleLikeMethodAsElement) element).getParameterInfos();

						if(thisCallArgumentPosition != -1)
						{
							if(parameterInfos.length > thisCallArgumentPosition)
							{
								for(int i = thisCallArgumentPosition; i < parameterInfos.length; i++)
								{
									CSharpSimpleParameterInfo parameterInfo = parameterInfos[i];
									ContainerUtil.addIfNotNull(wantToCompleteParameters, parameterInfo.getName());
								}
							}
						}
						else
						{
							for(CSharpSimpleParameterInfo parameterInfo : parameterInfos)
							{
								ContainerUtil.addIfNotNull(wantToCompleteParameters, parameterInfo.getName());
							}
						}
					}
				}

				wantToCompleteParameters.removeAll(alreadyDeclared);

				for(String wantToCompleteParameter : wantToCompleteParameters)
				{
					LookupElementBuilder builder = LookupElementBuilder.create(wantToCompleteParameter + ": ");
					builder = builder.withTailText("<expression>", true);
					builder = builder.withIcon(IconLoader.getTransparentIcon(AllIcons.Nodes.Parameter));

					CSharpCompletionSorting.force(builder, CSharpCompletionSorting.KindSorter.Type.parameterInCall);
					result.consume(builder);
				}
			}
		});

		contributor.extend(CompletionType.BASIC, CSharpPatterns.expressionStart(), new CompletionProvider()
		{
			@Override
			@RequiredReadAction
			public void addCompletions(@Nonnull final CompletionParameters parameters, ProcessingContext context, @Nonnull final CompletionResultSet result)
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

				final CSharpTypeDeclaration contextType = getContextType(expression);

				final List<ExpectedTypeInfo> expectedTypeRefs = CSharpInheritCompletionWeighter.getExpectedTypeInfosForExpression(parameters, context);
				for(ExpectedTypeInfo expectedTypeRef : expectedTypeRefs)
				{
					PsiElement element = expectedTypeRef.getTypeRef().resolve().getElement();
					if(element instanceof CSharpTypeDeclaration && ((CSharpTypeDeclaration) element).isEnum() && !element.isEquivalentTo(contextType))
					{
						DotNetNamedElement[] members = ((CSharpTypeDeclaration) element).getMembers();
						for(DotNetNamedElement member : members)
						{
							if(member instanceof CSharpEnumConstantDeclaration)
							{
								String name = member.getName();
								if(name == null)
								{
									continue;
								}
								LookupElementBuilder builder = LookupElementBuilder.create(member, ((CSharpTypeDeclaration) element).getName() + "." + name);
								builder = builder.withIcon(IconDescriptorUpdaters.getIcon(member, 0));
								builder = builder.withTypeText(((CSharpTypeDeclaration) element).getPresentableParentQName());
								builder = builder.withLookupString(name);
								CSharpCompletionSorting.force(builder, CSharpCompletionSorting.KindSorter.Type.constants);
								result.addElement(builder);
							}
						}
					}
				}

				final CSharpNewExpression newExpression = getNewExpression(expression);

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

							if(!expectedTypeRefs.isEmpty())
							{
								for(ExpectedTypeInfo newExpectedTypeRef : expectedTypeRefs)
								{
									DotNetTypeResolveResult expectedTypeResult = newExpectedTypeRef.getTypeRef().resolve();

									PsiElement expectedTypeResultElement = expectedTypeResult.getElement();
									if(expectedTypeResult instanceof CSharpLambdaResolveResult)
									{
										expectedTypeResultElement = ((CSharpLambdaResolveResult) expectedTypeResult).getTarget();
									}

									if(element instanceof CSharpTypeDeclaration && expectedTypeResultElement instanceof CSharpTypeDeclaration)
									{
										DotNetGenericParameter[] genericParameters = ((CSharpTypeDeclaration) element).getGenericParameters();

										Map<DotNetGenericParameter, DotNetTypeRef> map = new HashMap<>(genericParameters.length);
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
									DotNetTypeResolveResult typeResolveResult = expectedTypeInfo.getTypeRef().resolve();
									if(typeResolveResult instanceof CSharpLambdaResolveResult)
									{
										if(CSharpTypeUtil.isInheritable(expectedTypeInfo.getTypeRef(), new CSharpLambdaTypeRef(methodDeclaration), expression))
										{
											result.consume(buildForMethodReference(methodDeclaration, contextType, expression));
											return true;
										}
									}
								}
							}
						}

						if(element instanceof CSharpIndexMethodDeclaration)
						{
							lookupElement = PrioritizedLookupElement.withPriority(lookupElement, 1);
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
					PsiElement element = ((DotNetExpression) qualifier).toTypeRef(true).resolve().getElement();
					return element instanceof CSharpTypeDeclaration ? (CSharpTypeDeclaration) element : null;
				}
				else
				{
					return PsiTreeUtil.getContextOfType(referenceExpression, CSharpTypeDeclaration.class);
				}
			}
		});

		contributor.extend(CompletionType.BASIC, CSharpPatterns.expressionStart(), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			public void addCompletions(@Nonnull CompletionParameters parameters, ProcessingContext context, @Nonnull CompletionResultSet result)
			{
				if(isCorrectPosition(parameters.getPosition()))
				{
					TokenSet set = TokenSet.create(CSharpTokens.AS_KEYWORD, CSharpTokens.IS_KEYWORD);
					CSharpCompletionUtil.tokenSetToLookup(result, set, (lookupElementBuilder, iElementType) ->
					{
						lookupElementBuilder = lookupElementBuilder.withInsertHandler(SpaceInsertHandler.INSTANCE);
						return lookupElementBuilder;
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

		contributor.extend(CompletionType.BASIC, psiElement(CSharpTokens.IDENTIFIER).withParent(CSharpReferenceExpression.class).withSuperParent(2, CSharpArrayInitializerImpl.class).withSuperParent
				(3, CSharpNewExpressionImpl.class), new CompletionProvider()
		{
			@Override
			@RequiredReadAction
			public void addCompletions(@Nonnull final CompletionParameters parameters, ProcessingContext context, @Nonnull final CompletionResultSet result)
			{
				final CSharpReferenceExpressionEx expression = (CSharpReferenceExpressionEx) parameters.getPosition().getParent();

				CSharpArrayInitializerImpl arrayInitializationExpression = PsiTreeUtil.getParentOfType(expression, CSharpArrayInitializerImpl.class);

				assert arrayInitializationExpression != null;
				CSharpArrayInitializerValue[] arrayInitializerValues = arrayInitializationExpression.getValues();
				if(arrayInitializerValues.length != 1 || !(arrayInitializerValues[0] instanceof CSharpArrayInitializerSingleValueImpl) || ((CSharpArrayInitializerSingleValueImpl)
						arrayInitializerValues[0]).getArgumentExpression() != expression)
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
							lookupElementBuilder = lookupElementBuilder.withInsertHandler((context12, item) ->
							{
								if(context12.getCompletionChar() != '=')
								{
									Editor editor = context12.getEditor();
									int offset = context12.getTailOffset();
									TailType.insertChar(editor, offset, ' ');
									TailType.insertChar(editor, offset + 1, '=');
									TailType.insertChar(editor, offset + 2, ' ');
									editor.getCaretModel().moveToOffset(offset + 3);
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

		contributor.extend(CompletionType.BASIC, CSharpPatterns.expressionStart(), new CompletionProvider()
		{

			@RequiredReadAction
			@Override
			public void addCompletions(@Nonnull final CompletionParameters completionParameters, ProcessingContext processingContext, @Nonnull CompletionResultSet completionResultSet)
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

				if(kind == CSharpReferenceExpression.ResolveToKind.TYPE_LIKE || kind == CSharpReferenceExpression.ResolveToKind.EXPRESSION_OR_TYPE_LIKE || kind == CSharpReferenceExpression
						.ResolveToKind.ANY_MEMBER)
				{
					CSharpCompletionUtil.tokenSetToLookup(completionResultSet, CSharpTokenSets.NATIVE_TYPES, (lookupElementBuilder, elementType) ->
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
					}, new Condition<IElementType>()
					{
						@Override
						@RequiredReadAction
						public boolean value(IElementType elementType)
						{
							if(elementType == CSharpTokens.EXPLICIT_KEYWORD || elementType == CSharpTokens.IMPLICIT_KEYWORD)
							{
								PsiElement invalidParent = PsiTreeUtil.getParentOfType(parent, DotNetStatement.class, DotNetParameterList.class);
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
					});
				}
			}
		});
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
				if(nextParent instanceof CSharpIsExpressionImpl || nextParent instanceof CSharpAsExpressionImpl || nextParent instanceof CSharpNewExpression || nextParent instanceof
						CSharpTypeOfExpressionImpl || nextParent instanceof CSharpSizeOfExpressionImpl || nextParent instanceof CSharpTypeCastExpressionImpl || nextParent instanceof
						CSharpLocalVariable && nextParent.getParent() instanceof CSharpCatchStatementImpl)
				{
					return false;
				}
			}
		}
		if(kind != CSharpReferenceExpression.ResolveToKind.LABEL && kind != CSharpReferenceExpression.ResolveToKind.QUALIFIED_NAMESPACE && kind != CSharpReferenceExpression.ResolveToKind
				.FIELD_OR_PROPERTY && kind != CSharpReferenceExpression.ResolveToKind.SOFT_QUALIFIED_NAMESPACE)
		{
			if(PsiTreeUtil.getParentOfType(expression, DotNetStatement.class) != null)
			{
				return true;
			}
		}
		return false;
	}

	@Nonnull
	@RequiredReadAction
	private static LookupElement buildForMethodReference(final CSharpMethodDeclaration methodDeclaration, CSharpTypeDeclaration contextType, final CSharpReferenceExpressionEx expression)
	{
		LookupElementBuilder builder = LookupElementBuilder.create(methodDeclaration.getName());
		builder = builder.withIcon((Image) AllIcons.Nodes.MethodReference);

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

		if(contextType != null && contextType.isEquivalentTo(methodDeclaration.getParent()))
		{
			builder = builder.bold();
		}

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
			DotNetTypeResolveResult typeResolveResult = extendTypeRef.resolve();

			PsiElement element = typeResolveResult.getElement();
			if(element instanceof DotNetTypeDeclaration && element.isEquivalentTo(expectedType))
			{
				DotNetGenericExtractor genericExtractor = typeResolveResult.getGenericExtractor();
				for(DotNetGenericParameter genericParameter : ((DotNetTypeDeclaration) element).getGenericParameters())
				{
					DotNetTypeRef tempTypeRef = genericExtractor.extract(genericParameter);
					PsiElement tempElement = tempTypeRef == null ? null : tempTypeRef.resolve().getElement();
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
}
