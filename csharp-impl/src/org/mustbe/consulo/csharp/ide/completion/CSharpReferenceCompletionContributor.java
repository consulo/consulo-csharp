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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.codeInsight.completion.CompletionProvider;
import org.mustbe.consulo.csharp.ide.CSharpLookupElementBuilder;
import org.mustbe.consulo.csharp.ide.codeInsight.actions.MethodGenerateUtil;
import org.mustbe.consulo.csharp.ide.codeStyle.CSharpCodeGenerationSettings;
import org.mustbe.consulo.csharp.ide.completion.expected.ExpectedTypeInfo;
import org.mustbe.consulo.csharp.ide.completion.expected.ExpectedTypeVisitor;
import org.mustbe.consulo.csharp.ide.completion.insertHandler.CSharpParenthesesWithSemicolonInsertHandler;
import org.mustbe.consulo.csharp.ide.completion.item.ReplaceableTypeLikeLookupElement;
import org.mustbe.consulo.csharp.ide.completion.util.SpaceInsertHandler;
import org.mustbe.consulo.csharp.lang.psi.*;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpVisibilityUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.resolve.CSharpResolveContextUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.*;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.CSharpResolveOptions;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpArrayTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaResolveResult;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpMethodImplUtil;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.csharp.module.extension.CSharpModuleUtil;
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
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.ide.IconDescriptorUpdaters;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Function;
import com.intellij.util.NotNullPairFunction;
import com.intellij.util.ProcessingContext;
import com.intellij.util.Processor;

/**
 * @author VISTALL
 * @since 23.11.14
 */
public class CSharpReferenceCompletionContributor extends CompletionContributor
{
	public CSharpReferenceCompletionContributor()
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

				boolean any = false;
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

							LookupElementBuilder builder = LookupElementBuilder.create(typeText);
							builder = builder.withIcon(getIconForInnerTypeRef((CSharpArrayTypeRef) typeRef, position));
							// add without {...}
							result.addElement(PrioritizedLookupElement.withPriority(builder, CSharpCompletionUtil.MAX_PRIORITY));

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

