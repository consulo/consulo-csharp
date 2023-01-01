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

package consulo.csharp.lang.impl.psi.source;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.application.progress.ProgressIndicatorProvider;
import consulo.application.util.CachedValueProvider;
import consulo.application.util.function.CommonProcessors;
import consulo.application.util.function.Processor;
import consulo.csharp.lang.doc.psi.CSharpDocRoot;
import consulo.csharp.lang.impl.ide.codeInspection.unusedUsing.UnusedUsingVisitor;
import consulo.csharp.lang.impl.psi.CSharpFileFactory;
import consulo.csharp.lang.impl.psi.CSharpNullableTypeUtil;
import consulo.csharp.lang.impl.psi.CSharpTokenSets;
import consulo.csharp.lang.impl.psi.resolve.AttributeByNameSelector;
import consulo.csharp.lang.impl.psi.source.resolve.*;
import consulo.csharp.lang.impl.psi.source.resolve.extensionResolver.ExtensionResolveScopeProcessor;
import consulo.csharp.lang.impl.psi.source.resolve.genericInference.GenericInferenceManager;
import consulo.csharp.lang.impl.psi.source.resolve.handlers.*;
import consulo.csharp.lang.impl.psi.source.resolve.sorter.StaticVsInstanceComparator;
import consulo.csharp.lang.impl.psi.source.resolve.sorter.TypeLikeComparator;
import consulo.csharp.lang.impl.psi.source.resolve.type.*;
import consulo.csharp.lang.impl.psi.source.resolve.util.CSharpResolveUtil;
import consulo.csharp.lang.impl.psi.source.using.AddUsingUtil;
import consulo.csharp.lang.psi.*;
import consulo.csharp.lang.impl.psi.source.injection.CSharpForInjectionFragmentHolder;
import consulo.csharp.lang.psi.resolve.*;
import consulo.document.util.TextRange;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.*;
import consulo.dotnet.psi.resolve.*;
import consulo.dotnet.util.ArrayUtil2;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;
import consulo.language.inject.InjectedLanguageManager;
import consulo.language.psi.*;
import consulo.language.psi.resolve.ResolveCache;
import consulo.language.psi.resolve.ResolveState;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.util.LanguageCachedValueUtil;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.util.collection.ArrayUtil;
import consulo.util.lang.Comparing;
import consulo.util.lang.Couple;
import consulo.util.lang.ObjectUtil;
import consulo.util.lang.SystemProperties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static consulo.csharp.lang.psi.CSharpReferenceExpression.ResolveToKind;

/**
 * @author VISTALL
 * @since 17.06.14
 */
public class CSharpReferenceExpressionImplUtil
{
	public static final boolean ourEnableCache = SystemProperties.getBooleanProperty("consulo.csharp.enable.ref.cache", true);

	private static class OurResolver implements ResolveCache.PolyVariantResolver<CSharpReferenceExpressionEx>
	{
		public static final OurResolver INSTANCE = new OurResolver();

		@RequiredReadAction
		@Nonnull
		@Override
		public ResolveResult[] resolve(@Nonnull CSharpReferenceExpressionEx ref, boolean resolveFromParent)
		{
			return ref.multiResolveImpl(ref.kind(), resolveFromParent);
		}
	}

	private static class ParamatizeValue
	{
		private final Map<PsiElement, ResolveResult[]> myResults = new ConcurrentHashMap<>();

		public ResolveResult[] get(@Nonnull PsiElement element, @Nonnull Function<PsiElement, ResolveResult[]> function)
		{
			return myResults.computeIfAbsent(element, function);
		}
	}

	public static final TokenSet ourReferenceElements = TokenSet.orSet(CSharpTokenSets.NATIVE_TYPES, TokenSet.create(CSharpTokens.THIS_KEYWORD, CSharpTokens.BASE_KEYWORD, CSharpTokens.IDENTIFIER,
			CSharpSoftTokens.GLOBAL_KEYWORD));

