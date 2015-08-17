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

import java.util.Comparator;
import java.util.HashMap;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.doc.psi.CSharpDocRoot;
import org.mustbe.consulo.csharp.lang.psi.*;
import org.mustbe.consulo.csharp.lang.psi.impl.source.injection.CSharpForInjectionFragmentHolder;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.CSharpResolveOptions;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.CompletionResolveScopeProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.ExecuteTarget;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.MemberResolveScopeProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.PsiElementResolveResultWithExtractor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.SimpleNamedScopeProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.SortedMemberResolveScopeProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.StubScopeProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.extensionResolver.ExtensionResolveScopeProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.handlers.*;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.sorter.StaticVsInstanceComparator;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.sorter.TypeLikeComparator;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpElementGroupTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpGenericExtractor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpGenericWrapperTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefFromGenericParameter;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefFromNamespace;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.csharp.lang.psi.resolve.AttributeByNameSelector;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveSelector;
import org.mustbe.consulo.csharp.lang.psi.resolve.ExtensionMethodByNameSelector;
import org.mustbe.consulo.csharp.lang.psi.resolve.MemberByNameSelector;
import org.mustbe.consulo.csharp.lang.psi.resolve.StaticResolveSelectors;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.*;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetNamespaceAsElement;
import org.mustbe.consulo.dotnet.resolve.DotNetPointerTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import org.mustbe.consulo.dotnet.util.ArrayUtil2;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.util.Couple;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.ResolveState;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.CommonProcessors;
import com.intellij.util.ObjectUtil;
import com.intellij.util.Processor;
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

	private static KindProcessor[] ourProcessors = new KindProcessor[ResolveToKind.VALUES.length];

	static
	{
		for(int i = 0; i < ResolveToKind.VALUES.length; i++)
		{
			ResolveToKind value = ResolveToKind.VALUES[i];
			KindProcessor kindProcessor;
			switch(value)
			{
				case GENERIC_PARAMETER_FROM_PARENT:
					kindProcessor = new GenericFromParentKindProcessor();
					break;
				case QUALIFIED_NAMESPACE:
				case SOFT_QUALIFIED_NAMESPACE:
					kindProcessor = new QualifiedNamespaceKindProcessor();
					break;
				case NATIVE_TYPE_WRAPPER:
					kindProcessor = new NativeTypeWrapperKindProcessor();
					break;
				case METHOD:
				case ARRAY_METHOD:
				case CONSTRUCTOR:
				case BASE_CONSTRUCTOR:
				case THIS_CONSTRUCTOR:
					kindProcessor = new MethodLikeKindProcessor();
					break;
				case TYPE_LIKE:
				case NAMEOF:
					kindProcessor = new TypeLikeKindProcessor();
					break;
				case ANY_MEMBER:
					kindProcessor = new AnyMemberKindProcessor();
					break;
				case FIELD_OR_PROPERTY:
					kindProcessor = new FieldOrPropertyKindProcessor();
					break;
				case PARAMETER:
					kindProcessor = new ParameterKindProcessor();
					break;
				case THIS:
					kindProcessor = new ThisKindProcessor();
					break;
				case BASE:
					kindProcessor = new BaseKindProcessor();
					break;
				case ROOT_NAMESPACE:
					kindProcessor = new RootNamespaceKindProcessor();
					break;
				case LABEL:
					kindProcessor = new LabelKindProcessor();
					break;
				case PARAMETER_FROM_PARENT:
					kindProcessor = new ParameterFromParentKindProcessor();
					break;
				case EXPRESSION_OR_TYPE_LIKE:
					kindProcessor = new ExpressionOrTypeLikeKindProcessor();
					break;
				default:
					kindProcessor = new DummyKindProcessor(value);
					break;
			}
			ourProcessors[i] = kindProcessor;
		}
	}

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

	@NotNull
	public static String getReferenceText(@NotNull CSharpReferenceExpression referenceExpression)
	{
		StringBuilder builder = new StringBuilder();
		PsiElement qualifier = referenceExpression.getQualifier();
		if(qualifier instanceof CSharpReferenceExpression)
		{
			if(((CSharpReferenceExpression) qualifier).isGlobalElement())
			{
				builder.append("global");
			}
			else
			{
				builder.append(((CSharpReferenceExpression) qualifier).getReferenceText());
			}
			switch(referenceExpression.getMemberAccessType())
			{
				case NONE:
					break;
				case DOT:
					builder.append(".");
					break;
				case ARROW:
					builder.append("->");
					break;
				case COLONCOLON:
					builder.append("::");
					break;
				case NULLABLE_CALL:
					builder.append(".?");
					break;
			}
		}
		return builder.toString();
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
	@RequiredReadAction
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
	@RequiredReadAction
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

	@RequiredReadAction
	public static int getTypeArgumentListSize(@NotNull CSharpReferenceExpression referenceExpression)
	{
		DotNetTypeList typeArgumentList = referenceExpression.getTypeArgumentList();
		if(typeArgumentList == null)
		{
			return 0;
		}
		return typeArgumentList.getTypesCount();
	}

	@RequiredReadAction
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
	@RequiredReadAction
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
			PsiElement parent = tempElement.getParent();

			CSharpLocalVariable localVariable = PsiTreeUtil.getParentOfType(tempElement, CSharpLocalVariable.class);

			if(localVariable != null)
			{
				if(CSharpPsiUtilImpl.isNullOrEmpty(localVariable))
				{
					return ResolveToKind.EXPRESSION_OR_TYPE_LIKE;
				}
			}

			if(parent instanceof CSharpCallArgumentListOwner && ((CSharpCallArgumentListOwner) parent).canResolve())
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
		else if(tempElement instanceof CSharpNamedFieldOrPropertySet)
		{
			if(((CSharpFieldOrPropertySet) tempElement).getNameElement() == referenceExpression)
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

			CSharpUserType userType = PsiTreeUtil.getParentOfType(referenceExpression, CSharpUserType.class);
			if(userType != null)
			{
				CSharpLocalVariable localVariable = PsiTreeUtil.getParentOfType(userType, CSharpLocalVariable.class);
				if(localVariable != null)
				{
					if(CSharpPsiUtilImpl.isNullOrEmpty(localVariable))
					{
						return ResolveToKind.EXPRESSION_OR_TYPE_LIKE;
					}
				}
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
	@RequiredReadAction
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

	@NotNull
	@RequiredReadAction
	public static ResolveResult[] buildSelectorAndMultiResolve(@NotNull ResolveToKind kind,
			@Nullable final CSharpCallArgumentListOwner callArgumentListOwner,
			@NotNull final CSharpQualifiedNonReference element,
			boolean resolveFromParent)
	{
		return buildSelectorAndMultiResolve(kind, callArgumentListOwner, element, null, resolveFromParent);
	}

	@NotNull
	@RequiredReadAction
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

		CommonProcessors.CollectProcessor<ResolveResult> processor = new CommonProcessors.CollectProcessor<ResolveResult>();
		collectResults(new CSharpResolveOptions(kind, selector, element, callArgumentListOwner, false, resolveFromParent),
				DotNetGenericExtractor.EMPTY, forceQualifierElement, processor);
		return processor.toArray(ResolveResult.ARRAY_FACTORY);
	}

	@RequiredReadAction
	public static void collectResults(@NotNull CSharpResolveOptions options, @NotNull Processor<ResolveResult> processor)
	{
		collectResults(options, DotNetGenericExtractor.EMPTY, null, processor);
	}

	@NotNull
	@RequiredReadAction
	@Deprecated
	public static ResolveResult[] collectResults(@NotNull CSharpResolveOptions options)
	{
		CommonProcessors.CollectProcessor<ResolveResult> processor = new CommonProcessors.CollectProcessor<ResolveResult>();
		collectResults(options, processor);
		return processor.toArray(ResolveResult.ARRAY_FACTORY);
	}

	@RequiredReadAction
	public static void collectResults(@NotNull CSharpResolveOptions options,
			@NotNull DotNetGenericExtractor defaultExtractor,
			@Nullable PsiElement forceQualifierElement,
			@NotNull final Processor<ResolveResult> processor)
	{
		final ResolveToKind kind = options.getKind();

		KindProcessor kindProcessor = ourProcessors[kind.ordinal()];

		kindProcessor.process(options, defaultExtractor, forceQualifierElement, processor);
	}

	@RequiredReadAction
	public static void processAnyMember(@NotNull CSharpResolveOptions options, @NotNull Processor<ResolveResult> processor)
	{
		processAnyMember(options, DotNetGenericExtractor.EMPTY, options.getElement(), processor);
	}

	@NotNull
	@RequiredReadAction
	public static ResolveResult[] tryResolveFromQualifier(@NotNull CSharpReferenceExpressionEx referenceExpressionEx,
			@NotNull PsiElement qualifierElement)
	{
		ResolveToKind kind = referenceExpressionEx.kind();
		return buildSelectorAndMultiResolve(kind, findCallArgumentListOwner(kind, referenceExpressionEx), referenceExpressionEx, qualifierElement,
				false);
	}

	@RequiredReadAction
	public static void processAnyMember(@NotNull CSharpResolveOptions options,
			@NotNull DotNetGenericExtractor defaultExtractor,
			@Nullable PsiElement forceQualifierElement,
			@NotNull Processor<ResolveResult> processor)
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
			if(scopeElement == null)
			{
				scopeElement = element;
			}
		}

		if(isConstructorKind(kind))
		{
			CSharpReferenceExpressionEx referenceExpression = (CSharpReferenceExpressionEx) element;

			DotNetTypeRef typeRef = DotNetTypeRef.ERROR_TYPE;

			PsiElement referenceElement = referenceExpression.getReferenceElement();
			if(referenceElement == null)
			{
				return;
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
				return;
			}

			ResolveState resolveState = ResolveState.initial();
			resolveState = resolveState.put(CSharpResolveUtil.EXTRACTOR, typeResolveResult.getGenericExtractor());
			if(selector != null)
			{
				resolveState = resolveState.put(CSharpResolveUtil.SELECTOR, selector);
			}

			StubScopeProcessor memberProcessor = createMemberProcessor(options, processor);

			CSharpResolveUtil.walkChildren(memberProcessor, resolveElement, true, false, resolveState);
			return;
		}

		PsiElement target = ObjectUtil.notNull(forceQualifierElement, element);
		DotNetGenericExtractor extractor = defaultExtractor;
		DotNetTypeRef qualifierTypeRef = DotNetTypeRef.ERROR_TYPE;

		if(forceQualifierElement == null && qualifier instanceof DotNetExpression)
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
				return;
			}
		}

		if(!target.isValid())
		{
			return;
		}

		ResolveState resolveState = ResolveState.initial();
		resolveState = resolveState.put(CSharpResolveUtil.EXTRACTOR, extractor);
		if(selector != null)
		{
			resolveState = resolveState.put(CSharpResolveUtil.SELECTOR, selector);
		}

		if(target != element)
		{
			StubScopeProcessor memberProcessor = createMemberProcessor(options, processor);

			if(!CSharpResolveUtil.walkChildren(memberProcessor, target, false, true, resolveState))
			{
				consumeSorted(memberProcessor);
				return;
			}

			if((kind == ResolveToKind.METHOD || kind == ResolveToKind.ANY_MEMBER) && element instanceof CSharpReferenceExpression)
			{
				// walk for extensions
				ExtensionResolveScopeProcessor extensionProcessor = new ExtensionResolveScopeProcessor(qualifierTypeRef, (CSharpReferenceExpression) element,
						completion, processor, callArgumentListOwner);

				resolveState = resolveState.put(CSharpResolveUtil.EXTRACTOR, extractor);
				resolveState = resolveState.put(CSharpResolveUtil.SELECTOR, new ExtensionMethodByNameSelector(((CSharpReferenceExpression) element)
						.getReferenceName()));

				Couple<PsiElement> resolveLayers = getResolveLayers(scopeElement, scopeElement != element);

				//PsiElement last = resolveLayers.getFirst();
				PsiElement targetToWalkChildren = resolveLayers.getSecond();

				if(!CSharpResolveUtil.walkChildren(extensionProcessor, targetToWalkChildren, true, false, resolveState))
				{
					consumeSorted(memberProcessor);
					return;
				}

				CSharpResolveUtil.walkUsing(extensionProcessor, targetToWalkChildren, null, resolveState);

				extensionProcessor.consumeAsMethodGroup();
			}

			consumeSorted(memberProcessor);
		}
		else
		{
			Couple<PsiElement> resolveLayers = getResolveLayers(scopeElement, scopeElement != element);

			PsiElement last = resolveLayers.getFirst();
			PsiElement targetToWalkChildren = resolveLayers.getSecond();

			// if resolving is any member, first we need process locals and then go to fields and other
			if(kind == ResolveToKind.ANY_MEMBER || kind == ResolveToKind.METHOD || kind == ResolveToKind.NAMEOF)
			{
				SimpleNamedScopeProcessor localProcessor = new SimpleNamedScopeProcessor(processor, completion,
						ExecuteTarget.LOCAL_VARIABLE_OR_PARAMETER);

				if(!CSharpResolveUtil.treeWalkUp(localProcessor, target, element, last, resolveState))
				{
					return;
				}
			}

			StubScopeProcessor memberProcessor = createMemberProcessor(options, processor);

			if(!CSharpResolveUtil.walkChildren(memberProcessor, targetToWalkChildren, true, true, resolveState))
			{
				consumeSorted(memberProcessor);
				return;
			}

			if(!CSharpResolveUtil.walkGenericParameterList(memberProcessor, processor, element, null, resolveState))
			{
				consumeSorted(memberProcessor);
				return;
			}

			CSharpResolveUtil.walkUsing(memberProcessor, element, null, resolveState);

			consumeSorted(memberProcessor);
		}
	}

	private static void consumeSorted(StubScopeProcessor memberProcessor)
	{
		if(memberProcessor instanceof SortedMemberResolveScopeProcessor)
		{
			((SortedMemberResolveScopeProcessor) memberProcessor).consumeAll();
		}
	}

	@SuppressWarnings("unchecked")
	public static <T extends PsiElement> T findParentOrNextIfDoc(PsiElement element, Class<T> clazz)
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
	public static StubScopeProcessor createMemberProcessor(@NotNull CSharpResolveOptions options, @NotNull Processor<ResolveResult> resultProcessor)
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
				sorter = StaticVsInstanceComparator.create(element);
				if(completion)
				{
					// append generic when completion due at ANY_MEMBER it dont resolved
					targets = ArrayUtil.append(targets, ExecuteTarget.GENERIC_PARAMETER);
				}
				break;
		}

		if(options.isCompletion())
		{
			return new CompletionResolveScopeProcessor(options, resultProcessor, targets);
		}
		else
		{
			if(sorter != null)
			{
				return new SortedMemberResolveScopeProcessor(options, resultProcessor, sorter, targets);
			}
			else
			{
				return new MemberResolveScopeProcessor(options, resultProcessor, targets);
			}
		}
	}

	/**
	 * @return couple of psieelement, first is the last element for walk, second is the stub member for walk
	 */
	@NotNull
	public static Couple<PsiElement> getResolveLayers(final PsiElement element, boolean strict)
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
	@RequiredReadAction
	public static DotNetTypeRef toTypeRef(@Nullable PsiElement resolve)
	{
		return toTypeRef(resolve, DotNetGenericExtractor.EMPTY);
	}

	@NotNull
	@RequiredReadAction
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
	@RequiredReadAction
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
