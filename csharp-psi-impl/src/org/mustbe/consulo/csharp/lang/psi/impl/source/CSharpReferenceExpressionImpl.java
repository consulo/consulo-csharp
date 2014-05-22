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

package org.mustbe.consulo.csharp.lang.psi.impl.source;

import java.util.ArrayList;
import java.util.List;

import org.consulo.lombok.annotations.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.CSharpLookupElementBuilder;
import org.mustbe.consulo.csharp.lang.psi.*;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpNamespaceAsElement;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpNamespaceHelper;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpVisibilityUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.AbstractScopeProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.ConstructorProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.ExtensionResolveScopeProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.MemberResolveScopeProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.MethodAcceptorImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.ResolveResultWithWeight;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.WeightProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpGenericParameterTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpNamespaceDefTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpNativeTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpQualifiedTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeDefTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.index.CSharpIndexKeys;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.*;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Conditions;
import com.intellij.openapi.util.Couple;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.CharFilter;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiQualifiedReference;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.ResolveState;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.QualifiedName;
import com.intellij.util.CommonProcessors;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.indexing.IdFilter;
import lombok.val;

/**
 * @author VISTALL
 * @since 28.11.13.
 */
@Logger
public class CSharpReferenceExpressionImpl extends CSharpElementImpl implements CSharpReferenceExpression, PsiPolyVariantReference
{
	private static class OurResolver implements ResolveCache.PolyVariantResolver<CSharpReferenceExpressionImpl>
	{
		private static final OurResolver INSTANCE = new OurResolver();

		@NotNull
		@Override
		public ResolveResult[] resolve(@NotNull CSharpReferenceExpressionImpl ref, boolean incompleteCode)
		{
			ResolveResult[] resolveResults = ref.multiResolveImpl(ref.kind());
			if(!incompleteCode)
			{
				return resolveResults;
			}
			List<ResolveResultWithWeight> filter = new ArrayList<ResolveResultWithWeight>();
			for(ResolveResult resolveResult : resolveResults)
			{
				ResolveResultWithWeight resolveResultWithWeight = (ResolveResultWithWeight) resolveResult;
				if(resolveResultWithWeight.isGoodResult())
				{
					filter.add(resolveResultWithWeight);
				}
			}
			return ContainerUtil.toArray(filter, ResolveResultWithWeight.ARRAY_FACTORY);
		}
	}

	public static final String AttributeSuffix = "Attribute";

	private static final Condition<PsiNamedElement> ourTypeOrMethodOrGenericCondition = new Condition<PsiNamedElement>()
	{
		@Override
		public boolean value(PsiNamedElement psiNamedElement)
		{
			return psiNamedElement instanceof DotNetTypeDeclaration || psiNamedElement instanceof DotNetGenericParameter ||
					psiNamedElement instanceof DotNetMethodDeclaration || psiNamedElement instanceof CSharpTypeDefStatementImpl;
		}
	};

	private static final Condition<PsiNamedElement> ourMethodCondition = new Condition<PsiNamedElement>()
	{
		@Override
		public boolean value(PsiNamedElement psiNamedElement)
		{
			return psiNamedElement instanceof DotNetLikeMethodDeclaration;
		}
	};

	private static final Condition<PsiNamedElement> ourMethodDelegate = new Condition<PsiNamedElement>()
	{
		@Override
		public boolean value(PsiNamedElement psiNamedElement)
		{
			return psiNamedElement instanceof DotNetMethodDeclaration && ((DotNetMethodDeclaration) psiNamedElement).isDelegate();
		}
	};

	public static enum ResolveToKind
	{
		TYPE_PARAMETER_FROM_PARENT,
		NAMESPACE,
		SOFT_NAMESPACE,
		METHOD,
		ATTRIBUTE,
		NATIVE_TYPE_WRAPPER,
		ARRAY_METHOD,
		TYPE_OR_GENERIC_PARAMETER_OR_DELEGATE_METHOD,
		CONSTRUCTOR,
		ANY_MEMBER,
		FIELD_OR_PROPERTY,
		THIS,
		BASE,
		LABEL
	}

	private static final TokenSet ourReferenceElements = TokenSet.create(CSharpTokens.THIS_KEYWORD, CSharpTokens.BASE_KEYWORD,
			CSharpTokens.IDENTIFIER);

	public CSharpReferenceExpressionImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@Override
	public PsiReference getReference()
	{
		return this;
	}

	@Override
	@Nullable
	public PsiElement getReferenceElement()
	{
		return findChildByType(ourReferenceElements);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitReferenceExpression(this);
	}

