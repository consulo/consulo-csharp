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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.ide.CSharpLookupElementBuilder;
import org.mustbe.consulo.csharp.ide.codeInsight.actions.AddUsingAction;
import org.mustbe.consulo.csharp.ide.codeInsight.actions.MethodGenerateUtil;
import org.mustbe.consulo.csharp.ide.completion.expected.ExpectedTypeInfo;
import org.mustbe.consulo.csharp.ide.completion.expected.ExpectedTypeRefProvider;
import org.mustbe.consulo.csharp.ide.completion.util.LtGtInsertHandler;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpressionEx;
import org.mustbe.consulo.csharp.lang.psi.CSharpSimpleParameterInfo;
import org.mustbe.consulo.csharp.lang.psi.CSharpSoftTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokenSets;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpUsingList;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.MsilToCSharpUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpArrayInitializationExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpNewExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpReferenceExpressionImplUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.AbstractScopeProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.MemberResolveScopeProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaResolveResult;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.index.TypeIndex;
import org.mustbe.consulo.csharp.lang.psi.resolve.MemberByNameSelector;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.csharp.module.extension.CSharpModuleUtil;
import org.mustbe.consulo.dotnet.libraryAnalyzer.NamespaceReference;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetParameterList;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import org.mustbe.consulo.dotnet.psi.DotNetStatement;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetNamespaceAsElement;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.CompletionUtilCore;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.ide.IconDescriptorUpdaters;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Couple;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.ResolveState;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Function;
import com.intellij.util.ProcessingContext;
import com.intellij.util.Processor;
import lombok.val;

/**
 * @author VISTALL
 * @since 23.11.14
 */
public class CSharpReferenceCompletionContributor extends CompletionContributor
{
	public CSharpReferenceCompletionContributor()
	{
		extend(CompletionType.BASIC, StandardPatterns.psiElement(CSharpTokens.IDENTIFIER).withParent(CSharpReferenceExpressionEx.class),
				new CompletionProvider<CompletionParameters>()
		{
			@Override
			protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result)
			{
				val parent = (CSharpReferenceExpressionEx) parameters.getPosition().getParent();
				if(parent.getQualifier() != null || parent.kind() != CSharpReferenceExpression.ResolveToKind.ANY_MEMBER)
				{
					return;
				}

				List<ExpectedTypeInfo> expectedTypeRefs = ExpectedTypeRefProvider.findExpectedTypeRefs(parent);
				for(ExpectedTypeInfo expectedTypeRef : expectedTypeRefs)
				{
					DotNetTypeRef typeRef = expectedTypeRef.getTypeRef();
					DotNetTypeResolveResult typeResolveResult = typeRef.resolve(parent);
					if(typeResolveResult instanceof CSharpLambdaResolveResult)
					{
						CSharpSimpleParameterInfo[] parameterInfos = ((CSharpLambdaResolveResult) typeResolveResult).getParameterInfos();

						StringBuilder builder = new StringBuilder();
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

						result.addElement(PrioritizedLookupElement.withPriority(lookupElementBuilder, CSharpCompletionUtil.EXPR_REF_PRIORITY));

						DotNetTypeRef returnTypeRef = ((CSharpLambdaResolveResult) typeResolveResult).getReturnTypeRef();
						String defaultValueForType = MethodGenerateUtil.getDefaultValueForType(returnTypeRef, parent);
						builder.append("{");
						if(defaultValueForType != null)
						{
							builder.append("return ").append(defaultValueForType).append(";");
						}
						builder.append("}");

						lookupElementBuilder = LookupElementBuilder.create(builder.toString());
						lookupElementBuilder = lookupElementBuilder.withInsertHandler(new InsertHandler<LookupElement>()
						{
							@Override
							public void handleInsert(InsertionContext context, LookupElement item)
							{
								context.getEditor().getCaretModel().moveToOffset(context.getEditor().getCaretModel().getOffset() - 1);
							}
						});

						result.addElement(PrioritizedLookupElement.withPriority(lookupElementBuilder, CSharpCompletionUtil.EXPR_REF_PRIORITY));
					}
				}
			}
		});

