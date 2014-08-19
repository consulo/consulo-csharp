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
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpNativeTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefFromGenericParameter;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefFromNamespace;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefFromQualifiedElement;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.index.CSharpIndexKeys;
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
			return psiNamedElement instanceof CSharpMethodDeclaration && ((CSharpMethodDeclaration) psiNamedElement).isDelegate();
		}
	};

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

	@NotNull
	public ResolveResultWithWeight[] multiResolveImpl(ResolveToKind kind)
	{
		CSharpCallArgumentListOwner p = null;
		PsiElement parent = getParent();
		if(parent instanceof CSharpCallArgumentListOwner)
		{
			p = (CSharpCallArgumentListOwner) parent;
		}
		return multiResolve0(kind, p, this);
	}

	public static <T extends PsiQualifiedReference & PsiElement> ResolveResultWithWeight[] multiResolve0(ResolveToKind kind,
			final CSharpCallArgumentListOwner parameters, final T e)
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
						if(!(netNamedElement instanceof DotNetTypeDeclaration))
						{
							return false;
						}
						String candinateName = netNamedElement.getName();
						if(candinateName == null)
						{
							return false;
						}
						if(Comparing.equal(referenceName, candinateName))
						{
							return true;
						}

						if(candinateName.endsWith(CSharpReferenceExpressionImplUtil.AttributeSuffix) && Comparing.equal(referenceName +
								CSharpReferenceExpressionImplUtil.AttributeSuffix, netNamedElement.getName()))
						{
							return true;
						}
						return false;
					}
				};
				kind = ResolveToKind.TYPE_OR_GENERIC_PARAMETER_OR_DELEGATE_METHOD; //remap to type search
				break;
			case NATIVE_TYPE_WRAPPER:
			case THIS:
			case BASE:
			case ARRAY_METHOD:
				namedElementCondition = new Condition<PsiNamedElement>()
				{
					@Override
					public boolean value(PsiNamedElement psiNamedElement)
					{
						return psiNamedElement instanceof CSharpArrayMethodDeclaration;
					}
				};
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
									return MethodAcceptorImpl.calcAcceptableWeight(e, parameters, (CSharpMethodDeclaration) target);
								}
							}
						}
						else if(psiNamedElement instanceof DotNetMethodDeclaration)
						{
							return MethodAcceptorImpl.calcAcceptableWeight(e, parameters, (CSharpMethodDeclaration) psiNamedElement);
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
						if(!(psiNamedElement instanceof CSharpArrayMethodDeclaration))
						{
							return 0;
						}
						return MethodAcceptorImpl.calcAcceptableWeight(e, parameters,
								(CSharpArrayMethodDeclaration) psiNamedElement);
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
							PsiElement parent = e.getParent();
							if(parent instanceof DotNetAttribute)
							{
								return MAX_WEIGHT;
							}

							DotNetUserType referenceType = (DotNetUserType) parent;
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

	private static <T extends PsiQualifiedReference & PsiElement> ResolveResultWithWeight[] collectResults(@NotNull ResolveToKind kind,
			@NotNull Condition<PsiNamedElement> condition, @NotNull WeightProcessor<PsiNamedElement> weightProcessor, final T element,
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
			case GENERIC_PARAMETER_FROM_PARENT:
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
				DotNetTypeRef resolvedTypeRef;
				CSharpNamedCallArgument namedCallArgument = PsiTreeUtil.getParentOfType(element, CSharpNamedCallArgument.class);
				if(namedCallArgument != null)
				{
					DotNetAttribute attribute = PsiTreeUtil.getParentOfType(element, DotNetAttribute.class);
					assert attribute != null;
					resolvedTypeRef = attribute.toTypeRef();
				}
				else
				{
					CSharpNewExpression newExpression = PsiTreeUtil.getParentOfType(element, CSharpNewExpression.class);
					assert newExpression != null;
					resolvedTypeRef = newExpression.toTypeRef(false);
				}
				if(resolvedTypeRef == DotNetTypeRef.ERROR_TYPE)
				{
					return ResolveResultWithWeight.EMPTY_ARRAY;
				}
				PsiElement psiElement1 = resolvedTypeRef.resolve(element);
				if(psiElement1 == null)
				{
					return ResolveResultWithWeight.EMPTY_ARRAY;
				}
				ResolveState resolveState = ResolveState.initial();
				resolveState = resolveState.put(CSharpResolveUtil.EXTRACTOR_KEY, resolvedTypeRef.getGenericExtractor(psiElement1, element));

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
					val findFirstProcessor = new CommonProcessors.FindFirstProcessor<PsiElement>();

					StubIndex.getInstance().processElements(CSharpIndexKeys.NAMESPACE_BY_QNAME_INDEX, parent, element.getProject(),
							element.getResolveScope(), PsiElement.class, findFirstProcessor);

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
				CSharpCallArgumentListOwner parent = PsiTreeUtil.getParentOfType(element, CSharpCallArgumentListOwner.class);

				ResolveToKind typeResolveKind = ResolveToKind.TYPE_OR_GENERIC_PARAMETER_OR_DELEGATE_METHOD;
				PsiElement referenceElement = referenceExpression.getReferenceElement();
				assert referenceElement != null;
				if(referenceElement.getNode().getElementType() == CSharpTokens.BASE_KEYWORD)
				{
					typeResolveKind = ResolveToKind.BASE;
				}
				else if(referenceElement.getNode().getElementType() == CSharpTokens.THIS_KEYWORD)
				{
					typeResolveKind = ResolveToKind.THIS;
				}
				else if(parent instanceof DotNetAttribute)
				{
					typeResolveKind = ResolveToKind.ATTRIBUTE;
				}

				ResolveResultWithWeight[] resolveResult = referenceExpression.multiResolveImpl(typeResolveKind);
				if(resolveResult.length == 0)
				{
					return ResolveResultWithWeight.EMPTY_ARRAY;
				}
				ResolveResultWithWeight resolveResultWithWeight = resolveResult[0];
				if(!resolveResultWithWeight.isGoodResult())
				{
					return ResolveResultWithWeight.EMPTY_ARRAY;
				}


				val constructorProcessor = new ConstructorProcessor(completion ? null : parent);

				PsiElement resolveElement = resolveResultWithWeight.getElement();
				if(resolveElement instanceof DotNetConstructorListOwner)
				{
					((DotNetConstructorListOwner) resolveElement).processConstructors(constructorProcessor);
				}
				constructorProcessor.executeDefault((PsiNamedElement) resolveElement);
				return constructorProcessor.toResolveResults();
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
					/*else if(kind == ResolveToKind.ANY_MEMBER)
					{
						condition = Conditions.and(condition, Conditions.not(ourMethodCondition));
					}  */
					else if(kind == ResolveToKind.METHOD)
					{
						condition = Conditions.and(condition, Conditions.not(ourMethodDelegate));
					}
				}

				return processAnyMember(qualifier, condition, weightProcessor, element, kind, completion);
		}
		return ResolveResultWithWeight.EMPTY_ARRAY;
	}

	public static ResolveResultWithWeight[] processAnyMember(PsiElement qualifier, Condition<PsiNamedElement> condition,
			WeightProcessor<PsiNamedElement> weightProcessor, PsiElement element, ResolveToKind kind, boolean с)
	{
		PsiElement target = element;
		DotNetGenericExtractor extractor = DotNetGenericExtractor.EMPTY;

		DotNetTypeRef qualifierTypeRef = DotNetTypeRef.ERROR_TYPE;

		if(qualifier instanceof DotNetExpression)
		{
			qualifierTypeRef = ((DotNetExpression) qualifier).toTypeRef(false);

			PsiElement resolve = qualifierTypeRef.resolve(element);

			if(resolve != null)
			{
				target = resolve;
				extractor = qualifierTypeRef.getGenericExtractor(resolve, element);
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

			if(element instanceof CSharpReferenceExpression)
			{
				// walk for extensions
				ExtensionResolveScopeProcessor p2 = new ExtensionResolveScopeProcessor(qualifierTypeRef, (CSharpReferenceExpression) element,
					condition, !с);
				p2.merge(p);

				resolveState = ResolveState.initial();
				resolveState = resolveState.put(CSharpResolveUtil.EXTRACTOR_KEY, extractor);
				resolveState = resolveState.put(CSharpResolveUtil.CONTAINS_FILE_KEY, element.getContainingFile());

				CSharpResolveUtil.walkChildren(p2, targetToWalkChildren, true, null, resolveState);
				return p2.toResolveResults();
			}
			else
			{
				return p.toResolveResults();
			}
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
	public static Couple<PsiElement> getResolveLayers(PsiElement element, boolean strict)
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
			else if(temp instanceof CSharpAttribute)
			{
				last = temp;
				targetToWalkChildren = PsiTreeUtil.getParentOfType(temp, DotNetModifierListOwner.class);
			}
			else if(temp instanceof CSharpCallArgumentList)
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
				PsiElement scopeElement = ((CSharpCodeFragment) temp).getScopeElement();
				if(scopeElement == null)
				{
					break;
				}
				temp = scopeElement;
				continue;
			}
			temp = temp.getParent();
		}

		if(targetToWalkChildren == null)
		{
			return Couple.of(last, last);
			//LOGGER.error(element.getText() + " " + last + " " + kind + " " + element.getParent() + " " + element.getContainingFile().getName());
		}
		return Couple.of(last, targetToWalkChildren);
	}

	@NotNull
	private DotNetTypeRef resolveBaseTypeRef()
	{
		DotNetTypeDeclaration typeDeclaration = PsiTreeUtil.getParentOfType(this, DotNetTypeDeclaration.class);
		if(typeDeclaration == null)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}

		return CSharpTypeDeclarationImplUtil.resolveBaseTypeRef(typeDeclaration, this);
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

			return ResolveToKind.GENERIC_PARAMETER_FROM_PARENT;
		}
		else if(tempElement instanceof CSharpNamespaceDeclarationImpl)
		{
			return ResolveToKind.SOFT_NAMESPACE;
		}
		else if(tempElement instanceof DotNetUserType)
		{
			PsiElement parentOfParent = tempElement.getParent();
			if(parentOfParent instanceof DotNetTypeWithTypeArguments)
			{
				parentOfParent = parentOfParent.getParent();
			}

			if(parentOfParent instanceof CSharpCallArgumentListOwner && ((CSharpCallArgumentListOwner) parentOfParent).canResolve())
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
		else if(tempElement instanceof CSharpAttribute)
		{
			return ResolveToKind.CONSTRUCTOR;
		}
		else if(tempElement instanceof CSharpNamedCallArgument)
		{
			if(((CSharpNamedCallArgument) tempElement).getArgumentNameReference() == this)
			{
				return ResolveToKind.FIELD_OR_PROPERTY;
			}
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

			if(PsiTreeUtil.getParentOfType(this, DotNetUserType.class) != null)
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
		ResolveToKind kind = kind();
		if(kind != ResolveToKind.LABEL)
		{
			kind = ResolveToKind.ANY_MEMBER;
		}
		Condition<PsiNamedElement> condition = kind == ResolveToKind.LABEL ? Conditions.<PsiNamedElement>alwaysTrue() : new
				Condition<PsiNamedElement>()
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

				if(e instanceof DotNetVirtualImplementOwner && ((DotNetVirtualImplementOwner) e).getTypeRefForImplement() != DotNetTypeRef.ERROR_TYPE)
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
		};
		ResolveResult[] psiElements = collectResults(kind, condition, WeightProcessor.MAXIMUM, this, true);
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
			return new CSharpTypeRefFromNamespace(((CSharpNamespaceAsElement) resolve).getPresentableQName());
		}
		else if(resolve instanceof DotNetTypeDeclaration)
		{
			return new CSharpTypeRefFromQualifiedElement((DotNetTypeDeclaration) resolve);
		}
		else if(resolve instanceof CSharpTypeDefStatementImpl)
		{
			return ((CSharpTypeDefStatementImpl) resolve).toTypeRef();
		}
		else if(resolve instanceof DotNetGenericParameter)
		{
			return new CSharpTypeRefFromGenericParameter((DotNetGenericParameter) resolve);
		}
		else if(resolve instanceof CSharpPseudoMethod)
		{
			return new CSharpLambdaTypeRef((CSharpPseudoMethod) resolve);
		}
		else if(resolve instanceof DotNetVariable)
		{
			return ((DotNetVariable) resolve).toTypeRef(true);
		}
		return DotNetTypeRef.ERROR_TYPE;
	}
}