	@Nullable
	@Override
	public PsiElement getQualifier()
	{
		return findChildByClass(DotNetExpression.class);
	}

	@Nullable
	@Override
	public String getReferenceName()
	{
		PsiElement referenceElement = getReferenceElement();
		return referenceElement == null ? null : CSharpPsiUtilImpl.getNameWithoutAt(referenceElement.getText());
	}

	@Override
	public PsiElement getElement()
	{
		return this;
	}

	@Override
	public TextRange getRangeInElement()
	{
		PsiElement referenceElement = getReferenceElement();
		if(referenceElement == null)
		{
			return TextRange.EMPTY_RANGE;
		}

		PsiElement qualifier = getQualifier();
		int startOffset = qualifier != null ? qualifier.getTextLength() + 1 : 0;
		return new TextRange(startOffset, referenceElement.getTextLength() + startOffset);
	}

	@NotNull
	@Override
	public ResolveResult[] multiResolve(final boolean incompleteCode)
	{
		return ResolveCache.getInstance(getProject()).resolveWithCaching(this, OurResolver.INSTANCE, false, incompleteCode);
	}

	private ResolveResultWithWeight[] multiResolveImpl(ResolveToKind kind)
	{
		CSharpExpressionWithParameters p = null;
		PsiElement parent = getParent();
		if(parent instanceof CSharpExpressionWithParameters)
		{
			p = (CSharpExpressionWithParameters) parent;
		}
		return multiResolve0(kind, p, this);
	}

	public static <T extends PsiQualifiedReference & PsiElement> ResolveResultWithWeight[] multiResolve0(
			final ResolveToKind kind, final CSharpExpressionWithParameters parameters, final T e)
	{
		Condition<PsiNamedElement> namedElementCondition;
		@SuppressWarnings("unchecked") WeightProcessor<PsiNamedElement> weightProcessor = WeightProcessor.MAXIMUM;
		switch(kind)
		{
			case ATTRIBUTE:
				val referenceName = e.getReferenceName();
				if(referenceName == null)
				{
					return ResolveResultWithWeight.EMPTY_ARRAY;
				}
				namedElementCondition = new Condition<PsiNamedElement>()
				{
					@Override
					public boolean value(PsiNamedElement netNamedElement)
					{
						String candinateName = netNamedElement.getName();
						if(candinateName == null)
						{
							return false;
						}
						if(Comparing.equal(referenceName, candinateName))
						{
							return true;
						}

						if(candinateName.endsWith(AttributeSuffix) && Comparing.equal(referenceName + AttributeSuffix, netNamedElement.getName()))
						{
							return true;
						}
						return false;
					}
				};
				break;
			case NATIVE_TYPE_WRAPPER:
			case THIS:
			case BASE:
			case ARRAY_METHOD:
				namedElementCondition = Conditions.alwaysTrue();
				break;
			default:
				val referenceName2 = e.getReferenceName();
				if(referenceName2 == null)
				{
					return ResolveResultWithWeight.EMPTY_ARRAY;
				}
				val text2 = StringUtil.strip(referenceName2, CharFilter.NOT_WHITESPACE_FILTER);
				namedElementCondition = new Condition<PsiNamedElement>()
				{
					@Override
					public boolean value(PsiNamedElement netNamedElement)
					{
						return Comparing.equal(text2, netNamedElement.getName());
					}
				};
				break;
		}

		switch(kind)
		{
			case METHOD:
				weightProcessor = new WeightProcessor<PsiNamedElement>()
				{
					@Override
					public int getWeight(@NotNull PsiNamedElement psiNamedElement)
					{
						if(psiNamedElement instanceof DotNetVariable)
						{
							DotNetTypeRef typeRef = ((DotNetVariable) psiNamedElement).toTypeRef(true);
							if(typeRef instanceof CSharpLambdaTypeRef)
							{
								PsiElement target = ((CSharpLambdaTypeRef) typeRef).getTarget();
								if(target instanceof DotNetMethodDeclaration)
								{
									return MethodAcceptorImpl.calcAcceptableWeight(parameters, (CSharpMethodDeclaration) target);
								}
							}
						}
						else if(psiNamedElement instanceof DotNetMethodDeclaration)
						{
							return MethodAcceptorImpl.calcAcceptableWeight(parameters, (CSharpMethodDeclaration) psiNamedElement);
						}
						return 0;
					}
				};

				break;
			case ARRAY_METHOD:
				weightProcessor = new WeightProcessor<PsiNamedElement>()
				{
					@Override
					public int getWeight(@NotNull PsiNamedElement psiNamedElement)
					{
						if(!(psiNamedElement instanceof DotNetArrayMethodDeclaration))
						{
							return 0;
						}
						return MethodAcceptorImpl.calcAcceptableWeight(parameters, (DotNetArrayMethodDeclaration) psiNamedElement);
					}
				};
				break;
			case TYPE_OR_GENERIC_PARAMETER_OR_DELEGATE_METHOD:
				weightProcessor = new WeightProcessor<PsiNamedElement>()
				{
					@Override
					public int getWeight(@NotNull PsiNamedElement element)
					{
						if(element instanceof DotNetGenericParameterListOwner)
						{
							DotNetReferenceType referenceType = (DotNetReferenceType) e.getParent();
							if(referenceType.getParent() instanceof DotNetTypeWithTypeArguments)
							{
								DotNetType[] arguments = ((DotNetTypeWithTypeArguments) referenceType.getParent()).getArguments();
								return arguments.length == ((DotNetGenericParameterListOwner) element).getGenericParameters().length ? MAX_WEIGHT
										: 1;
							}
							else
							{
								return MAX_WEIGHT;
							}
						}
						else
						{
							return MAX_WEIGHT;
						}
					}
				};
				break;
		}

		return collectResults(kind, namedElementCondition, weightProcessor, e, false);
	}

