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

import static org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression.ResolveToKind;

import gnu.trove.THashMap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.doc.psi.CSharpDocRoot;
import org.mustbe.consulo.csharp.lang.psi.*;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.CSharpTransform;
import org.mustbe.consulo.csharp.lang.psi.impl.source.injection.CSharpForInjectionFragmentHolder;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.AbstractScopeProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.CSharpResolveOptions;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.CompletionResolveScopeProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.ExecuteTarget;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.MemberResolveScopeProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.MethodResolveResult;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.PsiElementResolveResultWithExtractor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.SimpleNamedScopeProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.WeightUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.extensionResolver.ExtensionResolveScopeProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.genericInference.GenericInferenceUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.MethodCalcResult;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.MethodResolver;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.arguments.NCallArgument;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.sorter.TypeLikeComparator;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.sorter.VariableOrTypeComparator;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpElementGroupTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpGenericExtractor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpGenericWrapperTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaResolveResult;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefFromGenericParameter;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefFromNamespace;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.wrapper.GenericUnwrapTool;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.csharp.lang.psi.resolve.AttributeByNameSelector;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpNamedResolveSelector;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveSelector;
import org.mustbe.consulo.csharp.lang.psi.resolve.ExtensionMethodByNameSelector;
import org.mustbe.consulo.csharp.lang.psi.resolve.MemberByNameSelector;
import org.mustbe.consulo.csharp.lang.psi.resolve.StaticResolveSelectors;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.lang.psi.impl.BaseDotNetNamespaceAsElement;
import org.mustbe.consulo.dotnet.lang.psi.impl.stub.DotNetNamespaceStubUtil;
import org.mustbe.consulo.dotnet.psi.*;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetNamespaceAsElement;
import org.mustbe.consulo.dotnet.resolve.DotNetPointerTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetPsiSearcher;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import org.mustbe.consulo.dotnet.util.ArrayUtil2;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.util.Couple;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.CharFilter;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.ResolveState;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import lombok.val;

/**
 * @author VISTALL
 * @since 17.06.14
 */
public class CSharpReferenceExpressionImplUtil
{
	public static final TokenSet ourReferenceElements = TokenSet.orSet(CSharpTokenSets.NATIVE_TYPES, TokenSet.create(CSharpTokens.THIS_KEYWORD,
			CSharpTokens.BASE_KEYWORD, CSharpTokens.IDENTIFIER, CSharpSoftTokens.GLOBAL_KEYWORD));

	public static final TokenSet ourAccessTokens = TokenSet.create(CSharpTokens.ARROW, CSharpTokens.DOT, CSharpTokens.COLONCOLON,
			CSharpTokens.NULLABE_CALL);

	public static boolean isConstructorKind(ResolveToKind kind)
	{
		switch(kind)
		{
			case CONSTRUCTOR:
			case BASE_CONSTRUCTOR:
			case THIS_CONSTRUCTOR:
				return true;
			default:
				return false;
		}
	}

	@Nullable
	public static CSharpCallArgumentListOwner findCallArgumentListOwner(ResolveToKind kind, CSharpReferenceExpression referenceExpression)
	{
		PsiElement parent = referenceExpression.getParent();

		CSharpCallArgumentListOwner p = null;
		if(CSharpReferenceExpressionImplUtil.isConstructorKind(kind) || kind == ResolveToKind.PARAMETER)
		{
			p = PsiTreeUtil.getParentOfType(referenceExpression, CSharpCallArgumentListOwner.class);
		}
		else if(parent instanceof CSharpCallArgumentListOwner)
		{
			p = (CSharpCallArgumentListOwner) parent;
		}
		return p;
	}

	@NotNull
	public static DotNetTypeRef toTypeRef(@NotNull CSharpReferenceExpressionEx referenceExpressionEx, boolean resolveFromParent)
	{
		ResolveResult[] resolveResults = referenceExpressionEx.multiResolve(false, resolveFromParent);
		if(resolveResults.length == 0)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}

