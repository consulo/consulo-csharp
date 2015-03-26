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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.CSharpLookupElementBuilder;
import org.mustbe.consulo.csharp.ide.codeInsight.actions.AddUsingAction;
import org.mustbe.consulo.csharp.ide.codeInsight.actions.MethodGenerateUtil;
import org.mustbe.consulo.csharp.ide.completion.expected.ExpectedTypeInfo;
import org.mustbe.consulo.csharp.ide.completion.expected.ExpectedTypeVisitor;
import org.mustbe.consulo.csharp.ide.completion.util.LtGtInsertHandler;
import org.mustbe.consulo.csharp.lang.psi.*;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpVisibilityUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.MsilToCSharpUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.resolve.CSharpResolveContextUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpArrayInitializerImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpArrayInitializerSingleValueImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpArrayInitializerValue;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpAsExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpIsExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpNewExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpReferenceExpressionImplUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpTypeCastExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.AbstractScopeProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.CSharpResolveOptions;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.MemberResolveScopeProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpArrayTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaResolveResult;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpMethodImplUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.index.TypeIndex;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import org.mustbe.consulo.csharp.lang.psi.resolve.MemberByNameSelector;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.csharp.module.extension.CSharpModuleUtil;
import org.mustbe.consulo.dotnet.ide.DotNetElementPresentationUtil;
import org.mustbe.consulo.dotnet.libraryAnalyzer.NamespaceReference;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetParameterList;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import org.mustbe.consulo.dotnet.psi.DotNetStatement;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.codeInsight.TailType;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.completion.util.ParenthesesInsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.ide.IconDescriptorUpdaters;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Couple;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.text.StringUtil;
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
		extend(CompletionType.BASIC, psiElement(CSharpTokens.IDENTIFIER).withParent(CSharpReferenceExpressionEx.class),
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

				List<ExpectedTypeInfo> expectedTypeRefs = ExpectedTypeVisitor.findExpectedTypeRefs(parent);
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

		extend(CompletionType.BASIC, psiElement(CSharpTokens.IDENTIFIER).withParent(CSharpReferenceExpressionEx.class),
				new CompletionProvider<CompletionParameters>()
		{
			@Override
			protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result)
			{
				CSharpReferenceExpressionEx expression = (CSharpReferenceExpressionEx) parameters.getPosition().getParent();
				CSharpReferenceExpression.ResolveToKind kind = expression.kind();
				if(needRemapToAnyResolving(kind, expression))
				{
					kind = CSharpReferenceExpression.ResolveToKind.ANY_MEMBER;
				}

				if(kind == CSharpReferenceExpression.ResolveToKind.CONSTRUCTOR)
				{
					kind = CSharpReferenceExpression.ResolveToKind.TYPE_LIKE;
				}
				CSharpCallArgumentListOwner callArgumentListOwner = CSharpReferenceExpressionImplUtil.findCallArgumentListOwner(kind, expression);
				ResolveResult[] psiElements = CSharpReferenceExpressionImplUtil.collectResults(new CSharpResolveOptions(kind, null,
						expression, callArgumentListOwner, true, true));
				List<LookupElement> lookupElements = CSharpLookupElementBuilder.buildToLookupElements(psiElements);

				prioritizeLookupItems(expression, kind, lookupElements);

				result.addAllElements(lookupElements);
			}
		});

		extend(CompletionType.BASIC, psiElement(CSharpTokens.IDENTIFIER).withParent(CSharpReferenceExpression.class).withSuperParent(2,
				CSharpArrayInitializerImpl.class).withSuperParent(3, CSharpNewExpressionImpl.class), new CompletionProvider<CompletionParameters>()
		{
			@Override
			protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result)
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

				ResolveResult[] resolveResults = CSharpReferenceExpressionImplUtil.collectResults(options);

				List<LookupElement> lookupElements = CSharpLookupElementBuilder.buildToLookupElements(resolveResults);

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
					}
					result.addElement(lookupElement);
				}
			}
		});

		extend(CompletionType.BASIC, psiElement(CSharpTokens.IDENTIFIER).withParent(CSharpReferenceExpression.class),
				new CompletionProvider<CompletionParameters>()
		{

			@Override
			protected void addCompletions(@NotNull final CompletionParameters completionParameters,
					ProcessingContext processingContext,
					@NotNull CompletionResultSet completionResultSet)
			{
				val parent = (CSharpReferenceExpression) completionParameters.getPosition().getParent();
				if(parent.getQualifier() != null)
				{
					return;
				}

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

		extend(CompletionType.BASIC, psiElement().afterLeaf(psiElement().withElementType(CSharpTokens.NEW_KEYWORD)),
				new CompletionProvider<CompletionParameters>()
		{
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

							LookupElementBuilder builder = LookupElementBuilder.create(typeText);
							builder = builder.withIcon(getIconForInnerTypeRef((CSharpArrayTypeRef) typeRef, position));
							// add without {...}
							result.addElement(PrioritizedLookupElement.withPriority(builder, CSharpCompletionUtil.EXPR_REF_PRIORITY + 0.1));

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

							result.addElement(PrioritizedLookupElement.withPriority(builder, CSharpCompletionUtil.EXPR_REF_PRIORITY));
						}
						else
						{
							DotNetTypeResolveResult typeResolveResult = typeRef.resolve(position);

							PsiElement element = typeResolveResult.getElement();
							if(element == null)
							{
								continue;
							}

							DotNetGenericExtractor genericExtractor = typeResolveResult.getGenericExtractor();
							CSharpResolveContext cSharpResolveContext = CSharpResolveContextUtil.createContext(genericExtractor,
									position.getResolveScope(), element);

							CSharpElementGroup<CSharpConstructorDeclaration> group = cSharpResolveContext.constructorGroup();
							Collection<CSharpConstructorDeclaration> objects = group == null ? Collections.<CSharpConstructorDeclaration>emptyList()
									: group.getElements();

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

								LookupElementBuilder builder = buildForConstructor(object, genericExtractor);
								if(builder != null)
								{
									result.addElement(PrioritizedLookupElement.withPriority(builder, CSharpCompletionUtil.EXPR_REF_PRIORITY));
								}
							}
						}
					}
				}
			}
		});

		extend(CompletionType.BASIC, psiElement(CSharpTokens.IDENTIFIER).withParent(CSharpReferenceExpression.class),
				new CompletionProvider<CompletionParameters>()
		{

			@Override
			protected void addCompletions(@NotNull final CompletionParameters completionParameters,
					ProcessingContext processingContext,
					@NotNull CompletionResultSet completionResultSet)
			{
				val parent = (CSharpReferenceExpression) completionParameters.getPosition().getParent();
				if(parent.getQualifier() != null)
				{
					return;
				}

				CSharpReferenceExpression.ResolveToKind kind = parent.kind();
				if(kind == CSharpReferenceExpression.ResolveToKind.TYPE_LIKE ||
						kind == CSharpReferenceExpression.ResolveToKind.CONSTRUCTOR ||
						kind == CSharpReferenceExpression.ResolveToKind.ANY_MEMBER)
				{
					if(kind == CSharpReferenceExpression.ResolveToKind.CONSTRUCTOR)
					{
						kind = CSharpReferenceExpression.ResolveToKind.TYPE_LIKE;
					}

					val referenceName = parent.getReferenceName().replace(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED, "");

					if(StringUtil.isEmpty(referenceName))
					{
						return;
					}

					val matcher = new PlainPrefixMatcher(referenceName);
					val names = new ArrayList<String>();
					TypeIndex.getInstance().processAllKeys(parent.getProject(), new Processor<String>()
					{
						@Override
						public boolean process(String s)
						{
							if(matcher.prefixMatches(s))
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

					List<LookupElement> lookupElements = new ArrayList<LookupElement>(typeDeclarations.size());
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
						lookupElements.add(builder);
					}

					//FIXME [VISTALL] perfomance issue
					if(lookupElements.size() < 500)
					{
						prioritizeLookupItems(parent, kind, lookupElements);
					}

					completionResultSet.addAllElements(lookupElements);
				}
			}
		});
	}

	private static void prioritizeLookupItems(@NotNull CSharpReferenceExpression expression,
			@NotNull CSharpReferenceExpression.ResolveToKind kind,
			@NotNull List<LookupElement> lookupElements)
	{
		List<ExpectedTypeInfo> expectedTypeRefs = ExpectedTypeVisitor.findExpectedTypeRefs(expression);
		if(!expectedTypeRefs.isEmpty())
		{
			ListIterator<LookupElement> iterator = lookupElements.listIterator();
			while(iterator.hasNext())
			{
				LookupElement next = iterator.next();

				PsiElement psiElement = next.getPsiElement();
				if(psiElement == null)
				{
					iterator.set(PrioritizedLookupElement.withPriority(next, -0.5));
					continue;
				}

				// if we have not type declaration, make types lower, dont allow int i = Int32 completion more high
				if(kind != CSharpReferenceExpression.ResolveToKind.TYPE_LIKE && CSharpCompletionUtil.isTypeLikeElement(psiElement))
				{
					iterator.set(PrioritizedLookupElement.withPriority(next, -0.5));
					continue;
				}

				DotNetTypeRef typeOfElement;
				if(psiElement instanceof CSharpMethodDeclaration)
				{
					CSharpMethodDeclaration methodDeclaration = (CSharpMethodDeclaration) psiElement;
					typeOfElement = methodDeclaration.getReturnTypeRef();

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
						else
						{
							DotNetTypeResolveResult typeResolveResult = expectedTypeInfo.getTypeRef().resolve(expression);
							if(typeResolveResult instanceof CSharpLambdaResolveResult)
							{
								if(CSharpTypeUtil.isInheritable(expectedTypeInfo.getTypeRef(), new CSharpLambdaTypeRef(methodDeclaration),
										expression))
								{
									next = buildForMethodReference(methodDeclaration);
									iterator.set(PrioritizedLookupElement.withPriority(next, CSharpCompletionUtil.EXPR_REF_PRIORITY));
								}
							}
						}
					}
				}
				else
				{
					typeOfElement = CSharpReferenceExpressionImplUtil.toTypeRef(psiElement);

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
		}
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
				if(parent1 instanceof CSharpIsExpressionImpl || parent1 instanceof CSharpAsExpressionImpl || parent1 instanceof
						CSharpTypeCastExpressionImpl)
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

	private static LookupElementBuilder buildForMethodReference(final CSharpMethodDeclaration methodDeclaration)
	{
		LookupElementBuilder builder = LookupElementBuilder.create(methodDeclaration.getName());
		builder = builder.withIcon(IconDescriptorUpdaters.getIcon(methodDeclaration, Iconable.ICON_FLAG_VISIBILITY));

		final DotNetTypeRef[] parameterTypes = methodDeclaration.getParameterTypeRefs();

		String genericText = DotNetElementPresentationUtil.formatGenericParameters(methodDeclaration);

		String parameterText = genericText + "(" + StringUtil.join(parameterTypes, new Function<DotNetTypeRef, String>()
		{
			@Override
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
		return builder;
	}

	@Nullable
	private static LookupElementBuilder buildForConstructor(final CSharpConstructorDeclaration declaration, final DotNetGenericExtractor extractor)
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
			public String fun(DotNetTypeRef parameter)
			{
				return CSharpTypeRefPresentationUtil.buildShortText(parameter, declaration);
			}
		}, ", ") + ")";

		LookupElementBuilder builder = LookupElementBuilder.create(parent, lookupString);
		builder = builder.withIcon(IconDescriptorUpdaters.getIcon(parent, Iconable.ICON_FLAG_VISIBILITY));
		builder = builder.withTailText(parameterText, true);
		builder = builder.withInsertHandler(ParenthesesInsertHandler.getInstance(parameters.length > 0));
		return builder;
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

		CSharpResolveOptions options = CSharpResolveOptions.build();
		options.kind(CSharpReferenceExpression.ResolveToKind.TYPE_LIKE);
		options.element(element);

		AbstractScopeProcessor p = CSharpReferenceExpressionImplUtil.createMemberProcessor(options);

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