	public static final TokenSet ourAccessTokens = TokenSet.create(CSharpTokens.ARROW, CSharpTokens.DOT, CSharpTokens.PLUS, CSharpTokens.COLONCOLON, CSharpTokens.NULLABE_CALL);

	private static final KindProcessor[] ourProcessors = new KindProcessor[ResolveToKind.VALUES.length];

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

	@Nonnull
	@RequiredWriteAction
	public static PsiElement bindToElement(CSharpReferenceExpressionEx expression, PsiElement element)
	{
		if(element instanceof CSharpTypeDeclaration)
		{
			String parentQName = ((CSharpTypeDeclaration) element).getPresentableParentQName();
			if(parentQName == null)
			{
				return element;
			}

			DotNetExpression qualifier = expression.getQualifier();
			if(qualifier != null)
			{
				if(qualifier instanceof CSharpReferenceExpression)
				{
					DotNetExpression newQualifier = CSharpFileFactory.createExpression(expression.getProject(), qualifier, parentQName);
					qualifier.replace(newQualifier);
					return element;
				}

				// if expression is not reference?
				return element;
			}
		}

		// it will CSharpNamespaceDeclaration (not DotNetNamespaceAsElement - since it can be not changed)
		if(element instanceof CSharpNamespaceDeclaration)
		{
			String qName = ((CSharpNamespaceDeclaration) element).getPresentableQName();
			PsiElement parent = expression.getParent();

			PsiElement resolvedElement = expression.resolve();
			// namespace is not resolved - replace it via current namespace
			if(resolvedElement == null)
			{
				if(parent instanceof CSharpUsingNamespaceStatement)
				{
					// if new qualifier already in using list - do not change expr and provide duplicate using
					if(AddUsingUtil.isUsingListContainsNamespace((CSharpUsingListOwner) parent.getParent(), qName))
					{
						parent.delete();
						return element;
					}
				}

				DotNetExpression newQualifier = CSharpFileFactory.createExpression(expression.getProject(), expression, qName);
				expression.replace(newQualifier);
				return newQualifier;
			}
			else
			{
				PsiFile containingFile = expression.getContainingFile();

				// if this expression parent is using statement. check if it unused. and change if unused
				if(parent instanceof CSharpUsingNamespaceStatement)
				{
					UnusedUsingVisitor usingVisitor = UnusedUsingVisitor.accept(containingFile);

					Boolean isUsed = usingVisitor.getUsingContext().get(parent);
					if(isUsed == Boolean.FALSE)
					{
						DotNetExpression newQualifier = CSharpFileFactory.createExpression(expression.getProject(), expression, qName);
						expression.replace(newQualifier);
						return newQualifier;
					}
				}

				AddUsingUtil.addUsingNoCaretMoving(containingFile, qName);
			}
		}
		return element;
	}

	@RequiredReadAction
	public static boolean isReferenceTo(PsiPolyVariantReference referenceExpression, PsiElement element)
	{
		final ResolveResult firstValidResult = CSharpResolveUtil.findValidOrFirstMaybeResult(referenceExpression.multiResolve(false));
		if(firstValidResult == null)
		{
			return false;
		}
		return isReferenceTo(firstValidResult, element);
	}

	@RequiredReadAction
	@Nonnull
	public static ResolveResult[] multiResolve(CSharpReferenceExpressionEx expression, final boolean incompleteCode, final boolean resolveFromParent)
	{
		if(!expression.isValid())
		{
			return ResolveResult.EMPTY_ARRAY;
		}

		ResolveResult[] resolveResults;
		CSharpReferenceExpressionImplUtil.OurResolver resolver = CSharpReferenceExpressionImplUtil.OurResolver.INSTANCE;
		if(isCacheDisabled(expression))
		{
			resolveResults = resolver.resolve(expression, resolveFromParent);
		}
		else
		{
			resolveResults = ResolveCache.getInstance(expression.getProject()).resolveWithCaching(expression, resolver, false, resolveFromParent);
		}

		if(incompleteCode)
		{
			return resolveResults;
		}
		else
		{
			ResolveResult[] validResults = CSharpResolveUtil.filterValidResults(resolveResults);
			if(validResults.length > 0)
			{
				return validResults;
			}
			else if(resolveResults.length > 0)
			{
				ResolveToKind kind = expression.kind();

				switch(kind)
				{
					case CONSTRUCTOR:
					case METHOD:
						return resolveResults;
				}
			}

			return ResolveResult.EMPTY_ARRAY;
		}
	}

