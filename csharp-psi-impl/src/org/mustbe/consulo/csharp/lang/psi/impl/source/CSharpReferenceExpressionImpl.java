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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.consulo.lombok.annotations.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.CSharpLookupElementBuilder;
import org.mustbe.consulo.csharp.lang.psi.*;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.CSharpTransform;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.AbstractScopeProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.CompletionResolveScopeProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.ConstructorProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.ExecuteTarget;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.MemberResolveScopeProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.MethodAcceptorImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.SimpleNamedScopeProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.WeightProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefFromGenericParameter;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefFromNamespace;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.wrapper.GenericUnwrapTool;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.csharp.lang.psi.resolve.AttributeByNameSelector;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveSelector;
import org.mustbe.consulo.csharp.lang.psi.resolve.MemberByNameSelector;
import org.mustbe.consulo.csharp.lang.psi.resolve.StaticResolveSelectors;
import org.mustbe.consulo.dotnet.lang.psi.impl.BaseDotNetNamespaceAsElement;
import org.mustbe.consulo.dotnet.lang.psi.impl.stub.DotNetNamespaceStubUtil;
import org.mustbe.consulo.dotnet.psi.*;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetNamespaceAsElement;
import org.mustbe.consulo.dotnet.resolve.DotNetPsiSearcher;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Couple;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.CharFilter;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.ResolveState;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import lombok.val;

/**
 * @author VISTALL
 * @since 28.11.13.
 */