	private static <T extends PsiQualifiedReference & PsiElement> ResolveResultWithWeight[] collectResults(
			@NotNull ResolveToKind kind,
			@NotNull Condition<PsiNamedElement> condition,
			@NotNull WeightProcessor<PsiNamedElement> weightProcessor,
			final T element,
			final boolean completion)
	{
		if(!element.isValid())
		{
			return ResolveResultWithWeight.EMPTY_ARRAY;
		}

		// dont allow resolving labels in references, when out from goto
		if(kind != ResolveToKind.LABEL)
		{
			condition = Conditions.and(condition, new Condition<PsiNamedElement>()
			{
				@Override
				public boolean value(PsiNamedElement psiNamedElement)
				{
					return !(psiNamedElement instanceof CSharpLabeledStatementImpl);
				}
			});
		}

		AbstractScopeProcessor p = null;
		PsiElement qualifier = element.getQualifier();
		switch(kind)
		{
			case THIS:
				DotNetTypeDeclaration typeDeclaration = PsiTreeUtil.getParentOfType(element, DotNetTypeDeclaration.class);
				if(typeDeclaration != null)
				{
					return new ResolveResultWithWeight[]{new ResolveResultWithWeight(typeDeclaration)};
				}
				break;
			case BASE:
				DotNetTypeRef baseDotNetTypeRef = ((CSharpReferenceExpressionImpl) element).resolveBaseTypeRef();
				PsiElement baseElement = baseDotNetTypeRef.resolve(element);
				if(baseElement != null)
				{
					return new ResolveResultWithWeight[]{new ResolveResultWithWeight(baseElement)};
				}
				break;
			case TYPE_PARAMETER_FROM_PARENT:
				DotNetGenericParameterListOwner parameterListOwner = PsiTreeUtil.getParentOfType(element, DotNetGenericParameterListOwner.class);
				if(parameterListOwner == null)
				{
					return ResolveResultWithWeight.EMPTY_ARRAY;
				}

				DotNetGenericParameter[] genericParameters = parameterListOwner.getGenericParameters();
				val list = new ArrayList<ResolveResultWithWeight>(genericParameters.length);
				for(val o : genericParameters)
				{
					if(condition.value(o))
					{
						list.add(new ResolveResultWithWeight(o));
					}
				}
				return list.isEmpty() ? ResolveResultWithWeight.EMPTY_ARRAY : list.toArray(new ResolveResultWithWeight[list.size()]);
			case NATIVE_TYPE_WRAPPER:
				PsiElement nativeElement = ((CSharpReferenceExpressionImpl) element).findChildByType(CSharpTokenSets.NATIVE_TYPES);
				assert nativeElement != null;
				CSharpNativeTypeRef nativeRuntimeType = CSharpNativeTypeImpl.ELEMENT_TYPE_TO_TYPE.get(nativeElement.getNode().getElementType());
				if(nativeRuntimeType == null)
				{
					return ResolveResultWithWeight.EMPTY_ARRAY;
				}
				PsiElement resolve = nativeRuntimeType.resolve(element);
				if(resolve == null)
				{
					return ResolveResultWithWeight.EMPTY_ARRAY;
				}

				return new ResolveResultWithWeight[]{new ResolveResultWithWeight(resolve)};
			case FIELD_OR_PROPERTY:
				CSharpNewExpression newExpression = PsiTreeUtil.getParentOfType(element, CSharpNewExpression.class);
				assert newExpression != null;
				DotNetTypeRef dotNetTypeRef = newExpression.toTypeRef(true);
				if(dotNetTypeRef == DotNetTypeRef.ERROR_TYPE)
				{
					return ResolveResultWithWeight.EMPTY_ARRAY;
				}
				PsiElement psiElement1 = dotNetTypeRef.resolve(element);
				if(psiElement1 == null)
				{
					return ResolveResultWithWeight.EMPTY_ARRAY;
				}
				ResolveState resolveState = ResolveState.initial();
				resolveState = resolveState.put(CSharpResolveUtil.EXTRACTOR_KEY, dotNetTypeRef.getGenericExtractor(psiElement1, element));

				p = new MemberResolveScopeProcessor(Conditions.and(condition, new Condition<PsiNamedElement>()
				{
					@Override
					public boolean value(PsiNamedElement psiNamedElement)
					{
						return psiNamedElement instanceof CSharpFieldDeclaration || psiNamedElement instanceof CSharpPropertyDeclaration;
					}
				}), weightProcessor, !completion);

				CSharpResolveUtil.walkChildren(p, psiElement1, false, null, resolveState);
				return p.toResolveResults();
			case LABEL:
				DotNetQualifiedElement parentOfType = PsiTreeUtil.getParentOfType(element, DotNetQualifiedElement.class);
				assert parentOfType != null;
				p = new MemberResolveScopeProcessor(Conditions.and(condition, new Condition<PsiNamedElement>()
				{
					@Override
					public boolean value(PsiNamedElement psiNamedElement)
					{
						return psiNamedElement instanceof CSharpLabeledStatementImpl;
					}
				}), weightProcessor, !completion);
				CSharpResolveUtil.treeWalkUp(p, element, element, parentOfType);
				return p.toResolveResults();
			case NAMESPACE:
				if(!completion)
				{
					String qName = StringUtil.strip(element.getText(), CharFilter.NOT_WHITESPACE_FILTER);
					val aPackage = CSharpNamespaceHelper.getNamespaceElementIfFind(element.getProject(), qName, element.getResolveScope());

					if(aPackage == null)
					{
						return ResolveResultWithWeight.EMPTY_ARRAY;
					}
					return new ResolveResultWithWeight[]{new ResolveResultWithWeight(aPackage)};
				}
				else
				{
					String parent = null;
					if(qualifier != null)
					{
						parent = StringUtil.strip(qualifier.getText(), CharFilter.NOT_WHITESPACE_FILTER);
					}
					else
					{
						parent = CSharpNamespaceHelper.ROOT;
					}
					val findFirstProcessor = new CommonProcessors.FindFirstProcessor<DotNetElement>();

					StubIndex.getInstance().processElements(CSharpIndexKeys.NAMESPACE_BY_QNAME_INDEX, parent, element.getProject(),
							element.getResolveScope(), DotNetElement.class, findFirstProcessor);

					if(findFirstProcessor.getFoundValue() != null)
					{
						val elements = new ArrayList<ResolveResultWithWeight>();

						val parentQName = QualifiedName.fromDottedString(parent);
						StubIndex.getInstance().processAllKeys(CSharpIndexKeys.NAMESPACE_BY_QNAME_INDEX, new Processor<String>()
						{
							@Override
							public boolean process(String qName)
							{
								ProgressIndicatorProvider.checkCanceled();

								QualifiedName childQName = QualifiedName.fromDottedString(qName);
								if(childQName.matchesPrefix(parentQName) && parentQName.getComponentCount() == (childQName.getComponentCount() - 1))
								{
									val namespaceAsElement = new CSharpNamespaceAsElement(element.getProject(), qName, element.getResolveScope());
									if(!namespaceAsElement.isValid())
									{
										return true;
									}
									elements.add(new ResolveResultWithWeight(namespaceAsElement));
								}
								return true;
							}
						}, element.getResolveScope(), IdFilter.getProjectIdFilter(element.getProject(), false));

						return ContainerUtil.toArray(elements, ResolveResultWithWeight.ARRAY_FACTORY);
					}
					else
					{
						return ResolveResultWithWeight.EMPTY_ARRAY;
					}
				}
			case SOFT_NAMESPACE:
				String qName2 = StringUtil.strip(element.getText(), CharFilter.NOT_WHITESPACE_FILTER);
				CSharpNamespaceAsElement namespaceAsElement = new CSharpNamespaceAsElement(element.getProject(), qName2, element.getResolveScope());
				return new ResolveResultWithWeight[]{new ResolveResultWithWeight(namespaceAsElement)};
			case CONSTRUCTOR:

				CSharpReferenceExpressionImpl referenceExpression = (CSharpReferenceExpressionImpl) element;

				ResolveToKind newKind = ResolveToKind.TYPE_OR_GENERIC_PARAMETER_OR_DELEGATE_METHOD;
				PsiElement referenceElement = referenceExpression.getReferenceElement();
				assert referenceElement != null;
				if(referenceElement.getNode().getElementType() == CSharpTokens.BASE_KEYWORD)
				{
					newKind = ResolveToKind.BASE;
				}
				else if(referenceElement.getNode().getElementType() == CSharpTokens.THIS_KEYWORD)
				{
					newKind = ResolveToKind.THIS;
				}

				ResolveResultWithWeight[] resolveResult = referenceExpression.multiResolveImpl(newKind);
				if(resolveResult.length == 0)
				{
					return ResolveResultWithWeight.EMPTY_ARRAY;
				}
				ResolveResultWithWeight resolveResultWithWeight = resolveResult[0];
				if(!resolveResultWithWeight.isGoodResult())
				{
					return ResolveResultWithWeight.EMPTY_ARRAY;
				}
				PsiElement type = resolveResultWithWeight.getElement();
				if(!(type instanceof DotNetConstructorListOwner))
				{
					return ResolveResultWithWeight.EMPTY_ARRAY;
				}

				CSharpMethodCallParameterListOwner parent = PsiTreeUtil.getParentOfType(element, CSharpMethodCallParameterListOwner.class);

				val constructorProcessor = new ConstructorProcessor(parent, completion);
				((DotNetConstructorListOwner) type).processConstructors(new Processor<DotNetConstructorDeclaration>()
				{
					@Override
					public boolean process(DotNetConstructorDeclaration constructorDeclaration)
					{
						return constructorProcessor.execute(constructorDeclaration, null);
					}
				});

				constructorProcessor.executeDefault((DotNetConstructorListOwner) type);
				return constructorProcessor.toResolveResults();
			case ATTRIBUTE:
				/*condition = Conditions.and(condition, ourTypeOrMethodOrGenericCondition);
				val resolveResults = processAnyMember(qualifier, condition, named);
				if(resolveResults.size() != 1)
				{
					return resolveResults;
				}
				return resolveResults; //TODO [VISTALL] resolve to constuctor   */
			case TYPE_OR_GENERIC_PARAMETER_OR_DELEGATE_METHOD:
			case METHOD:
			case ARRAY_METHOD:
			case ANY_MEMBER:
				if(!completion)
				{
					if(kind == ResolveToKind.TYPE_OR_GENERIC_PARAMETER_OR_DELEGATE_METHOD)
					{
						condition = Conditions.and(condition, ourTypeOrMethodOrGenericCondition);
					}
					else if(kind == ResolveToKind.ANY_MEMBER)
					{
						condition = Conditions.and(condition, Conditions.not(ourMethodCondition));
					}
					else if(kind == ResolveToKind.METHOD)
					{
						condition = Conditions.and(condition, Conditions.not(ourMethodDelegate));
					}
				}

				return processAnyMember(qualifier, condition, weightProcessor, element, kind, completion);
		}
		return ResolveResultWithWeight.EMPTY_ARRAY;
	}