	public static boolean isCacheDisabled(@Nonnull PsiElement element)
	{
		// inside generic inference session - don't call cache
		if(GenericInferenceManager.getInstance(element.getProject()).isInsideGenericInferenceSession())
		{
			return true;
		}
		return !ourEnableCache;
	}

	@RequiredReadAction
	private static boolean isReferenceTo(@Nonnull ResolveResult resolveResult, PsiElement element)
	{
		PsiElement psiElement = resolveResult.getElement();
		if(element instanceof DotNetNamespaceAsElement && psiElement instanceof DotNetNamespaceAsElement)
		{
			if(Comparing.equal(((DotNetNamespaceAsElement) psiElement).getPresentableQName(), ((DotNetNamespaceAsElement) element).getPresentableQName()))
			{
				return true;
			}
		}

		if(element.getManager().areElementsEquivalent(element, psiElement))
		{
			return true;
		}
		return false;
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

	@Nonnull
	@RequiredReadAction
	public static DotNetTypeRef toTypeRef(@Nonnull CSharpReferenceExpressionEx referenceExpressionEx, boolean resolveFromParent)
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

		if(resolveResult instanceof MethodResolveResult && ((MethodResolveResult) resolveResult).isUnknown())
		{
			return DotNetTypeRef.UNKNOWN_TYPE;
		}

		DotNetTypeRef typeRef = CSharpReferenceExpressionImplUtil.toTypeRef(referenceExpressionEx.getResolveScope(), resolveResult);
		if(CSharpNullableTypeUtil.containsNullableCalls(referenceExpressionEx))
		{
			return CSharpNullableTypeUtil.boxIfNeed(typeRef);
		}
		return typeRef;
	}