		extend(CompletionType.BASIC, StandardPatterns.psiElement(CSharpTokens.IDENTIFIER).withParent(CSharpReferenceExpressionEx.class),
				new CompletionProvider<CompletionParameters>()
		{
			@Override
			protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result)
			{
				CSharpReferenceExpressionEx expression = (CSharpReferenceExpressionEx) parameters.getPosition().getParent();
				CSharpReferenceExpression.ResolveToKind kind = expression.kind();
				if(kind != CSharpReferenceExpression.ResolveToKind.LABEL &&
						kind != CSharpReferenceExpression.ResolveToKind.QUALIFIED_NAMESPACE &&
						kind != CSharpReferenceExpression.ResolveToKind.FIELD_OR_PROPERTY &&
						kind != CSharpReferenceExpression.ResolveToKind.SOFT_QUALIFIED_NAMESPACE)
				{
					kind = CSharpReferenceExpression.ResolveToKind.ANY_MEMBER;
				}
				ResolveResult[] psiElements = CSharpReferenceExpressionImplUtil.collectResults(kind, null, expression, null, true, true);
				List<LookupElement> lookupElements = CSharpLookupElementBuilder.getInstance(expression.getProject()).buildToLookupElements
						(expression, psiElements);

				List<ExpectedTypeInfo> expectedTypeRefs = ExpectedTypeRefProvider.findExpectedTypeRefs(expression);
				if(!expectedTypeRefs.isEmpty())
				{
					ListIterator<LookupElement> iterator = lookupElements.listIterator();
					while(iterator.hasNext())
					{
						LookupElement next = iterator.next();

						PsiElement psiElement = next.getPsiElement();
						if(psiElement == null || psiElement instanceof DotNetNamespaceAsElement || psiElement instanceof DotNetTypeDeclaration)
						{
							iterator.set(PrioritizedLookupElement.withPriority(next, -0.5));
							continue;
						}

						DotNetTypeRef typeOfElement;
						if(psiElement instanceof DotNetLikeMethodDeclaration)
						{
							typeOfElement = ((DotNetLikeMethodDeclaration) psiElement).getReturnTypeRef();
						}
						else
						{
							typeOfElement = CSharpReferenceExpressionImplUtil.toTypeRef(psiElement);
						}

						for(ExpectedTypeInfo expectedTypeInfo : expectedTypeRefs)
						{
							if(expectedTypeInfo.getTypeProvider() == psiElement)
							{
								continue;
							}

							if(CSharpTypeUtil.isInheritable(expectedTypeInfo.getTypeRef(), typeOfElement, expression))
							{
								iterator.set(PrioritizedLookupElement.withPriority(next, CSharpCompletionUtil.EXPR_REF_PRIORITY));
							}
						}
					}
				}
				result.addAllElements(lookupElements);
			}
		}

		);

		extend(CompletionType.BASIC, StandardPatterns.psiElement(CSharpTokens.IDENTIFIER).withParent(CSharpReferenceExpression.class)
				.withSuperParent(2, CSharpArrayInitializationExpressionImpl.class).withSuperParent(3, CSharpNewExpressionImpl.class),
				new CompletionProvider<CompletionParameters>()
		{
			@Override
			protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result)
			{
				CSharpReferenceExpressionEx expression = (CSharpReferenceExpressionEx) parameters.getPosition().getParent();

				CSharpArrayInitializationExpressionImpl arrayInitializationExpression = PsiTreeUtil.getParentOfType(expression,
						CSharpArrayInitializationExpressionImpl.class);

				assert arrayInitializationExpression != null;
				DotNetExpression[] expressions = arrayInitializationExpression.getExpressions();
				if(expressions.length != 1 || expressions[0] != expression)
				{
					return;
				}
				ResolveResult[] resolveResults = CSharpReferenceExpressionImplUtil.collectResults(CSharpReferenceExpression.ResolveToKind
						.FIELD_OR_PROPERTY, null, expression, null, true, true);

				List<LookupElement> lookupElements = CSharpLookupElementBuilder.getInstance(expression.getProject()).buildToLookupElements
						(expression, resolveResults);

				for(LookupElement lookupElement : lookupElements)
				{
					if(lookupElement instanceof LookupElementBuilder)
					{
						lookupElement = ((LookupElementBuilder) lookupElement).withTailText(" = ", true);
						lookupElement = ((LookupElementBuilder) lookupElement).withInsertHandler(new InsertHandler<LookupElement>()
						{
							@Override
							public void handleInsert(InsertionContext context, LookupElement item)
							{
								int offset = context.getEditor().getCaretModel().getOffset();
								context.getDocument().insertString(offset, " = ");
								context.getEditor().getCaretModel().moveToOffset(offset + 3);
							}
						});
					}
					result.addElement(lookupElement);
				}
			}
		});

		extend(CompletionType.BASIC, StandardPatterns.psiElement(CSharpTokens.IDENTIFIER).withParent(CSharpReferenceExpression.class),
				new CompletionProvider<CompletionParameters>()
		{

			@Override
			protected void addCompletions(@NotNull final CompletionParameters completionParameters,
					ProcessingContext processingContext,
					@NotNull CompletionResultSet completionResultSet)
			{
				val parent = (CSharpReferenceExpression) completionParameters.getPosition().getParent();

				if(parent.kind() == CSharpReferenceExpression.ResolveToKind.TYPE_LIKE || parent.kind() == CSharpReferenceExpression.ResolveToKind
						.ANY_MEMBER)
				{
					CSharpCompletionUtil.tokenSetToLookup(completionResultSet, CSharpTokenSets.NATIVE_TYPES, null, new Condition<IElementType>()
					{
						@Override
						public boolean value(IElementType elementType)
						{
							if(elementType == CSharpTokens.EXPLICIT_KEYWORD || elementType == CSharpTokens.IMPLICIT_KEYWORD)
							{
								PsiElement invalidParent = PsiTreeUtil.getParentOfType(parent, DotNetStatement.class, DotNetParameterList
										.class);
								return invalidParent == null;
							}
							if(elementType == CSharpSoftTokens.VAR_KEYWORD)
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

		extend(CompletionType.BASIC, StandardPatterns.psiElement(CSharpTokens.IDENTIFIER).withParent(CSharpReferenceExpression.class),
				new CompletionProvider<CompletionParameters>()
		{

			@Override
			protected void addCompletions(@NotNull final CompletionParameters completionParameters,
					ProcessingContext processingContext,
					@NotNull CompletionResultSet completionResultSet)
			{
				val parent = (CSharpReferenceExpression) completionParameters.getPosition().getParent();

				if(parent.kind() == CSharpReferenceExpression.ResolveToKind.TYPE_LIKE || parent.kind() == CSharpReferenceExpression.ResolveToKind
						.ANY_MEMBER)
				{
					val referenceName = parent.getReferenceName().replace(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED, "");

					if(StringUtil.isEmpty(referenceName))
					{
						return;
					}

					val names = new ArrayList<String>();
					TypeIndex.getInstance().processAllKeys(parent.getProject(), new Processor<String>()
					{
						@Override
						public boolean process(String s)
						{
							if(s.startsWith(referenceName))
							{
								names.add(s);
							}
							return true;
						}
					});

					List<DotNetTypeDeclaration> typeDeclarations = new ArrayList<DotNetTypeDeclaration>(names.size());
					for(String name : names)
					{
						Collection<DotNetTypeDeclaration> temp = TypeIndex.getInstance().get(name, parent.getProject(), parent.getResolveScope());
						typeDeclarations.addAll(temp);
					}
					if(typeDeclarations.isEmpty())
					{
						return;
					}

					for(DotNetTypeDeclaration dotNetTypeDeclaration : typeDeclarations)
					{
						DotNetQualifiedElement wrap = (DotNetQualifiedElement) MsilToCSharpUtil.wrap(dotNetTypeDeclaration);

						boolean insideUsingList = PsiTreeUtil.getParentOfType(parent, CSharpUsingList.class) != null;

						String presentationText = wrap.getName();

						if(!insideUsingList && isAlreadyResolved(wrap, parent))
						{
							continue;
						}

						int genericCount = 0;
						if(wrap instanceof DotNetGenericParameterListOwner)
						{
							DotNetGenericParameter[] genericParameters = ((DotNetGenericParameterListOwner) wrap).getGenericParameters();
							if((genericCount = genericParameters.length) > 0)
							{
								presentationText += "<" + StringUtil.join(genericParameters, new Function<DotNetGenericParameter, String>()
								{
									@Override
									public String fun(DotNetGenericParameter parameter)
									{
										return parameter.getName();
									}
								}, ", ");
								presentationText += ">";
							}
						}

						String lookupString = insideUsingList ? wrap.getPresentableQName() : wrap.getName();
						if(lookupString == null || presentationText == null)
						{
							continue;
						}
						LookupElementBuilder builder = LookupElementBuilder.create(wrap, lookupString);
						builder = builder.withPresentableText(presentationText);
						builder = builder.withIcon(IconDescriptorUpdaters.getIcon(wrap, Iconable.ICON_FLAG_VISIBILITY));

						val parentQName = wrap.getPresentableParentQName();
						builder = builder.withTypeText(parentQName, true);
						val ltGtInsertHandler = genericCount == 0 ? null : LtGtInsertHandler.getInstance(genericCount > 0);
						if(insideUsingList)
						{
							builder = builder.withInsertHandler(ltGtInsertHandler);
						}
						else
						{
							builder = builder.withInsertHandler(new InsertHandler<LookupElement>()
							{
								@Override
								public void handleInsert(InsertionContext context, LookupElement item)
								{
									if(ltGtInsertHandler != null)
									{
										ltGtInsertHandler.handleInsert(context, item);
									}

									new AddUsingAction(completionParameters.getEditor(), context.getFile(),
											Collections.<NamespaceReference>singleton(new NamespaceReference(parentQName, null))).execute();
								}
							});
						}
						completionResultSet.addElement(builder);
					}
				}
			}
		});
	}

	public static boolean isAlreadyResolved(DotNetQualifiedElement element, PsiElement parent)
	{
		String parentQName = element.getPresentableParentQName();
		if(StringUtil.isEmpty(parentQName))
		{
			return true;
		}

		ResolveState resolveState = ResolveState.initial();
		resolveState = resolveState.put(CSharpResolveUtil.SELECTOR, new MemberByNameSelector(element.getName()));
		resolveState = resolveState.put(MemberResolveScopeProcessor.BREAK_RULE, Boolean.TRUE);

		Couple<PsiElement> resolveLayers = CSharpReferenceExpressionImplUtil.getResolveLayers(parent, false);
		//PsiElement last = resolveLayers.getFirst();
		PsiElement targetToWalkChildren = resolveLayers.getSecond();

		AbstractScopeProcessor p = CSharpReferenceExpressionImplUtil.createMemberProcessor(element, CSharpReferenceExpression.ResolveToKind
				.TYPE_LIKE, ResolveResult.EMPTY_ARRAY, false);

		if(!CSharpResolveUtil.walkChildren(p, targetToWalkChildren, true, true, resolveState))
		{
			return true;
		}

		if(!CSharpResolveUtil.walkGenericParameterList(p, element, null, resolveState))
		{
			return true;
		}

		return !CSharpResolveUtil.walkUsing(p, parent, null, resolveState);
	}
}