	private static <T extends PsiQualifiedReference & PsiElement> ResolveResultWithWeight[] processAnyMember(
			PsiElement qualifier,
			Condition<PsiNamedElement> condition,
			WeightProcessor<PsiNamedElement> weightProcessor,
			T element,
			ResolveToKind kind,
			boolean с)
	{
		PsiElement target = element;
		DotNetGenericExtractor extractor = DotNetGenericExtractor.EMPTY;

		if(qualifier instanceof DotNetExpression)
		{
			DotNetTypeRef dotNetTypeRef = ((DotNetExpression) qualifier).toTypeRef(true);

			PsiElement resolve = dotNetTypeRef.resolve(element);

			if(resolve != null)
			{
				target = resolve;
				extractor = dotNetTypeRef.getGenericExtractor(resolve, element);
			}
			else
			{
				return ResolveResultWithWeight.EMPTY_ARRAY;
			}
		}

		if(!target.isValid())
		{
			return ResolveResultWithWeight.EMPTY_ARRAY;
		}

		MemberResolveScopeProcessor p = new MemberResolveScopeProcessor(condition, weightProcessor, !с);

		ResolveState resolveState = ResolveState.initial();
		resolveState = resolveState.put(CSharpResolveUtil.EXTRACTOR_KEY, extractor);

		if(target != element)
		{
			if(!CSharpResolveUtil.walkChildren(p, target, false, null, resolveState))
			{
				return p.toResolveResults();
			}


			Couple<PsiElement> resolveLayers = getResolveLayers(element, false);

			PsiElement targetToWalkChildren = resolveLayers.getSecond();
			if(targetToWalkChildren == null)
			{
				return p.toResolveResults();
			}

			// walk for extensions
			ExtensionResolveScopeProcessor p2 = new ExtensionResolveScopeProcessor(condition, weightProcessor, !с);
			p2.merge(p);

			resolveState = ResolveState.initial();
			resolveState = resolveState.put(CSharpResolveUtil.EXTRACTOR_KEY, extractor);
			resolveState = resolveState.put(CSharpResolveUtil.CONTAINS_FILE_KEY, element.getContainingFile());

			CSharpResolveUtil.walkChildren(p2, targetToWalkChildren, false, null, resolveState);
			return p2.toResolveResults();
		}
		else
		{
			resolveState = resolveState.put(CSharpResolveUtil.CONTAINS_FILE_KEY, element.getContainingFile());

			Couple<PsiElement> resolveLayers = getResolveLayers(element, false);

			PsiElement last = resolveLayers.getFirst();
			PsiElement targetToWalkChildren = resolveLayers.getSecond();

			if(!CSharpResolveUtil.treeWalkUp(p, target, element, last, resolveState))
			{
				return p.toResolveResults();
			}

			if(last == null)
			{
				return p.toResolveResults();
			}

			boolean typeResolving = kind != ResolveToKind.METHOD;
			CSharpResolveUtil.walkChildren(p, targetToWalkChildren, typeResolving, null, resolveState);
			return p.toResolveResults();
		}
	}