@Logger
public class CSharpReferenceExpressionImpl extends CSharpElementImpl implements CSharpReferenceExpression, PsiPolyVariantReference,
		CSharpQualifiedNonReference
{
	private static class OurResolver implements ResolveCache.PolyVariantResolver<CSharpReferenceExpressionImpl>
	{
		private static final OurResolver INSTANCE = new OurResolver();

		@NotNull
		@Override
		public ResolveResult[] resolve(@NotNull CSharpReferenceExpressionImpl ref, boolean incompleteCode)
		{
			if(!incompleteCode)
			{
				return ref.multiResolveImpl(ref.kind());
			}
			else
			{
				List<ResolveResult> filter = new SmartList<ResolveResult>();
				for(ResolveResult resolveResult : ref.multiResolve(true))
				{
					if(resolveResult.isValidResult())
					{
						filter.add(resolveResult);
					}
				}
				return ContainerUtil.toArray(filter, ResolveResult.EMPTY_ARRAY);
			}
		}
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

	@NotNull
	public ResolveResult[] multiResolveImpl(ResolveToKind kind)
	{
		CSharpCallArgumentListOwner p = null;
		PsiElement parent = getParent();
		if(parent instanceof CSharpCallArgumentListOwner)
		{
			p = (CSharpCallArgumentListOwner) parent;
		}
		return multiResolve0(kind, p, this);
	}

	public static <T extends CSharpQualifiedNonReference & PsiElement> ResolveResult[] multiResolve0(ResolveToKind kind,
			final CSharpCallArgumentListOwner callArgumentListOwner,
			final T element)
	{
		CSharpResolveSelector selector = StaticResolveSelectors.NONE;
		switch(kind)
		{
			case ATTRIBUTE:
				val referenceName = element.getReferenceName();
				if(referenceName == null)
				{
					return ResolveResult.EMPTY_ARRAY;
				}
				selector = new AttributeByNameSelector(referenceName);
				kind = ResolveToKind.TYPE_LIKE; //remap to type search
				break;
			case NATIVE_TYPE_WRAPPER:
			case THIS:
			case BASE:
				break;
			case ARRAY_METHOD:
				selector = StaticResolveSelectors.INDEX_METHOD_GROUP;
				break;
			case CONSTRUCTOR:
				selector = StaticResolveSelectors.CONSTRUCTOR_GROUP;
				break;
			default:
				val referenceName2 = element.getReferenceName();
				if(referenceName2 == null)
				{
					return ResolveResult.EMPTY_ARRAY;
				}
				val text2 = StringUtil.strip(referenceName2, CharFilter.NOT_WHITESPACE_FILTER);
				MemberByNameSelector selector2 = new MemberByNameSelector(text2);
				int expectGenericCount = findExpectGenericCount(element);
				selector2.putUserData(CSharpResolveContext.GENERIC_COUNT, expectGenericCount);
				selector = selector2;
				break;
		}

		return collectResults(kind, selector, element, callArgumentListOwner, false);
	}

	private static int findExpectGenericCount(@NotNull PsiElement element)
	{
		PsiElement parent = element.getParent();
		if(parent instanceof DotNetUserType)
		{
			PsiElement userTypeParent = parent.getParent();
			if(userTypeParent instanceof DotNetTypeWithTypeArguments)
			{
				return ((DotNetTypeWithTypeArguments) userTypeParent).getArguments().length;
			}
		}
		return 0;
	}

	public static <T extends CSharpQualifiedNonReference & PsiElement> ResolveResult[] collectResults(@NotNull ResolveToKind kind,
			@Nullable CSharpResolveSelector selector,
			final T element,
			CSharpCallArgumentListOwner callArgumentListOwner,
			final boolean completion)
	{
		if(!element.isValid())
		{
			return ResolveResult.EMPTY_ARRAY;
		}

		AbstractScopeProcessor scopeProcessor = null;
		ResolveState state = null;
		ResolveResult[] resolveResults = null;
		PsiElement qualifier = element.getQualifier();
		switch(kind)
		{
			case THIS:
				DotNetTypeDeclaration typeDeclaration = PsiTreeUtil.getParentOfType(element, DotNetTypeDeclaration.class);
				if(typeDeclaration != null)
				{
					return new ResolveResult[]{new PsiElementResolveResult(typeDeclaration, true)};
				}
				break;
			case BASE:
				DotNetTypeRef baseDotNetTypeRef = ((CSharpReferenceExpressionImpl) element).resolveBaseTypeRef();
				PsiElement baseElement = baseDotNetTypeRef.resolve(element).getElement();
				if(baseElement != null)
				{
					return new ResolveResult[]{new PsiElementResolveResult(baseElement, true)};
				}
				break;
			case GENERIC_PARAMETER_FROM_PARENT:
				DotNetGenericParameterListOwner parameterListOwner = PsiTreeUtil.getParentOfType(element, DotNetGenericParameterListOwner.class);
				if(parameterListOwner == null)
				{
					return ResolveResult.EMPTY_ARRAY;
				}

				scopeProcessor = new SimpleNamedScopeProcessor(ExecuteTarget.GENERIC_PARAMETER);
				state = ResolveState.initial();
				state = state.put(CSharpResolveUtil.SELECTOR, selector);

				parameterListOwner.processDeclarations(scopeProcessor, state, null, element);
				return scopeProcessor.toResolveResults();
			case NATIVE_TYPE_WRAPPER:
				PsiElement nativeElement = ((CSharpReferenceExpressionImpl) element).findChildByType(CSharpTokenSets.NATIVE_TYPES);
				assert nativeElement != null;
				String nativeRuntimeType = CSharpNativeTypeImpl.ourElementToQTypes.get(nativeElement.getNode().getElementType());
				if(nativeRuntimeType == null)
				{
					return ResolveResult.EMPTY_ARRAY;
				}
				PsiElement resolve = DotNetPsiSearcher.getInstance(element.getProject()).findType(nativeRuntimeType, element.getResolveScope(),
						DotNetPsiSearcher.TypeResoleKind.UNKNOWN, CSharpTransform.INSTANCE);
				if(resolve == null)
				{
					return ResolveResult.EMPTY_ARRAY;
				}

				return new ResolveResult[]{new PsiElementResolveResult(resolve, true)};
			case FIELD_OR_PROPERTY:
				DotNetTypeRef resolvedTypeRef;
				callArgumentListOwner = PsiTreeUtil.getParentOfType(element, CSharpCallArgumentListOwner.class);

				if(callArgumentListOwner instanceof CSharpNewExpression)
				{
					resolvedTypeRef = ((CSharpNewExpression) callArgumentListOwner).toTypeRef(false);
				}
				else if(callArgumentListOwner instanceof DotNetAttribute)
				{
					resolvedTypeRef = ((DotNetAttribute) callArgumentListOwner).toTypeRef();
				}
				else
				{
					throw new IllegalArgumentException(callArgumentListOwner == null ? "null" : callArgumentListOwner.getClass().getName());
				}

				if(resolvedTypeRef == DotNetTypeRef.ERROR_TYPE)
				{
					return ResolveResult.EMPTY_ARRAY;
				}

				DotNetTypeResolveResult typeResolveResult = resolvedTypeRef.resolve(element);

				PsiElement typeElement = typeResolveResult.getElement();
				if(typeElement == null)
				{
					return ResolveResult.EMPTY_ARRAY;
				}

				DotNetGenericExtractor genericExtractor = typeResolveResult.getGenericExtractor();

				if(selector != null)
				{
					scopeProcessor = createMemberProcessor(element, kind, ResolveResult.EMPTY_ARRAY, completion);

					state = ResolveState.initial();
					state = state.put(CSharpResolveUtil.EXTRACTOR, genericExtractor);
					state = state.put(CSharpResolveUtil.SELECTOR, selector);
					CSharpResolveUtil.walkChildren(scopeProcessor, typeElement, true, null, state);

					return scopeProcessor.toResolveResults();
				}
				break;
			case LABEL:
				DotNetQualifiedElement parentOfType = PsiTreeUtil.getParentOfType(element, DotNetQualifiedElement.class);
				assert parentOfType != null;
				scopeProcessor = new SimpleNamedScopeProcessor(ExecuteTarget.LABEL);
				CSharpResolveUtil.treeWalkUp(scopeProcessor, element, element, parentOfType);
				return scopeProcessor.toResolveResults();
			case TYPE_OR_NAMESPACE:
				ResolveResult[] typeResults = collectResults(ResolveToKind.TYPE_LIKE, selector, element, callArgumentListOwner, completion);
				ResolveResult[] namespaceResults = collectResults(ResolveToKind.NAMESPACE, selector, element, callArgumentListOwner, completion);
				return ArrayUtil.mergeArrays(typeResults, namespaceResults);
			case NAMESPACE:
			case SOFT_NAMESPACE:
				String qName = StringUtil.strip(element.getText(), CharFilter.NOT_WHITESPACE_FILTER);

				DotNetNamespaceAsElement namespace = null;

				if(!completion)
				{
					namespace = DotNetPsiSearcher.getInstance(element.getProject()).findNamespace(qName, element.getResolveScope());

					if(namespace == null)
					{
						return ResolveResult.EMPTY_ARRAY;
					}
					return new ResolveResult[]{new PsiElementResolveResult(namespace, true)};
				}
				else
				{
					String qualifiedText = "";
					if(qualifier != null)
					{
						qualifiedText = StringUtil.strip(qualifier.getText(), CharFilter.NOT_WHITESPACE_FILTER);
					}

					namespace = DotNetPsiSearcher.getInstance(element.getProject()).findNamespace(qualifiedText, element.getResolveScope());

					if(namespace == null)
					{
						return ResolveResult.EMPTY_ARRAY;
					}

					scopeProcessor = new AbstractScopeProcessor()
					{
						@Override
						public boolean executeImpl(@NotNull PsiElement element, ResolveState state)
						{
							if(element instanceof DotNetNamespaceAsElement)
							{
								if(StringUtil.equals(((DotNetNamespaceAsElement) element).getPresentableQName(),
										DotNetNamespaceStubUtil.ROOT_FOR_INDEXING))
								{
									return true;
								}
								addElement(element);
							}
							return true;
						}
					};

					state = ResolveState.initial();
					state = state.put(BaseDotNetNamespaceAsElement.FILTER, DotNetNamespaceAsElement.ChildrenFilter.NONE);
					state = state.put(BaseDotNetNamespaceAsElement.RESOLVE_SCOPE, element.getResolveScope());
					namespace.processDeclarations(scopeProcessor, state, null, element);
					return scopeProcessor.toResolveResults();
				}
			case CONSTRUCTOR:
				CSharpReferenceExpressionImpl referenceExpression = (CSharpReferenceExpressionImpl) element;
				CSharpCallArgumentListOwner parent = PsiTreeUtil.getParentOfType(element, CSharpCallArgumentListOwner.class);

				ResolveToKind typeResolveKind = ResolveToKind.TYPE_LIKE;
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

				ResolveResult[] resolveResult = referenceExpression.multiResolveImpl(typeResolveKind);
				if(resolveResult.length == 0)
				{
					return ResolveResult.EMPTY_ARRAY;
				}
				ResolveResult r = resolveResult[0];
				if(!r.isValidResult())
				{
					return ResolveResult.EMPTY_ARRAY;
				}

				val constructorProcessor = new ConstructorProcessor(completion ? null : parent);

				PsiElement resolveElement = r.getElement();
				if(resolveElement instanceof DotNetConstructorListOwner)
				{
					((DotNetConstructorListOwner) resolveElement).processConstructors(constructorProcessor);
				}
				constructorProcessor.executeDefault((PsiNamedElement) resolveElement);
				return constructorProcessor.toResolveResults();
			case PARAMETER:
				callArgumentListOwner = PsiTreeUtil.getParentOfType(element, CSharpCallArgumentListOwner.class);
				if(callArgumentListOwner == null)
				{
					return ResolveResult.EMPTY_ARRAY;
				}
				resolveResults = callArgumentListOwner.multiResolve(false);
				ResolveResult firstItem = ArrayUtil.getFirstElement(resolveResults);
				if(firstItem == null)
				{
					return ResolveResult.EMPTY_ARRAY;
				}

				PsiElement maybeParameterListOwner = firstItem.getElement();
				if(!(maybeParameterListOwner instanceof DotNetParameterListOwner))
				{
					return ResolveResult.EMPTY_ARRAY;
				}
				state = ResolveState.initial();
				state = state.put(CSharpResolveUtil.SELECTOR, selector);

				scopeProcessor = new SimpleNamedScopeProcessor(ExecuteTarget.LOCAL_VARIABLE_OR_PARAMETER);
				maybeParameterListOwner.processDeclarations(scopeProcessor, state, null, element);
				return scopeProcessor.toResolveResults();
			case TYPE_LIKE:
			case ANY_MEMBER:
				return processAnyMember(qualifier, selector, element, kind, completion);
			case METHOD:
			case ARRAY_METHOD:
				resolveResults = processAnyMember(qualifier, selector, element, kind, completion);
				if(callArgumentListOwner == null || resolveResults.length == 0)
				{
					return resolveResults;
				}

				DotNetTypeRef[] typeArgumentListRefs = callArgumentListOwner.getTypeArgumentListRefs();

				List<Pair<Integer, DotNetLikeMethodDeclaration>> list = new ArrayList<Pair<Integer, DotNetLikeMethodDeclaration>>();
				for(ResolveResult result : resolveResults)
				{
					PsiElement maybeElementGroup = result.getElement();
					if(maybeElementGroup instanceof CSharpElementGroup)
					{
						for(PsiElement psiElement : ((CSharpElementGroup) maybeElementGroup).getElements())
						{
							if(psiElement instanceof DotNetLikeMethodDeclaration)
							{
								DotNetGenericExtractor extractorFromCall = MethodAcceptorImpl.createExtractorFromCall(typeArgumentListRefs,
										(DotNetGenericParameterListOwner) psiElement);

								psiElement = GenericUnwrapTool.extract((DotNetNamedElement) psiElement, extractorFromCall, true);

								int i = MethodAcceptorImpl.calcAcceptableWeight(element, callArgumentListOwner,
										(DotNetLikeMethodDeclaration) psiElement);

								list.add(Pair.create(i, (DotNetLikeMethodDeclaration) psiElement));
							}
						}
					}
				}

				Collections.sort(list, new Comparator<Pair<Integer, DotNetLikeMethodDeclaration>>()
				{
					@Override
					public int compare(Pair<Integer, DotNetLikeMethodDeclaration> o1, Pair<Integer, DotNetLikeMethodDeclaration> o2)
					{
						return o2.getFirst() - o1.getFirst();
					}
				});
				resolveResults = new ResolveResult[list.size()];
				int i = 0;
				for(Pair<Integer, DotNetLikeMethodDeclaration> pair : list)
				{
					resolveResults[i++] = new PsiElementResolveResult(pair.getSecond(), pair.getFirst() == WeightProcessor.MAX_WEIGHT);
				}
				return resolveResults;
		}
		return ResolveResult.EMPTY_ARRAY;
	}

	public static ResolveResult[] processAnyMember(@Nullable PsiElement qualifier,
			@Nullable CSharpResolveSelector selector,
			@NotNull PsiElement element,
			ResolveToKind kind,
			boolean completion)
	{
		CSharpCodeFragment codeFragment = PsiTreeUtil.getParentOfType(element, CSharpCodeFragment.class);
		if(codeFragment != null)
		{
			element = codeFragment.getScopeElement();
		}

		PsiElement target = element;
		DotNetGenericExtractor extractor = DotNetGenericExtractor.EMPTY;
		DotNetTypeRef qualifierTypeRef;

		if(qualifier instanceof DotNetExpression)
		{
			qualifierTypeRef = ((DotNetExpression) qualifier).toTypeRef(false);

			DotNetTypeResolveResult typeResolveResult = qualifierTypeRef.resolve(element);

			PsiElement resolve = typeResolveResult.getElement();

			if(resolve != null)
			{
				target = resolve;
				extractor = typeResolveResult.getGenericExtractor();
			}
			else
			{
				return ResolveResult.EMPTY_ARRAY;
			}
		}

		if(!target.isValid())
		{
			return ResolveResult.EMPTY_ARRAY;
		}

		ResolveState resolveState = ResolveState.initial();
		resolveState = resolveState.put(CSharpResolveUtil.EXTRACTOR, extractor);
		if(selector != null)
		{
			resolveState = resolveState.put(CSharpResolveUtil.SELECTOR, selector);
		}

		if(target != element)
		{
			AbstractScopeProcessor memberProcessor = createMemberProcessor(element, kind, ResolveResult.EMPTY_ARRAY, completion);

			if(!CSharpResolveUtil.walkChildren(memberProcessor, target, false, null, resolveState))
			{
				return memberProcessor.toResolveResults();
			}

			return memberProcessor.toResolveResults();
		/*	Couple<PsiElement> resolveLayers = getResolveLayers(element, false);

			PsiElement targetToWalkChildren = resolveLayers.getSecond();
			if(targetToWalkChildren == null)
			{
				return memberProcessor.toResolveResults();
			}

			if(element instanceof CSharpReferenceExpression)
			{
				// walk for extensions
				ExtensionResolveScopeProcessor p2 = new ExtensionResolveScopeProcessor(qualifierTypeRef, (CSharpReferenceExpression) element,
						condition, !с);
				p2.merge(memberProcessor);

				resolveState = ResolveState.initial();
				resolveState = resolveState.put(CSharpResolveUtil.EXTRACTOR, extractor);
				resolveState = resolveState.put(CSharpResolveUtil.CONTAINS_FILE, element.getContainingFile());

				CSharpResolveUtil.walkChildren(p2, targetToWalkChildren, true, null, resolveState);
				return p2.toResolveResults();
			}
			else
			{
				return memberProcessor.toResolveResults();
			}*/
		}
		else
		{
			Couple<PsiElement> resolveLayers = getResolveLayers(element, false);

			PsiElement last = resolveLayers.getFirst();
			PsiElement targetToWalkChildren = resolveLayers.getSecond();

			ResolveResult[] elements = ResolveResult.EMPTY_ARRAY;
			// if resolving is any member, first we need process locals and then go to fields and other
			if(kind == ResolveToKind.ANY_MEMBER)
			{
				SimpleNamedScopeProcessor localProcessor = new SimpleNamedScopeProcessor(completion, ExecuteTarget.LOCAL_VARIABLE_OR_PARAMETER);
				if(!CSharpResolveUtil.treeWalkUp(localProcessor, target, element, last, resolveState))
				{
					return localProcessor.toResolveResults();
				}
				elements = localProcessor.toResolveResults();
			}

			AbstractScopeProcessor p = createMemberProcessor(element, kind, elements, completion);
			if(last == null)
			{
				return p.toResolveResults();
			}

			if(!CSharpResolveUtil.walkChildren(p, targetToWalkChildren, true, null, resolveState))
			{
				return p.toResolveResults();
			}

			/*if(!CSharpResolveUtil.walkGenericParameterList(p, targetToWalkChildren, null, resolveState))
			{
				return p.toResolveResults();
			} */

			if(PsiTreeUtil.getParentOfType(element, CSharpUsingList.class) == null)
			{
				CSharpResolveUtil.walkUsing(p, target, null, resolveState);
			}

			return p.toResolveResults();
		}
	}

	private static AbstractScopeProcessor createMemberProcessor(@NotNull PsiElement element,
			ResolveToKind kind,
			ResolveResult[] elements,
			boolean completion)
	{
		ExecuteTarget[] targets;
		switch(kind)
		{
			case TYPE_LIKE:
				targets = new ExecuteTarget[]{
						ExecuteTarget.GENERIC_PARAMETER,
						ExecuteTarget.TYPE,
						ExecuteTarget.DELEGATE_METHOD,
						ExecuteTarget.TYPE_DEF
				};
				break;
			case NAMESPACE:
				targets = new ExecuteTarget[]{ExecuteTarget.NAMESPACE};
				break;
			case FIELD_OR_PROPERTY:
				targets = new ExecuteTarget[]{
						ExecuteTarget.FIELD,
						ExecuteTarget.PROPERTY
				};
				break;
			case ARRAY_METHOD:
			case METHOD:
				targets = new ExecuteTarget[]{ExecuteTarget.ELEMENT_GROUP};
				break;
			default:
				targets = new ExecuteTarget[]{
						ExecuteTarget.MEMBER,
						ExecuteTarget.TYPE_DEF
				};
				break;
		}

		return completion ? new CompletionResolveScopeProcessor(element.getResolveScope(), elements, targets) : new MemberResolveScopeProcessor
				(element.getResolveScope(), elements, targets);
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
			else if(temp instanceof DotNetFieldDeclaration ||
					temp instanceof DotNetPropertyDeclaration ||
					temp instanceof DotNetEventDeclaration ||
					temp instanceof DotNetLikeMethodDeclaration)
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
			else if(temp instanceof DotNetTypeDeclaration)
			{
				last = temp;
				targetToWalkChildren = temp.getParent();
				break;
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
		ResolveResult resolveResult = resolveResults[0];
		if(!resolveResult.isValidResult())
		{
			return null;
		}
		return resolveResult.getElement();
	}

	@Override
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
			return ResolveToKind.TYPE_LIKE;
		}
		else if(tempElement instanceof CSharpUsingNamespaceStatement)
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
				return ResolveToKind.PARAMETER;
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
				return ResolveToKind.TYPE_OR_NAMESPACE;
			}

			if(PsiTreeUtil.getParentOfType(this, CSharpAttribute.class) != null)
			{
				return ResolveToKind.TYPE_OR_NAMESPACE;
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
		if(element instanceof DotNetNamespaceAsElement && resolve instanceof DotNetNamespaceAsElement)
		{
			return Comparing.equal(((DotNetNamespaceAsElement) resolve).getPresentableQName(), ((DotNetNamespaceAsElement) element)
					.getPresentableQName());
		}
		return element.getManager().areElementsEquivalent(element, resolve);
	}

	@NotNull
	@Override
	public Object[] getVariants()
	{
		ResolveToKind kind = kind();
		if(kind != ResolveToKind.LABEL && kind != ResolveToKind.NAMESPACE && kind != ResolveToKind.SOFT_NAMESPACE)
		{
			kind = ResolveToKind.ANY_MEMBER;
		}
		ResolveResult[] psiElements = collectResults(kind, null, this, null, true);
		return CSharpLookupElementBuilder.getInstance(getProject()).buildToLookupElements(this, psiElements);
	}

	@Override
	public boolean isSoft()
	{
		return kind() == ResolveToKind.SOFT_NAMESPACE;
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

		ResolveResult[] resolveResults = multiResolveImpl(resolveToKind);
		if(resolveResults.length == 0)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}

		ResolveResult resolveResult = resolveResults[0];
		if(!resolveResult.isValidResult())
		{
			return DotNetTypeRef.ERROR_TYPE;
		}
		return toTypeRef(resolveResult.getElement());
	}

	@NotNull
	public static DotNetTypeRef toTypeRef(@Nullable PsiElement resolve)
	{
		if(resolve instanceof DotNetNamespaceAsElement)
		{
			return new CSharpTypeRefFromNamespace(((DotNetNamespaceAsElement) resolve).getPresentableQName());
		}
		else if(resolve instanceof DotNetTypeDeclaration)
		{
			return new CSharpTypeRefByTypeDeclaration((DotNetTypeDeclaration) resolve);
		}
		else if(resolve instanceof CSharpTypeDefStatement)
		{
			return ((CSharpTypeDefStatement) resolve).toTypeRef();
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