	@Nonnull
	@RequiredReadAction
	public static DotNetTypeRef toTypeRefWithoutCaching(@Nonnull CSharpReferenceExpressionEx referenceExpressionEx, @Nonnull ResolveToKind kind, boolean resolveFromParent)
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
		return CSharpReferenceExpressionImplUtil.toTypeRef(referenceExpressionEx.getResolveScope(), firstValidResult);
	}

	@RequiredReadAction
	public static int getTypeArgumentListSize(@Nullable PsiElement element)
	{
		if(!(element instanceof CSharpReferenceExpression))
		{
			return 0;
		}
		DotNetTypeList typeArgumentList = ((CSharpReferenceExpression) element).getTypeArgumentList();
		if(typeArgumentList == null)
		{
			return 0;
		}
		return typeArgumentList.getTypesCount();
	}

	@RequiredReadAction
	public static TextRange getRangeInElement(@Nonnull CSharpReferenceExpression referenceExpression)
	{
		PsiElement referenceElement = referenceExpression.getReferenceElement();
		if(referenceElement == null)
		{
			return TextRange.EMPTY_RANGE;
		}

		int startOffset = referenceElement.getStartOffsetInParent();
		return new TextRange(startOffset, referenceElement.getTextLength() + startOffset);
	}

	@Nonnull
	@RequiredReadAction
	public static ResolveToKind kind(@Nonnull CSharpReferenceExpression referenceExpression)
	{
		return LanguageCachedValueUtil.getCachedValue(referenceExpression, () -> CachedValueProvider.Result.create(kindImpl(referenceExpression), PsiModificationTracker.MODIFICATION_COUNT));
	}

	@Nonnull
	@RequiredReadAction
	private static ResolveToKind kindImpl(@Nonnull CSharpReferenceExpression referenceExpression)
	{
		if(referenceExpression.isGlobalElement())
		{
			return ResolveToKind.ROOT_NAMESPACE;
		}
		PsiElement tempElement = referenceExpression.getParent();
		if(tempElement instanceof CSharpGenericConstraintImpl)
		{
			DotNetGenericParameterListOwner parameterListOwner = PsiTreeUtil.getParentOfType(referenceExpression, DotNetGenericParameterListOwner.class);
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
				if(localVariable.getParent() instanceof CSharpCatchStatementImpl)
				{
					// catch be without name
					return ResolveToKind.TYPE_LIKE;
				}

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
			CSharpNamespaceDeclarationImpl netNamespaceDeclaration = PsiTreeUtil.getParentOfType(referenceExpression, CSharpNamespaceDeclarationImpl.class);
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
					if(localVariable.getParent() instanceof CSharpCatchStatementImpl)
					{
						// catch be without name
						return ResolveToKind.TYPE_LIKE;
					}
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
	public static PsiElement resolveByTypeKind(@Nonnull DotNetReferenceExpression referenceExpression, boolean attributeSuffix)
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

	@Nonnull
	@RequiredReadAction
	public static ResolveResult[] multiResolveImpl(ResolveToKind kind, final CSharpCallArgumentListOwner callArgumentListOwner, final CSharpQualifiedNonReference element, boolean resolveFromParent)
	{
		ResolveResult[] resolveResults = buildSelectorAndMultiResolve(kind, callArgumentListOwner, element, resolveFromParent);
		if(element instanceof CSharpReferenceExpression)
		{
			int typeArgumentListSize = getTypeArgumentListSize(element);
			if(typeArgumentListSize > 0)
			{
				DotNetTypeRef[] typeArgumentListRefs = ((CSharpReferenceExpression) element).getTypeArgumentListRefs();

				for(int i = 0; i < resolveResults.length; i++)
				{
					ResolveResult resolveResult = resolveResults[i];

					PsiElement resolveResultElement = resolveResult.getElement();
					if(resolveResultElement instanceof CSharpTypeDeclaration)
					{
						Map<DotNetGenericParameter, DotNetTypeRef> map = new HashMap<DotNetGenericParameter, DotNetTypeRef>();
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
						resolveResults[i] = CSharpResolveResultWithExtractor.withExtractor(resolveResult, CSharpGenericExtractor.create(map));
					}
				}
			}
		}
		return resolveResults;
	}

	@Nonnull
	@RequiredReadAction
	public static ResolveResult[] buildSelectorAndMultiResolve(@Nonnull ResolveToKind kind,
															   @Nullable final CSharpCallArgumentListOwner callArgumentListOwner,
															   @Nonnull final CSharpQualifiedNonReference element,
															   boolean resolveFromParent)
	{
		return buildSelectorAndMultiResolve(kind, callArgumentListOwner, element, null, resolveFromParent);
	}

	@Nonnull
	@RequiredReadAction
	public static ResolveResult[] buildSelectorAndMultiResolve(@Nonnull ResolveToKind kind,
															   @Nullable final CSharpCallArgumentListOwner callArgumentListOwner,
															   @Nonnull final CSharpQualifiedNonReference element,
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
				String referenceName = element.getReferenceName();
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
		collectResults(new CSharpResolveOptions(kind, selector, element, callArgumentListOwner, false, resolveFromParent), DotNetGenericExtractor.EMPTY, forceQualifierElement, processor);
		return processor.toArray(ResolveResult.ARRAY_FACTORY);
	}

	@RequiredReadAction
	public static void collectResults(@Nonnull CSharpResolveOptions options, @Nonnull Processor<ResolveResult> processor)
	{
		collectResults(options, DotNetGenericExtractor.EMPTY, null, processor);
	}

	@RequiredReadAction
	public static void collectResults(@Nonnull CSharpResolveOptions options,
									  @Nonnull DotNetGenericExtractor defaultExtractor,
									  @Nullable PsiElement forceQualifierElement,
									  @Nonnull final Processor<ResolveResult> processor)
	{
		final ResolveToKind kind = options.getKind();

		KindProcessor kindProcessor = ourProcessors[kind.ordinal()];

		kindProcessor.process(options, defaultExtractor, forceQualifierElement, processor);
	}

	@Nonnull
	@RequiredReadAction
	public static ResolveResult[] tryResolveFromQualifier(@Nonnull CSharpReferenceExpressionEx expression, @Nonnull PsiElement qualifierElement)
	{
		if(!expression.isValid())
		{
			return ResolveResult.EMPTY_ARRAY;
		}

		ParamatizeValue paramatizeValue = LanguageCachedValueUtil.getCachedValue(expression, () -> CachedValueProvider.Result.create(new ParamatizeValue(), PsiModificationTracker.MODIFICATION_COUNT));

		return paramatizeValue.get(qualifierElement, element -> tryResolveFromQualifierImpl(expression, element));
	}

	@Nonnull
	@RequiredReadAction
	private static ResolveResult[] tryResolveFromQualifierImpl(@Nonnull CSharpReferenceExpressionEx referenceExpressionEx, @Nonnull PsiElement qualifierElement)
	{
		ResolveToKind kind = referenceExpressionEx.kind();
		return buildSelectorAndMultiResolve(kind, findCallArgumentListOwner(kind, referenceExpressionEx), referenceExpressionEx, qualifierElement, false);
	}

	@RequiredReadAction
	public static void processAnyMember(@Nonnull CSharpResolveOptions options,
										@Nonnull DotNetGenericExtractor defaultExtractor,
										@Nullable PsiElement forceQualifierElement,
										@Nonnull @RequiredReadAction Processor<ResolveResult> processor)
	{
		PsiElement qualifier = options.getQualifier();
		@Nonnull PsiElement element = options.getElement();
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

			DotNetTypeResolveResult typeResolveResult = typeRef.resolve();

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
			if(qualifierTypeRef == DotNetTypeRef.UNKNOWN_TYPE)
			{
				processor.process(CSharpUndefinedResolveResult.INSTANCE);
				return;
			}

			// try to remap data if parent is wrong, for example resolve to type, but actual it's a field
			if(options.isCompletion() && qualifierTypeRef == DotNetTypeRef.ERROR_TYPE && qualifier instanceof CSharpReferenceExpressionEx)
			{
				ResolveResult[] resolveResults = ((CSharpReferenceExpressionEx) qualifier).multiResolveImpl(ResolveToKind.ANY_MEMBER, false);
				for(ResolveResult resolveResult : resolveResults)
				{
					PsiElement el = resolveResult.getElement();
					if(el instanceof CSharpLocalVariable)
					{
						DotNetType type = ((CSharpLocalVariable) el).getType();
						if(type instanceof CSharpUserType)
						{
							CSharpReferenceExpression referenceExpression = ((CSharpUserType) type).getReferenceExpression();
							if(referenceExpression.getQualifier() == qualifier)
							{
								// local variable is self holder
								continue;
							}
						}
					}

					qualifierTypeRef = toTypeRef(element.getResolveScope(), resolveResult);
					break;
				}
			}

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

			DotNetTypeResolveResult typeResolveResult = qualifierTypeRef.resolve();

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
		else if(forceQualifierElement != null)
		{
			qualifierTypeRef = toTypeRef(element.getResolveScope(), forceQualifierElement);
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
				ExtensionResolveScopeProcessor extensionProcessor = new ExtensionResolveScopeProcessor(qualifierTypeRef, (CSharpReferenceExpression) element, completion, memberProcessor,
						callArgumentListOwner);

				resolveState = resolveState.put(CSharpResolveUtil.EXTRACTOR, extractor);
				resolveState = resolveState.put(CSharpResolveUtil.SELECTOR, new ExtensionMethodByNameSelector(((CSharpReferenceExpression) element).getReferenceName()));

				Couple<PsiElement> resolveLayers = getResolveLayers(scopeElement, scopeElement != element);

				//PsiElement last = resolveLayers.getFirst();
				PsiElement targetToWalkChildren = resolveLayers.getSecond();

				if(!CSharpResolveUtil.walkChildren(extensionProcessor, targetToWalkChildren, true, false, resolveState))
				{
					consumeSorted(memberProcessor);
					return;
				}

				CSharpResolveUtil.walkUsing(extensionProcessor, targetToWalkChildren, null, resolveState);

				if(element instanceof CSharpReferenceExpression)
				{
					if(DotNetTypeRefUtil.isVmQNameEqual(qualifierTypeRef, DotNetTypes.System.Nullable$1))
					{
						extensionProcessor.unpackNullableTypeRef();

						if(!CSharpResolveUtil.walkChildren(extensionProcessor, targetToWalkChildren, true, false, resolveState))
						{
							consumeSorted(memberProcessor);
							return;
						}

						CSharpResolveUtil.walkUsing(extensionProcessor, targetToWalkChildren, null, resolveState);
					}
				}

				extensionProcessor.consumeAsMethodGroup();
			}

			consumeSorted(memberProcessor);
		}
		else
		{
			Couple<PsiElement> resolveLayers = getResolveLayers(scopeElement, scopeElement != element);

			PsiElement last = resolveLayers.getFirst();
			PsiElement targetToWalkChildren = resolveLayers.getSecond();

			StubScopeProcessor memberProcessor = createMemberProcessor(options, processor);

			// if resolving is any member, first we need process locals and then go to fields and other
			if(kind == ResolveToKind.ANY_MEMBER || kind == ResolveToKind.METHOD || kind == ResolveToKind.NAMEOF)
			{
				SimpleNamedScopeProcessor localProcessor = new SimpleNamedScopeProcessor(memberProcessor, completion, ExecuteTarget.LOCAL_VARIABLE_OR_PARAMETER_OR_LOCAL_METHOD);

				CSharpResolveUtil.treeWalkUp(localProcessor, target, element, last, resolveState);
			}

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

	@Nonnull
	@RequiredReadAction
	public static StubScopeProcessor createMemberProcessor(@Nonnull CSharpResolveOptions options, @Nonnull Processor<ResolveResult> resultProcessor)
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
						ExecuteTarget.LOCAL_VARIABLE_OR_PARAMETER_OR_LOCAL_METHOD
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
	@Nonnull
	@RequiredReadAction
	public static Couple<PsiElement> getResolveLayers(final PsiElement element, boolean strict)
	{
		PsiElement last = null;
		PsiElement targetToWalkChildren = null;

		PsiElement temp = strict ? element : element.getParent();
		loop:
		while(temp != null)
		{
			ProgressIndicatorProvider.checkCanceled();

			if(temp instanceof DotNetType)
			{
				PsiElement parent = temp.getParent();
				if(parent instanceof CSharpTypeDefStatement)
				{
					targetToWalkChildren = last = parent.getParent();
					break;
				}

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
				targetToWalkChildren = PsiTreeUtil.getParentOfType(temp, DotNetTypeDeclaration.class);
				if(targetToWalkChildren == null)
				{
					targetToWalkChildren = PsiTreeUtil.getParentOfType(temp, DotNetModifierListOwner.class);
				}
				break;
			}
			else if(temp instanceof CSharpMethodDeclaration && ((CSharpMethodDeclaration) temp).isLocal())
			{
				// nothing - go up
			}
			else if(temp instanceof DotNetFieldDeclaration || temp instanceof DotNetPropertyDeclaration || temp instanceof DotNetEventDeclaration || temp instanceof DotNetLikeMethodDeclaration)
			{
				last = temp.getParent();
				targetToWalkChildren = temp.getParent();
				break;
			}
			else if(temp instanceof DotNetXAccessor)
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
			else if(temp instanceof CSharpForInjectionFragmentHolder)
			{
				PsiLanguageInjectionHost.Place shreds = InjectedLanguageManager.getInstance(temp.getProject()).getShreds(temp.getContainingFile());
				if(shreds != null)
				{
					for(PsiLanguageInjectionHost.Shred shred : shreds)
					{
						temp = shred.getHost();
						continue loop;
					}
				}
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
			targetToWalkChildren = element.getContainingFile();
		}
		return Couple.of(last, targetToWalkChildren);
	}

	@RequiredReadAction
	public static boolean isSoft(@Nonnull CSharpReferenceExpression referenceExpression)
	{
		ResolveToKind kind = referenceExpression.kind();
		switch(kind)
		{
			case SOFT_QUALIFIED_NAMESPACE:
				return true;
			case METHOD:
			case ARRAY_METHOD:
			case ANY_MEMBER:
				DotNetExpression qualifier = referenceExpression.getQualifier();
				if(qualifier == null)
				{
					return false;
				}
				DotNetTypeRef typeRef = qualifier.toTypeRef(false);
				return typeRef instanceof CSharpDynamicTypeRef;
		}
		return false;
	}

	@Nonnull
	@RequiredReadAction
	public static DotNetTypeRef toTypeRef(@Nonnull GlobalSearchScope scope, @Nullable PsiElement resolve)
	{
		return toTypeRef(scope, resolve, DotNetGenericExtractor.EMPTY);
	}

	@Nonnull
	@RequiredReadAction
	public static DotNetTypeRef toTypeRef(@Nonnull GlobalSearchScope scope, @Nonnull ResolveResult resolveResult)
	{
		PsiElement element = resolveResult.getElement();
		DotNetGenericExtractor extractor = DotNetGenericExtractor.EMPTY;
		if(resolveResult instanceof CSharpResolveResultWithExtractor)
		{
			extractor = ((CSharpResolveResultWithExtractor) resolveResult).getExtractor();
		}
		return toTypeRef(scope, element, extractor);
	}

	@Nonnull
	@RequiredReadAction
	public static DotNetTypeRef toTypeRef(@Nonnull GlobalSearchScope resolveScope, @Nullable PsiElement resolvedElement, @Nonnull DotNetGenericExtractor extractor)
	{
		if(resolvedElement instanceof DotNetNamespaceAsElement)
		{
			return new CSharpTypeRefFromNamespace((DotNetNamespaceAsElement) resolvedElement, resolveScope);
		}
		else if(resolvedElement instanceof DotNetTypeDeclaration)
		{
			return new CSharpTypeRefByTypeDeclaration((DotNetTypeDeclaration) resolvedElement, extractor);
		}
		else if(resolvedElement instanceof CSharpTypeDefStatement)
		{
			return ((CSharpTypeDefStatement) resolvedElement).toTypeRef();
		}
		else if(resolvedElement instanceof DotNetGenericParameter)
		{
			return new CSharpTypeRefFromGenericParameter((DotNetGenericParameter) resolvedElement);
		}
		else if(resolvedElement instanceof CSharpMethodDeclaration)
		{
			return new CSharpLambdaTypeRef((CSharpMethodDeclaration) resolvedElement);
		}
		else if(resolvedElement instanceof DotNetVariable)
		{
			return ((DotNetVariable) resolvedElement).toTypeRef(true);
		}
		else if(resolvedElement instanceof CSharpElementGroup)
		{
			return new CSharpElementGroupTypeRef(resolveScope.getProject(), resolveScope, (CSharpElementGroup<?>) resolvedElement);
		}
		return DotNetTypeRef.ERROR_TYPE;
	}
}