	@NotNull
	private static Couple<PsiElement> getResolveLayers(PsiElement element, boolean strict)
	{
		PsiElement last = null;
		PsiElement targetToWalkChildren = null;

		PsiElement temp = strict ? element : element.getParent();
		while(temp != null)
		{
			ProgressIndicatorProvider.checkCanceled();

			if(temp instanceof DotNetType)
			{
				DotNetStatement statement = PsiTreeUtil.getParentOfType(temp, DotNetStatement.class);
				if(statement == null)
				{
					PsiElement listOwner = PsiTreeUtil.getParentOfType(temp, DotNetModifierListOwner.class);
					if(listOwner != null)
					{
						Couple<PsiElement> resolveLayers = getResolveLayers(listOwner, true);
						last = resolveLayers.getFirst();
						targetToWalkChildren = resolveLayers.getSecond();
						break;
					}
				}
			}
			else if(temp instanceof DotNetParameter)
			{
				targetToWalkChildren = PsiTreeUtil.getParentOfType(temp, DotNetParameterListOwner.class);
				assert targetToWalkChildren != null;
				last = targetToWalkChildren.getParent();
			}
			else if(temp instanceof CSharpAttributeImpl)
			{
				last = temp;
				targetToWalkChildren = PsiTreeUtil.getParentOfType(temp, DotNetModifierListOwner.class);
			}
			else if(temp instanceof CSharpMethodCallParameterListImpl)
			{
				DotNetAttribute attribute = PsiTreeUtil.getParentOfType(temp, DotNetAttribute.class);
				if(attribute != null)
				{
					last = attribute;
					targetToWalkChildren = PsiTreeUtil.getParentOfType(attribute, DotNetModifierListOwner.class);
					break;
				}
			}
			else if(temp instanceof DotNetFieldDeclaration)
			{
				last = temp.getParent();
				targetToWalkChildren = temp.getParent();
				break;
			}
			else if(temp instanceof DotNetXXXAccessor)
			{
				last = temp;
				targetToWalkChildren = temp.getParent().getParent();
				break;
			}
			else if(temp instanceof DotNetLikeMethodDeclaration)
			{
				last = temp.getParent();
				targetToWalkChildren = temp.getParent();
				break;
			}
			else if(temp instanceof DotNetTypeDeclaration)
			{
				last = temp;
				targetToWalkChildren = temp.getParent();
				break;
			}
			else if(temp instanceof CSharpCodeFragment)
			{
				temp = ((CSharpCodeFragment) temp).getScopeElement();
				continue;
			}
			temp = temp.getParent();
		}

		if(targetToWalkChildren == null)
		{
			return Couple.newOne(last, last);
			//LOGGER.error(element.getText() + " " + last + " " + kind + " " + element.getParent() + " " + element.getContainingFile().getName());
		}
		return Couple.newOne(last, targetToWalkChildren);
	}