		ResolveResult resolveResult = CSharpResolveUtil.findFirstValidResult(resolveResults);
		if(resolveResult == null)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}

		CSharpReferenceExpression.AccessType memberAccessType = referenceExpressionEx.getMemberAccessType();

		DotNetTypeRef typeRef = CSharpReferenceExpressionImplUtil.toTypeRef(resolveResult);
		switch(memberAccessType)
		{
			case NULLABLE_CALL:
				if(typeRef.resolve(referenceExpressionEx).isNullable())
				{
					return typeRef;
				}
				else
				{
					return new CSharpGenericWrapperTypeRef(new CSharpTypeRefByQName(DotNetTypes.System.Nullable$1), typeRef);
				}
			default:

				return typeRef;
		}
	}

	@NotNull
	public static DotNetTypeRef toTypeRefWithoutCaching(@NotNull CSharpReferenceExpressionEx referenceExpressionEx,
			@NotNull ResolveToKind kind,
			boolean resolveFromParent)
	{
		ResolveResult[] resolveResults = referenceExpressionEx.multiResolveImpl(kind, resolveFromParent);
		if(resolveResults.length == 0)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}

		ResolveResult firstValidResult = CSharpResolveUtil.findFirstValidResult(resolveResults);
		if(firstValidResult == null)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}
		return CSharpReferenceExpressionImplUtil.toTypeRef(firstValidResult);
	}

	public static int getTypeArgumentListSize(@NotNull CSharpReferenceExpression referenceExpression)
	{
		DotNetTypeList typeArgumentList = referenceExpression.getTypeArgumentList();
		if(typeArgumentList == null)
		{
			return 0;
		}
		return typeArgumentList.getTypesCount();
	}

	public static TextRange getRangeInElement(@NotNull CSharpReferenceExpression referenceExpression)
	{
		PsiElement referenceElement = referenceExpression.getReferenceElement();
		if(referenceElement == null)
		{
			return TextRange.EMPTY_RANGE;
		}

		PsiElement qualifier = referenceExpression.getQualifier();
		int startOffset = qualifier != null ? qualifier.getTextLength() : 0;
		CSharpReferenceExpression.AccessType accessType = referenceExpression.getMemberAccessType();
		switch(accessType)
		{
			case ARROW:
			case COLONCOLON:
			case NULLABLE_CALL:
				startOffset += 2;
				break;
			case DOT:
				startOffset += 1;
				break;
		}
		return new TextRange(startOffset, referenceElement.getTextLength() + startOffset);
	}

	@NotNull
	public static ResolveToKind kind(@NotNull CSharpReferenceExpression referenceExpression)
	{
		if(referenceExpression.isGlobalElement())
		{
			return ResolveToKind.ROOT_NAMESPACE;
		}
		PsiElement tempElement = referenceExpression.getParent();
		if(tempElement instanceof CSharpGenericConstraintImpl)
		{
			DotNetGenericParameterListOwner parameterListOwner = PsiTreeUtil.getParentOfType(referenceExpression,
					DotNetGenericParameterListOwner.class);
			if(parameterListOwner == null)
			{
				return ResolveToKind.ANY_MEMBER;
			}

			return ResolveToKind.GENERIC_PARAMETER_FROM_PARENT;
		}
		else if(tempElement instanceof CSharpNamespaceDeclarationImpl)
		{
			return ResolveToKind.SOFT_QUALIFIED_NAMESPACE;
		}
		else if(tempElement instanceof CSharpNameOfExpressionImpl)
		{
			return ResolveToKind.NAMEOF;
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
			return ResolveToKind.QUALIFIED_NAMESPACE;
		}
		else if(tempElement instanceof CSharpConstructorSuperCallImpl)
		{
			CSharpReferenceExpression expression = ((CSharpConstructorSuperCallImpl) tempElement).getExpression();
			PsiElement referenceElement = expression.getReferenceElement();
			if(referenceElement == null)
			{
				return ResolveToKind.CONSTRUCTOR;
			}
			IElementType elementType = referenceElement.getNode().getElementType();
			if(elementType == CSharpTokens.BASE_KEYWORD)
			{
				return ResolveToKind.BASE_CONSTRUCTOR;
			}
			else if(elementType == CSharpTokens.THIS_KEYWORD)
			{
				return ResolveToKind.THIS_CONSTRUCTOR;
			}
			return ResolveToKind.CONSTRUCTOR;
		}
		else if(tempElement instanceof CSharpAttribute)
		{
			return ResolveToKind.CONSTRUCTOR;
		}
		else if(tempElement instanceof CSharpNamedCallArgument)
		{
			if(((CSharpNamedCallArgument) tempElement).getArgumentNameReference() == referenceExpression)
			{
				return ResolveToKind.PARAMETER;
			}
		}
		else if(tempElement instanceof CSharpFieldOrPropertySet)
		{
			if(((CSharpFieldOrPropertySet) tempElement).getNameReferenceExpression() == referenceExpression)
			{
				return ResolveToKind.FIELD_OR_PROPERTY;
			}
		}
		else if(tempElement instanceof CSharpReferenceExpression)
		{
			CSharpNamespaceDeclarationImpl netNamespaceDeclaration = PsiTreeUtil.getParentOfType(referenceExpression,
					CSharpNamespaceDeclarationImpl.class);
			if(netNamespaceDeclaration != null)
			{
				DotNetReferenceExpression namespaceReference = netNamespaceDeclaration.getNamespaceReference();
				if(namespaceReference != null && PsiTreeUtil.isAncestor(namespaceReference, referenceExpression, false))
				{
					return ResolveToKind.SOFT_QUALIFIED_NAMESPACE;
				}
			}

			if(PsiTreeUtil.getParentOfType(referenceExpression, CSharpUsingNamespaceStatementImpl.class) != null)
			{
				return ResolveToKind.QUALIFIED_NAMESPACE;
			}

			if(PsiTreeUtil.getParentOfType(referenceExpression, CSharpUserType.class) != null)
			{
				return ResolveToKind.TYPE_LIKE;
			}

			if(PsiTreeUtil.getParentOfType(referenceExpression, CSharpAttribute.class) != null)
			{
				return ResolveToKind.TYPE_LIKE;
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
		else if(tempElement instanceof CSharpForInjectionFragmentHolder)
		{
			return ((CSharpForInjectionFragmentHolder) tempElement).getKind();
		}

		tempElement = referenceExpression.getReferenceElement();
		ASTNode node = tempElement == null ? null : tempElement.getNode();
		if(node == null)
		{
			return ResolveToKind.ANY_MEMBER;
		}

		IElementType elementType = node.getElementType();
		if(CSharpTokenSets.NATIVE_TYPES.contains(elementType))
		{
			return ResolveToKind.NATIVE_TYPE_WRAPPER;
		}
		else if(elementType == CSharpTokens.THIS_KEYWORD)
		{
			return ResolveToKind.THIS;
		}
		else if(elementType == CSharpTokens.BASE_KEYWORD)
		{
			return ResolveToKind.BASE;
		}
		return ResolveToKind.ANY_MEMBER;
	}

	@Nullable
	public static PsiElement resolveByTypeKind(@NotNull DotNetReferenceExpression referenceExpression, boolean attributeSuffix)
	{
		assert referenceExpression instanceof CSharpReferenceExpressionEx;
		ResolveToKind kind = ResolveToKind.TYPE_LIKE;
		if(attributeSuffix)
		{
			kind = ResolveToKind.ATTRIBUTE;
		}
		ResolveResult[] resultWithWeights = ((CSharpReferenceExpressionEx) referenceExpression).multiResolveImpl(kind, true);
		if(resultWithWeights.length == 0)
		{
			return null;
		}
		return resultWithWeights[0].getElement();
	}

	@NotNull
	public static ResolveResult[] multiResolveImpl(ResolveToKind kind,
			final CSharpCallArgumentListOwner callArgumentListOwner,
			final CSharpQualifiedNonReference element,
			boolean resolveFromParent)
	{
		ResolveResult[] resolveResults = buildSelectorAndMultiResolve(kind, callArgumentListOwner, element, resolveFromParent);
		if(element instanceof CSharpReferenceExpression)
		{
			int typeArgumentListSize = getTypeArgumentListSize((CSharpReferenceExpression) element);
			if(typeArgumentListSize > 0)
			{
				DotNetTypeRef[] typeArgumentListRefs = ((CSharpReferenceExpression) element).getTypeArgumentListRefs();

				for(int i = 0; i < resolveResults.length; i++)
				{
					ResolveResult resolveResult = resolveResults[i];

					PsiElement resolveResultElement = resolveResult.getElement();
					if(resolveResultElement instanceof CSharpTypeDeclaration)
					{
						val map = new HashMap<DotNetGenericParameter, DotNetTypeRef>();
						DotNetGenericParameter[] genericParameters = ((CSharpTypeDeclaration) resolveResultElement).getGenericParameters();
						for(int j = 0; j < typeArgumentListRefs.length; j++)
						{
							DotNetTypeRef typeArgumentListRef = typeArgumentListRefs[j];
							DotNetGenericParameter genericParameter = ArrayUtil2.safeGet(genericParameters, j);
							if(genericParameter == null)
							{
								continue;
							}
							map.put(genericParameter, typeArgumentListRef);
						}
						resolveResults[i] = new PsiElementResolveResultWithExtractor(resolveResultElement, CSharpGenericExtractor.create(map));
					}
				}
			}
		}
		return resolveResults;
	}

	public static ResolveResult[] buildSelectorAndMultiResolve(@NotNull ResolveToKind kind,
			@Nullable final CSharpCallArgumentListOwner callArgumentListOwner,
			@NotNull final CSharpQualifiedNonReference element,
			boolean resolveFromParent)
	{
		return buildSelectorAndMultiResolve(kind, callArgumentListOwner, element, null, resolveFromParent);
	}

	public static ResolveResult[] buildSelectorAndMultiResolve(@NotNull ResolveToKind kind,
			@Nullable final CSharpCallArgumentListOwner callArgumentListOwner,
			@NotNull final CSharpQualifiedNonReference element,
			@Nullable final PsiElement forceQualifierElement,
			boolean resolveFromParent)
	{
		CSharpResolveSelector selector = StaticResolveSelectors.NONE;
		switch(kind)
		{
			case NATIVE_TYPE_WRAPPER:
			case THIS:
			case BASE:
			case ROOT_NAMESPACE:
				break;
			case ARRAY_METHOD:
				selector = StaticResolveSelectors.INDEX_METHOD_GROUP;
				break;
			case CONSTRUCTOR:
			case BASE_CONSTRUCTOR:
			case THIS_CONSTRUCTOR:
				selector = StaticResolveSelectors.CONSTRUCTOR_GROUP;
				break;
			case TYPE_LIKE:
				if(element instanceof CSharpReferenceExpression && ((CSharpReferenceExpression) element).isGlobalElement())
				{
					kind = ResolveToKind.ROOT_NAMESPACE;
					break;
				}
			default:
			case ATTRIBUTE:
				val referenceName = element.getReferenceName();
				if(referenceName == null)
				{
					return ResolveResult.EMPTY_ARRAY;
				}

				if(kind == ResolveToKind.ATTRIBUTE)
				{
					String referenceNameWithAt = element.getReferenceNameWithAt();
					assert referenceNameWithAt != null;
					selector = new AttributeByNameSelector(referenceNameWithAt);
					kind = ResolveToKind.TYPE_LIKE; //remap to type search
				}
				else
				{
					selector = new MemberByNameSelector(referenceName);
				}
				break;
		}

		return collectResults(new CSharpResolveOptions(kind, selector, element, callArgumentListOwner, false, resolveFromParent),
				DotNetGenericExtractor.EMPTY, forceQualifierElement);
	}

	public static ResolveResult[] collectResults(@NotNull CSharpResolveOptions options)
	{
		return collectResults(options, DotNetGenericExtractor.EMPTY, options.getElement());
	}

	public static ResolveResult[] collectResults(@NotNull CSharpResolveOptions options,
			@NotNull DotNetGenericExtractor defaultExtractor,
			@Nullable PsiElement forceQualifierElement)
	{
		PsiElement element = options.getElement();
		ResolveToKind kind = options.getKind();
		CSharpCallArgumentListOwner callArgumentListOwner = options.getCallArgumentListOwner();
		CSharpResolveSelector selector = options.getSelector();
		boolean completion = options.isCompletion();
		boolean resolveFromParent = options.isResolveFromParent();

		if(!element.isValid())
		{
			return ResolveResult.EMPTY_ARRAY;
		}

		AbstractScopeProcessor scopeProcessor = null;
		ResolveState state = null;
		ResolveResult[] resolveResults = null;
		PsiElement qualifier = options.getQualifier();
		List<Pair<MethodCalcResult, PsiElement>> methodResolveResults = null;
		DotNetTypeDeclaration thisTypeDeclaration = null;
		switch(kind)
		{
			case THIS:
				thisTypeDeclaration = PsiTreeUtil.getParentOfType(element, DotNetTypeDeclaration.class);
				if(thisTypeDeclaration != null)
				{
					DotNetGenericExtractor genericExtractor = DotNetGenericExtractor.EMPTY;
					int genericParametersCount = thisTypeDeclaration.getGenericParametersCount();
					if(genericParametersCount > 0)
					{
						val map = new THashMap<DotNetGenericParameter, DotNetTypeRef>(genericParametersCount);
						for(DotNetGenericParameter genericParameter : thisTypeDeclaration.getGenericParameters())
						{
							map.put(genericParameter, new CSharpTypeRefFromGenericParameter(genericParameter));
						}
						genericExtractor = CSharpGenericExtractor.create(map);
					}
					return new ResolveResult[]{new PsiElementResolveResultWithExtractor(thisTypeDeclaration, genericExtractor, true)};
				}
				break;
			case BASE:
				thisTypeDeclaration = PsiTreeUtil.getParentOfType(element, DotNetTypeDeclaration.class);
				if(thisTypeDeclaration != null)
				{
					val pair = CSharpTypeDeclarationImplUtil.resolveBaseType(thisTypeDeclaration, element);
					if(pair != null)
					{
						return new ResolveResult[]{new PsiElementResolveResultWithExtractor(pair.getFirst(), pair.getSecond(), true)};
					}
				}
				break;
			case ROOT_NAMESPACE:
				val temp = DotNetPsiSearcher.getInstance(element.getProject()).findNamespace("", element.getResolveScope());
				assert temp != null;
				return new ResolveResult[]{new PsiElementResolveResult(temp, true)};
			case GENERIC_PARAMETER_FROM_PARENT:
				DotNetGenericParameterListOwner genericParameterListOwner = findParentOrNextIfDoc(element, DotNetGenericParameterListOwner.class);
				if(genericParameterListOwner == null)
				{
					return ResolveResult.EMPTY_ARRAY;
				}

				scopeProcessor = new SimpleNamedScopeProcessor(completion, ExecuteTarget.GENERIC_PARAMETER);
				state = ResolveState.initial();
				state = state.put(CSharpResolveUtil.SELECTOR, selector);

				genericParameterListOwner.processDeclarations(scopeProcessor, state, null, element);
				return scopeProcessor.toResolveResults();
			case PARAMETER_FROM_PARENT:
				DotNetParameterListOwner parameterListOwner = findParentOrNextIfDoc(element, DotNetParameterListOwner.class);
				if(parameterListOwner == null)
				{
					return ResolveResult.EMPTY_ARRAY;
				}

				scopeProcessor = new SimpleNamedScopeProcessor(completion, ExecuteTarget.LOCAL_VARIABLE_OR_PARAMETER);
				state = ResolveState.initial();
				state = state.put(CSharpResolveUtil.SELECTOR, selector);

				parameterListOwner.processDeclarations(scopeProcessor, state, null, element);
				return scopeProcessor.toResolveResults();
			case NATIVE_TYPE_WRAPPER:
				PsiElement nativeElement = ((CSharpReferenceExpression) element).getReferenceElement();
				assert nativeElement != null;
				String nativeRuntimeType = CSharpNativeTypeImplUtil.ourElementToQTypes.get(nativeElement.getNode().getElementType());
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

				scopeProcessor = createMemberProcessor(options);

				state = ResolveState.initial();
				state = state.put(CSharpResolveUtil.EXTRACTOR, genericExtractor);
				state = state.put(CSharpResolveUtil.SELECTOR, selector);
				CSharpResolveUtil.walkChildren(scopeProcessor, typeElement, false, true, state);

				return scopeProcessor.toResolveResults();
			case LABEL:
				scopeProcessor = new SimpleNamedScopeProcessor(completion, ExecuteTarget.LABEL);

				DotNetQualifiedElement parentOfType = PsiTreeUtil.getParentOfType(element, DotNetQualifiedElement.class);
				assert parentOfType != null;

				state = ResolveState.initial();
				state = state.put(CSharpResolveUtil.SELECTOR, selector);
				CSharpResolveUtil.walkForLabel(scopeProcessor, parentOfType, state);
				return scopeProcessor.toResolveResults();
			case QUALIFIED_NAMESPACE:
			case SOFT_QUALIFIED_NAMESPACE:
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
						public boolean execute(@NotNull PsiElement element, ResolveState state)
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
			case PARAMETER:
				if(callArgumentListOwner == null)
				{
					return ResolveResult.EMPTY_ARRAY;
				}

				resolveResults = callArgumentListOwner.multiResolve(false);
				ResolveResult goodResolveResult = CSharpResolveUtil.findValidOrFirstMaybeResult(resolveResults);

				if(goodResolveResult == null)
				{
					return ResolveResult.EMPTY_ARRAY;
				}

				if(!(goodResolveResult instanceof MethodResolveResult))
				{
					return ResolveResult.EMPTY_ARRAY;
				}

				MethodCalcResult ourCalcResult = ((MethodResolveResult) goodResolveResult).getCalcResult();

				List<ResolveResult> newResults = new SmartList<ResolveResult>();
				for(NCallArgument o : ourCalcResult.getArguments())
				{
					PsiElement parameterElement = o.getParameterElement();
					if(parameterElement != null)
					{
						if(selector != null)
						{
							if(selector instanceof CSharpNamedResolveSelector && ((CSharpNamedResolveSelector) selector).isNameEqual(o
									.getParameterName()))
							{
								newResults.add(new PsiElementResolveResult(parameterElement, true));
							}
						}
						else
						{
							newResults.add(new PsiElementResolveResult(parameterElement, true));
						}
					}

				}
				return ContainerUtil.toArray(newResults, ResolveResult.EMPTY_ARRAY);
			case TYPE_LIKE:
			case NAMEOF:
				return processAnyMember(options, defaultExtractor, forceQualifierElement);
			case ANY_MEMBER:
				resolveResults = processAnyMember(options, defaultExtractor, forceQualifierElement);
				if(resolveResults.length == 0)
				{
					return ResolveResult.EMPTY_ARRAY;
				}

				methodResolveResults = new ArrayList<Pair<MethodCalcResult, PsiElement>>();

				for(ResolveResult result : resolveResults)
				{
					PsiElement resolvedElement = result.getElement();
					if(resolvedElement instanceof CSharpElementGroup && resolveFromParent)
					{
						CSharpLambdaResolveResult lambdaResolveResult = CSharpLambdaExpressionImplUtil.resolveLeftLambdaTypeRef(element);
						if(lambdaResolveResult != null)
						{
							for(PsiElement psiElement : ((CSharpElementGroup<?>) resolvedElement).getElements())
							{
								if(psiElement instanceof DotNetLikeMethodDeclaration)
								{
									MethodCalcResult calc = MethodResolver.calc(lambdaResolveResult.getParameterTypeRefs(),
											((DotNetLikeMethodDeclaration) psiElement).getParameterTypeRefs(), element);

									methodResolveResults.add(Pair.create(calc, psiElement));
								}
							}
						}
					}
					else
					{
						methodResolveResults.add(Pair.<MethodCalcResult, PsiElement>create(MethodCalcResult.VALID, result.getElement()));
					}
				}
				return WeightUtil.sortAndReturn(methodResolveResults);
			case METHOD:
			case ARRAY_METHOD:
			case CONSTRUCTOR:
			case BASE_CONSTRUCTOR:
			case THIS_CONSTRUCTOR:
				resolveResults = processAnyMember(options, defaultExtractor, forceQualifierElement);
				if(callArgumentListOwner == null || resolveResults.length == 0)
				{
					return ResolveResult.EMPTY_ARRAY;
				}

				methodResolveResults = new ArrayList<Pair<MethodCalcResult, PsiElement>>();
				for(ResolveResult result : resolveResults)
				{
					PsiElement maybeElementGroup = result.getElement();
					if(maybeElementGroup instanceof CSharpElementGroup)
					{
						for(PsiElement psiElement : ((CSharpElementGroup<?>) maybeElementGroup).getElements())
						{
							if(psiElement instanceof DotNetLikeMethodDeclaration)
							{
								GenericInferenceUtil.GenericInferenceResult inferenceResult = psiElement.getUserData(GenericInferenceUtil
										.INFERENCE_RESULT);

								if(inferenceResult == null && !isConstructorKind(kind))
								{
									inferenceResult = GenericInferenceUtil.inferenceGenericExtractor(element, callArgumentListOwner,
											(DotNetLikeMethodDeclaration) psiElement);
									psiElement = GenericUnwrapTool.extract((DotNetNamedElement) psiElement, inferenceResult.getExtractor());
								}

								val calcResult = MethodResolver.calc(callArgumentListOwner, (DotNetLikeMethodDeclaration) psiElement, element);

								if(inferenceResult == null || inferenceResult.isSuccess())
								{
									methodResolveResults.add(Pair.create(calcResult, psiElement));
								}
								else
								{
									methodResolveResults.add(Pair.create(calcResult.dup(Short.MIN_VALUE), psiElement));
								}
							}
						}
					}
					else if(maybeElementGroup instanceof DotNetVariable)
					{
						DotNetTypeRef dotNetTypeRef = ((DotNetVariable) maybeElementGroup).toTypeRef(true);

						DotNetTypeResolveResult maybeLambdaResolveResult = dotNetTypeRef.resolve(element);

						if(maybeLambdaResolveResult instanceof CSharpLambdaResolveResult)
						{
							CSharpLambdaResolveResult lambdaTypeResolveResult = (CSharpLambdaResolveResult) maybeLambdaResolveResult;

							val calcResult = MethodResolver.calc(callArgumentListOwner, lambdaTypeResolveResult.getParameterInfos(), element);

							methodResolveResults.add(Pair.create(calcResult, maybeElementGroup));
						}
					}
				}

				return WeightUtil.sortAndReturn(methodResolveResults);
		}
		return ResolveResult.EMPTY_ARRAY;
	}

	public static ResolveResult[] processAnyMember(@NotNull CSharpResolveOptions options)
	{
		return processAnyMember(options, DotNetGenericExtractor.EMPTY, options.getElement());
	}

	@NotNull
	public static ResolveResult[] tryResolveFromQualifier(@NotNull CSharpReferenceExpressionEx referenceExpressionEx,
			@NotNull PsiElement qualifierElement)
	{
		ResolveToKind kind = referenceExpressionEx.kind();
		return buildSelectorAndMultiResolve(kind, findCallArgumentListOwner(kind, referenceExpressionEx), referenceExpressionEx, qualifierElement,
				false);
	}

	public static ResolveResult[] processAnyMember(@NotNull CSharpResolveOptions options,
			@NotNull DotNetGenericExtractor defaultExtractor,
			@Nullable PsiElement forceQualifierElement)
	{
		PsiElement qualifier = options.getQualifier();
		@NotNull PsiElement element = options.getElement();
		ResolveToKind kind = options.getKind();
		CSharpCallArgumentListOwner callArgumentListOwner = options.getCallArgumentListOwner();
		CSharpResolveSelector selector = options.getSelector();
		boolean completion = options.isCompletion();

		PsiElement scopeElement = element;
		CSharpCodeFragment codeFragment = PsiTreeUtil.getParentOfType(element, CSharpCodeFragment.class);
		if(codeFragment != null)
		{
			scopeElement = codeFragment.getScopeElement();
		}

		if(isConstructorKind(kind))
		{
			CSharpReferenceExpressionEx referenceExpression = (CSharpReferenceExpressionEx) element;

			DotNetTypeRef typeRef = DotNetTypeRef.ERROR_TYPE;

			PsiElement referenceElement = referenceExpression.getReferenceElement();
			if(referenceElement == null)
			{
				return ResolveResult.EMPTY_ARRAY;
			}

			switch(kind)
			{
				case THIS_CONSTRUCTOR:
					typeRef = referenceExpression.toTypeRefWithoutCaching(ResolveToKind.THIS, true);
					break;
				case BASE_CONSTRUCTOR:
					typeRef = referenceExpression.toTypeRefWithoutCaching(ResolveToKind.BASE, true);
					break;
				default:
					if(callArgumentListOwner instanceof CSharpNewExpression)
					{
						typeRef = ((CSharpNewExpression) callArgumentListOwner).toTypeRef(true);
					}
					else if(callArgumentListOwner instanceof DotNetAttribute)
					{
						typeRef = ((DotNetAttribute) callArgumentListOwner).toTypeRef();
					}
					break;
			}

			DotNetTypeResolveResult typeResolveResult = typeRef.resolve(element);

			PsiElement resolveElement = typeResolveResult.getElement();
			if(resolveElement == null)
			{
				return ResolveResult.EMPTY_ARRAY;
			}

			ResolveState resolveState = ResolveState.initial();
			resolveState = resolveState.put(CSharpResolveUtil.EXTRACTOR, typeResolveResult.getGenericExtractor());
			if(selector != null)
			{
				resolveState = resolveState.put(CSharpResolveUtil.SELECTOR, selector);
			}

			AbstractScopeProcessor memberProcessor = createMemberProcessor(options);

			CSharpResolveUtil.walkChildren(memberProcessor, resolveElement, true, false, resolveState);
			return memberProcessor.toResolveResults();
		}

		PsiElement target = ObjectUtils.notNull(forceQualifierElement, element);
		DotNetGenericExtractor extractor = defaultExtractor;
		DotNetTypeRef qualifierTypeRef = DotNetTypeRef.ERROR_TYPE;

		if(qualifier instanceof DotNetExpression)
		{
			qualifierTypeRef = ((DotNetExpression) qualifier).toTypeRef(false);

			if(element instanceof CSharpReferenceExpression)
			{
				CSharpReferenceExpression.AccessType memberAccessType = ((CSharpReferenceExpression) element).getMemberAccessType();
				switch(memberAccessType)
				{
					case ARROW:
						if(qualifierTypeRef instanceof DotNetPointerTypeRef)
						{
							qualifierTypeRef = ((DotNetPointerTypeRef) qualifierTypeRef).getInnerTypeRef();
						}
						break;
				}
			}

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
			AbstractScopeProcessor memberProcessor = createMemberProcessor(options);

			if(!CSharpResolveUtil.walkChildren(memberProcessor, target, false, true, resolveState))
			{
				return memberProcessor.toResolveResults();
			}

			if((kind == ResolveToKind.METHOD || kind == ResolveToKind.ANY_MEMBER) && element instanceof CSharpReferenceExpression)
			{
				// walk for extensions
				val p = new ExtensionResolveScopeProcessor(qualifierTypeRef, (CSharpReferenceExpression) element, completion, callArgumentListOwner);
				p.merge(memberProcessor);

				resolveState = resolveState.put(CSharpResolveUtil.EXTRACTOR, extractor);
				resolveState = resolveState.put(CSharpResolveUtil.SELECTOR, new ExtensionMethodByNameSelector(((CSharpReferenceExpression) element)
						.getReferenceName()));

				Couple<PsiElement> resolveLayers = getResolveLayers(scopeElement, scopeElement != element);

				//PsiElement last = resolveLayers.getFirst();
				PsiElement targetToWalkChildren = resolveLayers.getSecond();

				if(!CSharpResolveUtil.walkChildren(p, targetToWalkChildren, true, false, resolveState))
				{
					return p.toResolveResults();
				}

				CSharpResolveUtil.walkUsing(p, targetToWalkChildren, null, resolveState);

				return p.toResolveResults();
			}
			return memberProcessor.toResolveResults();
		}
		else
		{
			Couple<PsiElement> resolveLayers = getResolveLayers(scopeElement, scopeElement != element);

			PsiElement last = resolveLayers.getFirst();
			PsiElement targetToWalkChildren = resolveLayers.getSecond();

			ResolveResult[] elements = ResolveResult.EMPTY_ARRAY;
			// if resolving is any member, first we need process locals and then go to fields and other
			if(kind == ResolveToKind.ANY_MEMBER || kind == ResolveToKind.METHOD || kind == ResolveToKind.NAMEOF)
			{
				SimpleNamedScopeProcessor localProcessor = new SimpleNamedScopeProcessor(completion, ExecuteTarget.LOCAL_VARIABLE_OR_PARAMETER);
				if(!CSharpResolveUtil.treeWalkUp(localProcessor, target, element, last, resolveState))
				{
					return localProcessor.toResolveResults();
				}
				elements = localProcessor.toResolveResults();
			}

			AbstractScopeProcessor p = createMemberProcessor(options.additionalElements(elements));

			if(!CSharpResolveUtil.walkChildren(p, targetToWalkChildren, true, true, resolveState))
			{
				return p.toResolveResults();
			}

			if(!CSharpResolveUtil.walkGenericParameterList(p, element, null, resolveState))
			{
				return p.toResolveResults();
			}

			CSharpResolveUtil.walkUsing(p, element, null, resolveState);

			return p.toResolveResults();
		}
	}

	@SuppressWarnings("unchecked")
	private static <T extends PsiElement> T findParentOrNextIfDoc(PsiElement element, Class<T> clazz)
	{
		CSharpGenericConstraintList constraintList = PsiTreeUtil.getParentOfType(element, CSharpGenericConstraintList.class);
		if(constraintList != null)
		{
			PsiElement parent = constraintList.getParent();
			if(parent != null && clazz.isInstance(parent))
			{
				return (T) parent;
			}
			return null;
		}

		CSharpDocRoot docRoot = PsiTreeUtil.getParentOfType(element, CSharpDocRoot.class);
		if(docRoot != null)
		{
			PsiElement docRootParent = docRoot.getParent();
			if(docRootParent != null && clazz.isInstance(docRootParent))
			{
				return (T) docRootParent;
			}
		}
		return null;
	}

	@NotNull
	public static AbstractScopeProcessor createMemberProcessor(@NotNull CSharpResolveOptions options)
	{
		ResolveToKind kind = options.getKind();
		PsiElement element = options.getElement();
		boolean completion = options.isCompletion();

		ExecuteTarget[] targets;
		Comparator<ResolveResult> sorter = null;
		switch(kind)
		{
			case TYPE_LIKE:
				targets = new ExecuteTarget[]{
						ExecuteTarget.GENERIC_PARAMETER,
						ExecuteTarget.TYPE,
						ExecuteTarget.DELEGATE_METHOD,
						ExecuteTarget.NAMESPACE,
						ExecuteTarget.TYPE_DEF
				};
				sorter = TypeLikeComparator.create(element);
				break;
			case QUALIFIED_NAMESPACE:
				targets = new ExecuteTarget[]{ExecuteTarget.NAMESPACE};
				break;
			case FIELD_OR_PROPERTY:
				targets = new ExecuteTarget[]{
						ExecuteTarget.FIELD,
						ExecuteTarget.PROPERTY,
						ExecuteTarget.EVENT
				};
				break;
			case ARRAY_METHOD:
				targets = new ExecuteTarget[]{ExecuteTarget.ELEMENT_GROUP};
				break;
			case METHOD:
				targets = new ExecuteTarget[]{
						ExecuteTarget.ELEMENT_GROUP,
						ExecuteTarget.FIELD,
						ExecuteTarget.PROPERTY,
						ExecuteTarget.EVENT,
						ExecuteTarget.LOCAL_VARIABLE_OR_PARAMETER
				};
				break;
			case CONSTRUCTOR:
			case THIS_CONSTRUCTOR:
			case BASE_CONSTRUCTOR:
				targets = new ExecuteTarget[]{
						ExecuteTarget.ELEMENT_GROUP
				};
				break;
			case NAMEOF:
				targets = ExecuteTarget.values();
				break;
			default:
				targets = new ExecuteTarget[]{
						ExecuteTarget.MEMBER,
						ExecuteTarget.TYPE_DEF,
						ExecuteTarget.ELEMENT_GROUP
				};
				sorter = VariableOrTypeComparator.create(element);
				if(completion)
				{
					// append generic when completion due at ANY_MEMBER it dont resolved
					targets = ArrayUtil.append(targets, ExecuteTarget.GENERIC_PARAMETER);
				}
				break;
		}

		AbstractScopeProcessor processor = completion ? new CompletionResolveScopeProcessor(options, targets) : new MemberResolveScopeProcessor
				(options, targets);
		processor.setComparator(sorter);
		return processor;
	}

	/**
	 * @return couple of psieelement, first is the last element for walk, second is the stub member for walk
	 */
	@NotNull
	public static Couple<PsiElement> getResolveLayers(PsiElement element, boolean strict)
	{
		String text = element.getText();
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
			targetToWalkChildren = element.getContainingFile();
		}
		return Couple.of(last, targetToWalkChildren);
	}

	@NotNull
	public static DotNetTypeRef toTypeRef(@Nullable PsiElement resolve)
	{
		return toTypeRef(resolve, DotNetGenericExtractor.EMPTY);
	}

	@NotNull
	public static DotNetTypeRef toTypeRef(@NotNull ResolveResult resolveResult)
	{
		PsiElement element = resolveResult.getElement();
		DotNetGenericExtractor extractor = DotNetGenericExtractor.EMPTY;
		if(resolveResult instanceof PsiElementResolveResultWithExtractor)
		{
			extractor = ((PsiElementResolveResultWithExtractor) resolveResult).getExtractor();
		}
		return toTypeRef(element, extractor);
	}

	@NotNull
	public static DotNetTypeRef toTypeRef(@Nullable PsiElement resolve, @NotNull DotNetGenericExtractor extractor)
	{
		if(resolve instanceof DotNetNamespaceAsElement)
		{
			return new CSharpTypeRefFromNamespace(((DotNetNamespaceAsElement) resolve).getPresentableQName());
		}
		else if(resolve instanceof DotNetTypeDeclaration)
		{
			return new CSharpTypeRefByTypeDeclaration((DotNetTypeDeclaration) resolve, extractor);
		}
		else if(resolve instanceof CSharpTypeDefStatement)
		{
			return ((CSharpTypeDefStatement) resolve).toTypeRef();
		}
		else if(resolve instanceof DotNetGenericParameter)
		{
			return new CSharpTypeRefFromGenericParameter((DotNetGenericParameter) resolve);
		}
		else if(resolve instanceof CSharpMethodDeclaration)
		{
			return new CSharpLambdaTypeRef((CSharpMethodDeclaration) resolve);
		}
		else if(resolve instanceof DotNetVariable)
		{
			return ((DotNetVariable) resolve).toTypeRef(true);
		}
		else if(resolve instanceof CSharpElementGroup)
		{
			return new CSharpElementGroupTypeRef((CSharpElementGroup<?>) resolve);
		}
		return DotNetTypeRef.ERROR_TYPE;
	}
}