							result.addElement(PrioritizedLookupElement.withPriority(builder, CSharpCompletionUtil.MAX_PRIORITY));
						}
						else
						{
							DotNetTypeResolveResult typeResolveResult = typeRef.resolve(position);

							PsiElement element = typeResolveResult.getElement();
							if(element == null)
							{
								continue;
							}

							if(element instanceof CSharpTypeDeclaration && ((CSharpTypeDeclaration) element).isInterface())
							{
								return;
							}

							DotNetGenericExtractor genericExtractor = typeResolveResult.getGenericExtractor();
							CSharpResolveContext resolveContext = CSharpResolveContextUtil.createContext(genericExtractor, position.getResolveScope(), element);

							CSharpElementGroup<CSharpConstructorDeclaration> group = resolveContext.constructorGroup();
							Collection<CSharpConstructorDeclaration> objects = group == null ? Collections.<CSharpConstructorDeclaration>emptyList() : group.getElements();

							if(objects.isEmpty())
							{
								return;
							}

							for(CSharpConstructorDeclaration object : objects)
							{
								if(!CSharpVisibilityUtil.isVisible(object, position))
								{
									continue;
								}

								LookupElementBuilder builder = buildForConstructorAfterNew(object, genericExtractor);
								if(builder != null)
								{
									result.addElement(PrioritizedLookupElement.withPriority(builder, CSharpCompletionUtil.MAX_PRIORITY));
									any = true;
								}
							}
						}
					}
				}

				if(any && parameters.getInvocationCount() == 1)
				{
					result.stopHere();
				}
			}
		});

		extend(CompletionType.BASIC, psiElement(CSharpTokens.IDENTIFIER).withParent(CSharpReferenceExpressionEx.class), new CompletionProvider()
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
				List<ExpectedTypeInfo> expectedTypeRefs = ExpectedTypeVisitor.findExpectedTypeRefs(parent);
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

				result.addElement(PrioritizedLookupElement.withPriority(lookupElementBuilder, CSharpCompletionUtil.NORMAL_PRIORITY));
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

				result.addElement(PrioritizedLookupElement.withPriority(lookupElementBuilder, CSharpCompletionUtil.NORMAL_PRIORITY));
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

		extend(CompletionType.BASIC, psiElement(CSharpTokens.IDENTIFIER).withParent(CSharpReferenceExpressionEx.class), new DumbCompletionProvider()
		{
			@Override
			@RequiredReadAction
			protected void addLookupElements(@NotNull final CompletionParameters parameters, ProcessingContext context, @NotNull final CompletionResultSet result)
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

				final List<ExpectedTypeInfo> expectedTypeRefs = ExpectedTypeVisitor.findExpectedTypeRefs(expression);

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
						LookupElement builder = CSharpLookupElementBuilder.buildLookupElement(element, contextType);
						if(builder == null)
						{
							return true;
						}

						if(element instanceof CSharpMethodDeclaration)
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
											result.consume(buildForMethodReference(methodDeclaration));
											return true;
										}
									}
								}
							}
						}
						result.consume(builder);
						return true;
					}
				});
			}

			@RequiredReadAction
			private CSharpTypeDeclaration getContextType(CSharpReferenceExpression referenceExpression)
			{
				PsiElement qualifier = referenceExpression.getQualifier();
				if(qualifier instanceof DotNetExpression)
				{
					PsiElement element = ((DotNetExpression) qualifier).toTypeRef(true).resolve(referenceExpression).getElement();
					return element instanceof CSharpTypeDeclaration ? (CSharpTypeDeclaration) element : null;
				}
				else
				{
					return PsiTreeUtil.getParentOfType(referenceExpression, CSharpTypeDeclaration.class);
				}
			}
		});

		extend(CompletionType.BASIC, psiElement(CSharpTokens.IDENTIFIER).withParent(CSharpReferenceExpression.class).withSuperParent(2, CSharpArrayInitializerImpl.class).withSuperParent(3,
				CSharpNewExpressionImpl.class), new CompletionProvider()
		{
			@Override
			@RequiredReadAction
			protected void addCompletions(@NotNull final CompletionParameters parameters, ProcessingContext context, @NotNull final CompletionResultSet result)
			{
				CSharpReferenceExpressionEx expression = (CSharpReferenceExpressionEx) parameters.getPosition().getParent();

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
						LookupElementBuilder lookupElementBuilder = CSharpLookupElementBuilder.createLookupElementBuilder(element);
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
								result.consume(new ReplaceableTypeLikeLookupElement(lookupElementBuilder));
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

				if(parent.kind() == CSharpReferenceExpression.ResolveToKind.TYPE_LIKE ||
						parent.kind() == CSharpReferenceExpression.ResolveToKind.EXPRESSION_OR_TYPE_LIKE ||
						parent.kind() == CSharpReferenceExpression.ResolveToKind.ANY_MEMBER)
				{
					CSharpCompletionUtil.tokenSetToLookup(completionResultSet, CSharpTokenSets.NATIVE_TYPES, new NotNullPairFunction<LookupElementBuilder, IElementType, LookupElement>()
							{
								@NotNull
								@Override
								public LookupElement fun(LookupElementBuilder lookupElementBuilder, IElementType elementType)
								{
									if(elementType == CSharpTokens.VOID_KEYWORD)
									{
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
										return false;
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
				PsiElement parent1 = parent.getParent();
				if(parent1 instanceof CSharpIsExpressionImpl || parent1 instanceof CSharpAsExpressionImpl || parent1 instanceof CSharpTypeCastExpressionImpl)
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
	private static Icon getIconForInnerTypeRef(@NotNull CSharpArrayTypeRef typeRef, @NotNull PsiElement scope)
	{
		DotNetTypeRef innerTypeRef = typeRef.getInnerTypeRef();
		if(!(innerTypeRef instanceof CSharpArrayTypeRef))
		{
			PsiElement element = innerTypeRef.resolve(scope).getElement();
			if(element != null)
			{
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

	@NotNull
	@RequiredReadAction
	private static LookupElement buildForMethodReference(final CSharpMethodDeclaration methodDeclaration)
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
		return PrioritizedLookupElement.withExplicitProximity(builder, 1);
	}

	@Nullable
	@RequiredReadAction
	private static LookupElementBuilder buildForConstructorAfterNew(final CSharpConstructorDeclaration declaration, final DotNetGenericExtractor extractor)
	{
		PsiElement parent = declaration.getParent();

		if(!(parent instanceof DotNetNamedElement))
		{
			return null;
		}

		String lookupString = ((DotNetNamedElement) parent).getName();
		if(parent instanceof DotNetGenericParameterListOwner)
		{
			DotNetGenericParameter[] genericParameters = ((DotNetGenericParameterListOwner) parent).getGenericParameters();
			if(genericParameters.length > 0)
			{
				lookupString += "<" + StringUtil.join(genericParameters, new Function<DotNetGenericParameter, String>()
				{
					@Override
					@RequiredReadAction
					public String fun(DotNetGenericParameter parameter)
					{
						DotNetTypeRef extract = extractor.extract(parameter);
						if(extract != null)
						{
							return CSharpTypeRefPresentationUtil.buildShortText(extract, declaration);
						}
						return parameter.getName();
					}
				}, ", ") + ">";
			}
		}

		if(lookupString == null)
		{
			return null;
		}

		DotNetTypeRef[] parameters = declaration.getParameterTypeRefs();

		String parameterText = "(" + StringUtil.join(parameters, new Function<DotNetTypeRef, String>()
		{
			@Override
			@RequiredReadAction
			public String fun(DotNetTypeRef parameter)
			{
				return CSharpTypeRefPresentationUtil.buildShortText(parameter, declaration);
			}
		}, ", ") + ")";

		LookupElementBuilder builder = LookupElementBuilder.create(declaration, lookupString);
		builder = builder.withIcon(IconDescriptorUpdaters.getIcon(parent, Iconable.ICON_FLAG_VISIBILITY));
		builder = builder.withTailText(parameterText, true);
		builder = builder.withInsertHandler(new CSharpParenthesesWithSemicolonInsertHandler(declaration));
		return builder;
	}
}