	@NotNull
	private DotNetTypeRef resolveBaseTypeRef()
	{
		DotNetTypeDeclaration typeDeclaration = PsiTreeUtil.getParentOfType(this, DotNetTypeDeclaration.class);
		if(typeDeclaration == null)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}

		DotNetTypeRef[] anExtends = typeDeclaration.getExtendTypeRefs();
		if(anExtends.length == 0)
		{
			return new CSharpTypeDefTypeRef(DotNetTypes.System_Object, 0);
		}
		else
		{
			for(DotNetTypeRef anExtend : anExtends)
			{
				PsiElement resolve = anExtend.resolve(this);
				if(resolve instanceof DotNetTypeDeclaration && !((DotNetTypeDeclaration) resolve).isInterface())
				{
					return anExtend;
				}
			}

			return new CSharpTypeDefTypeRef(DotNetTypes.System_Object, 0);
		}
	}

	@Nullable
	@Override
	public PsiElement resolve()
	{
		ResolveResult[] resolveResults = multiResolve(false);
		if(resolveResults.length == 0)
		{
			return null;
		}
		ResolveResultWithWeight resolveResult = (ResolveResultWithWeight) resolveResults[0];
		if(!resolveResult.isGoodResult())
		{
			return null;
		}
		return resolveResult.getElement();
	}

	@NotNull
	public ResolveToKind kind()
	{
		PsiElement tempElement = getParent();
		if(tempElement instanceof CSharpGenericConstraintImpl)
		{
			DotNetGenericParameterListOwner parameterListOwner = PsiTreeUtil.getParentOfType(this, DotNetGenericParameterListOwner.class);
			if(parameterListOwner == null)
			{
				return ResolveToKind.ANY_MEMBER;
			}

			return ResolveToKind.TYPE_PARAMETER_FROM_PARENT;
		}
		else if(tempElement instanceof CSharpNamespaceDeclarationImpl)
		{
			return ResolveToKind.SOFT_NAMESPACE;
		}
		else if(tempElement instanceof DotNetReferenceType)
		{
			PsiElement parentOfParent = tempElement.getParent();
			if(parentOfParent instanceof CSharpMethodCallParameterListOwner && ((CSharpMethodCallParameterListOwner) parentOfParent).canResolve())
			{
				return ResolveToKind.CONSTRUCTOR;
			}
			return ResolveToKind.TYPE_OR_GENERIC_PARAMETER_OR_DELEGATE_METHOD;
		}
		else if(tempElement instanceof CSharpUsingNamespaceStatementImpl)
		{
			return ResolveToKind.NAMESPACE;
		}
		else if(tempElement instanceof CSharpConstructorSuperCallImpl)
		{
			return ResolveToKind.CONSTRUCTOR;
		}
		else if(tempElement instanceof CSharpAttributeImpl)
		{
			return ResolveToKind.ATTRIBUTE;
		}
		else if(tempElement instanceof CSharpFieldOrPropertySet)
		{
			if(((CSharpFieldOrPropertySet) tempElement).getNameReferenceExpression() == this)
			{
				return ResolveToKind.FIELD_OR_PROPERTY;
			}
		}
		else if(tempElement instanceof CSharpReferenceExpressionImpl)
		{
			CSharpNamespaceDeclarationImpl netNamespaceDeclaration = PsiTreeUtil.getParentOfType(this, CSharpNamespaceDeclarationImpl.class);
			if(netNamespaceDeclaration != null)
			{
				DotNetReferenceExpression namespaceReference = netNamespaceDeclaration.getNamespaceReference();
				if(namespaceReference != null && PsiTreeUtil.isAncestor(namespaceReference, this, false))
				{
					return ResolveToKind.SOFT_NAMESPACE;
				}
			}

			if(PsiTreeUtil.getParentOfType(this, CSharpUsingNamespaceStatementImpl.class) != null)
			{
				return ResolveToKind.NAMESPACE;
			}

			if(PsiTreeUtil.getParentOfType(this, DotNetReferenceType.class) != null)
			{
				return ResolveToKind.NAMESPACE;
			}
		}
		else if(tempElement instanceof CSharpMethodCallExpressionImpl)
		{
			return ResolveToKind.METHOD;
		}
		else if(tempElement instanceof CSharpGotoStatementImpl)
		{
			return ResolveToKind.LABEL;
		}

		tempElement = findChildByType(CSharpTokenSets.NATIVE_TYPES);
		if(tempElement != null)
		{
			return ResolveToKind.NATIVE_TYPE_WRAPPER;
		}

		tempElement = findChildByType(CSharpTokens.THIS_KEYWORD);
		if(tempElement != null)
		{
			return ResolveToKind.THIS;
		}
		tempElement = findChildByType(CSharpTokens.BASE_KEYWORD);
		if(tempElement != null)
		{
			return ResolveToKind.BASE;
		}
		return ResolveToKind.ANY_MEMBER;
	}

	@NotNull
	@Override
	public String getCanonicalText()
	{
		return getText();
	}

	@Override
	public PsiElement handleElementRename(String s) throws IncorrectOperationException
	{
		PsiElement element = getReferenceElement();

		PsiElement newIdentifier = CSharpFileFactory.createIdentifier(getProject(), s);

		element.replace(newIdentifier);
		return this;
	}

	@Override
	public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException
	{
		return this;
	}

	@Override
	public boolean isReferenceTo(PsiElement element)
	{
		PsiElement resolve = resolve();
		if(element instanceof CSharpNamespaceAsElement && resolve instanceof CSharpNamespaceAsElement)
		{
			return Comparing.equal(((CSharpNamespaceAsElement) resolve).getPresentableQName(), ((CSharpNamespaceAsElement) element)
					.getPresentableQName());
		}
		return element.getManager().areElementsEquivalent(element, resolve);
	}

	@NotNull
	@Override
	public Object[] getVariants()
	{
		ResolveResult[] psiElements = collectResults(ResolveToKind.ANY_MEMBER, new Condition<PsiNamedElement>()
		{
			@Override
			public boolean value(PsiNamedElement e)
			{
				if(e.getName() == null)
				{
					return false;
				}

				if(e instanceof CSharpLocalVariable || e instanceof DotNetParameter || e instanceof CSharpLambdaParameter)
				{
					return true;
				}
				if(e instanceof CSharpConstructorDeclaration)
				{
					return false;
				}
				if(e instanceof CSharpMethodDeclaration && ((CSharpMethodDeclaration) e).isOperator())
				{
					return false;
				}
				if(e instanceof CSharpArrayMethodDeclarationImpl)
				{
					return false;
				}

				if(e instanceof DotNetModifierListOwner)
				{
					if(!CSharpVisibilityUtil.isVisibleForCompletion((DotNetModifierListOwner) e, CSharpReferenceExpressionImpl.this))
					{
						return false;
					}
				}
				return true;
			}
		}, WeightProcessor.MAXIMUM, this, true);
		return CSharpLookupElementBuilder.getInstance(getProject()).buildToLookupElements(this, psiElements);
	}

	@Override
	public boolean isSoft()
	{
		return false;
	}

	@NotNull
	@Override
	public DotNetTypeRef toTypeRef(boolean resolveFromParent)
	{
		return toTypeRef(kind(), resolveFromParent);
	}

	@NotNull
	public DotNetTypeRef toTypeRef(ResolveToKind resolveToKind, boolean resolveFromParent)
	{
		switch(resolveToKind)
		{
			case BASE:
				return resolveBaseTypeRef();
		}

		ResolveResultWithWeight[] resolveResults = multiResolveImpl(resolveToKind);
		if(resolveResults.length == 0)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}

		ResolveResultWithWeight resolveResult = resolveResults[0];
		if(!resolveResult.isGoodResult())
		{
			return DotNetTypeRef.ERROR_TYPE;
		}
		return toTypeRef(resolveResult.getElement());
	}

	@NotNull
	public static DotNetTypeRef toTypeRef(@Nullable PsiElement resolve)
	{
		if(resolve instanceof CSharpNamespaceAsElement)
		{
			return new CSharpNamespaceDefTypeRef(((CSharpNamespaceAsElement) resolve).getPresentableQName());
		}
		else if(resolve instanceof DotNetTypeDeclaration)
		{
			return new CSharpQualifiedTypeRef((DotNetTypeDeclaration) resolve);
		}
		else if(resolve instanceof CSharpTypeDefStatementImpl)
		{
			return ((CSharpTypeDefStatementImpl) resolve).toTypeRef();
		}
		else if(resolve instanceof DotNetGenericParameter)
		{
			return new CSharpGenericParameterTypeRef((DotNetGenericParameter) resolve);
		}
		else if(resolve instanceof CSharpMethodDeclaration)
		{
			return ((CSharpMethodDeclaration) resolve).getReturnTypeRef();
		}
		else if(resolve instanceof DotNetVariable)
		{
			return ((DotNetVariable) resolve).toTypeRef(true);
		}
		return DotNetTypeRef.ERROR_TYPE;
	}
}
