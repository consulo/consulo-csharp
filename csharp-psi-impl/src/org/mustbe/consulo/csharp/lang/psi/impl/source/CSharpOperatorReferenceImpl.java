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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgument;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentList;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpQualifiedNonReference;
import org.mustbe.consulo.csharp.lang.psi.CSharpSimpleLikeMethodAsElement;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokenSets;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.light.CSharpLightCallArgument;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.AsPsiElementProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.CSharpResolveOptions;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.ExecuteTarget;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.MemberResolveScopeProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.MethodResolveResult;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.StubElementResolveResult;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.WeightUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.MethodCalcResult;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.MethodResolver;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.operatorResolving.ImplicitCastInfo;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.operatorResolving.ImplicitOperatorArgumentAsCallArgumentWrapper;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpOperatorNameHelper;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpPointerTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpStaticTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.csharp.lang.psi.resolve.OperatorByTokenSelector;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetPointerTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import org.mustbe.consulo.dotnet.util.ArrayUtil2;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.ResolveState;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import lombok.val;

/**
 * @author VISTALL
 * @since 12.03.14
 */
public class CSharpOperatorReferenceImpl extends CSharpElementImpl implements PsiReference, PsiPolyVariantReference, CSharpCallArgumentListOwner,
		CSharpQualifiedNonReference
{
	private static class OurResolver implements ResolveCache.PolyVariantResolver<CSharpOperatorReferenceImpl>
	{
		private static final OurResolver INSTANCE = new OurResolver();

		@NotNull
		@Override
		public ResolveResult[] resolve(@NotNull CSharpOperatorReferenceImpl reference, boolean incompleteCode)
		{
			if(!incompleteCode)
			{
				return multiResolveImpl(reference);
			}
			else
			{
				ResolveResult[] resolveResults = reference.multiResolve(false);

				List<ResolveResult> filter = new SmartList<ResolveResult>();
				for(ResolveResult resolveResult : resolveResults)
				{
					if(resolveResult.isValidResult())
					{
						filter.add(resolveResult);
					}
				}
				return ContainerUtil.toArray(filter, ResolveResult.EMPTY_ARRAY);
			}
		}

		@NotNull
		private ResolveResult[] multiResolveImpl(CSharpOperatorReferenceImpl reference)
		{
			Object o = reference.resolveImpl();

			List<ResolveResult> elements = new SmartList<ResolveResult>();
			if(o instanceof List)
			{
				//noinspection unchecked
				List<Pair<MethodCalcResult, PsiElement>> list = (List<Pair<MethodCalcResult, PsiElement>>) o;
				for(Pair<MethodCalcResult, PsiElement> pair : list)
				{
					elements.add(new MethodResolveResult(pair.getSecond(), pair.getFirst()));
				}
			}
			else if(o instanceof PsiElement)
			{
				elements.add(new PsiElementResolveResult((PsiElement) o, true));
			}
			else if(o instanceof DotNetTypeRef)
			{
				elements.add(new StubElementResolveResult(reference, true, (DotNetTypeRef) o));
			}

			return ContainerUtil.toArray(elements, ResolveResult.EMPTY_ARRAY);
		}
	}

	private static final TokenSet ourMergeSet = TokenSet.orSet(CSharpTokenSets.OVERLOADING_OPERATORS, CSharpTokenSets.ASSIGNMENT_OPERATORS,
			TokenSet.create(CSharpTokens.ANDAND, CSharpTokens.OROR));

	private static final Map<IElementType, IElementType> ourAssignmentOperatorMap = new HashMap<IElementType, IElementType>()
	{
		{
			put(CSharpTokens.MULEQ, CSharpTokens.MUL);
			put(CSharpTokens.PLUSEQ, CSharpTokens.PLUS);
			put(CSharpTokens.MINUSEQ, CSharpTokens.MINUS);
			put(CSharpTokens.DIVEQ, CSharpTokens.DIV);
			put(CSharpTokens.GTEQ, CSharpTokens.GT);
			put(CSharpTokens.LTEQ, CSharpTokens.LT);
			put(CSharpTokens.GTGTEQ, CSharpTokens.GTGT);
			put(CSharpTokens.LTLTEQ, CSharpTokens.LTLT);
			put(CSharpTokens.ANDEQ, CSharpTokens.AND);
			put(CSharpTokens.OREQ, CSharpTokens.OR);
			put(CSharpTokens.XOREQ, CSharpTokens.XOR);
		}
	};

	public CSharpOperatorReferenceImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@Override
	public PsiReference getReference()
	{
		return this;
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitOperatorReference(this);
	}

	@Override
	public PsiElement getElement()
	{
		return this;
	}

	@Override
	public TextRange getRangeInElement()
	{
		PsiElement operator = getFirstOperator();

		int len = operator.getTextLength();

		IElementType operatorElementType = getOperatorElementType();
		if(operatorElementType == CSharpTokens.LTLT || operatorElementType == CSharpTokens.GTGT)
		{
			len += 1;
		}
		return new TextRange(0, len);
	}

	@NotNull
	public PsiElement getFirstOperator()
	{
		return findNotNullChildByFilter(ourMergeSet);
	}

	@Nullable
	@Override
	public PsiElement resolve()
	{
		ResolveResult[] resolveResults = multiResolve(true);
		return CSharpResolveUtil.findFirstValidElement(resolveResults);
	}

	private Object resolveImpl()
	{
		IElementType elementType = getOperatorElementType();
		// normalize
		IElementType normalized = ourAssignmentOperatorMap.get(elementType);
		if(normalized != null)
		{
			elementType = normalized;
		}

		PsiElement parent = getParent();

		if(parent instanceof CSharpPrefixExpressionImpl)
		{
			if(elementType == CSharpTokens.AND)
			{
				DotNetExpression dotNetExpression = ArrayUtil2.safeGet(getParameterExpressions(), 0);
				if(dotNetExpression == null)
				{
					return DotNetTypeRef.ERROR_TYPE;
				}
				return new CSharpPointerTypeRef(dotNetExpression.toTypeRef(true));
			}
			else if(elementType == CSharpTokens.MUL)
			{
				DotNetExpression dotNetExpression = ArrayUtil2.safeGet(getParameterExpressions(), 0);
				if(dotNetExpression == null)
				{
					return DotNetTypeRef.ERROR_TYPE;
				}
				DotNetTypeRef expressionTypeRef = dotNetExpression.toTypeRef(true);
				if(expressionTypeRef instanceof DotNetPointerTypeRef)
				{
					return ((DotNetPointerTypeRef) expressionTypeRef).getInnerTypeRef();
				}
				else
				{
					return DotNetTypeRef.ERROR_TYPE;
				}
			}
		}

		if(parent instanceof CSharpExpressionWithOperatorImpl)
		{
			if(elementType == CSharpTokenSets.OROR || elementType == CSharpTokens.ANDAND)
			{
				return new CSharpTypeRefByQName(DotNetTypes.System.Boolean);
			}

			DotNetExpression[] parameterExpressions = getParameterExpressions();
			if(elementType == CSharpTokenSets.EQ)
			{
				if(parameterExpressions.length > 0)
				{
					return parameterExpressions[0].toTypeRef(false);
				}
				return new CSharpTypeRefByQName(DotNetTypes.System.Void);
			}

			List<Pair<MethodCalcResult, PsiElement>> pairs = new SmartList<Pair<MethodCalcResult, PsiElement>>();

			for(DotNetExpression dotNetExpression : parameterExpressions)
			{
				DotNetTypeRef expressionTypeRef = dotNetExpression.toTypeRef(false);

				resolveUserDefinedOperators(elementType, expressionTypeRef, expressionTypeRef, pairs, null);

				for(DotNetTypeRef implicitTypeRef : getAllImplicitCasts(expressionTypeRef, parent))
				{
					resolveUserDefinedOperators(elementType, expressionTypeRef, implicitTypeRef, pairs, dotNetExpression);
				}
			}

			Collections.sort(pairs, WeightUtil.ourComparator);
			return pairs;
		}

		return null;
	}

	@NotNull
	private List<DotNetTypeRef> getAllImplicitCasts(DotNetTypeRef expressionTypeRef, PsiElement parent)
	{
		List<DotNetTypeRef> list = new SmartList<DotNetTypeRef>();
		for(DotNetExpression dotNetExpression : ((CSharpExpressionWithOperatorImpl) parent).getParameterExpressions())
		{
			List<DotNetTypeRef> implicitOrExplicitTypeRefs = CSharpTypeUtil.getImplicitOrExplicitTypeRefs(dotNetExpression.toTypeRef(true),
					expressionTypeRef, CSharpStaticTypeRef.IMPLICIT, this);

			list.addAll(implicitOrExplicitTypeRefs);
		}
		return list;
	}

	@NotNull
	public DotNetTypeRef resolveToTypeRef()
	{
		ResolveResult[] resolveResults = multiResolve(true);
		if(resolveResults.length == 0)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}

		ResolveResult resolveResult = CSharpResolveUtil.findValidOrFirstMaybeResult(resolveResults);

		if(resolveResult instanceof StubElementResolveResult)
		{
			return ((StubElementResolveResult) resolveResult).getTypeRef();
		}

		PsiElement element = resolveResult.getElement();

		if(element instanceof CSharpSimpleLikeMethodAsElement)
		{
			return ((CSharpSimpleLikeMethodAsElement) element).getReturnTypeRef();
		}
		else
		{
			return CSharpReferenceExpressionImplUtil.toTypeRef(element);
		}
	}

	@NotNull
	public IElementType getOperatorElementType()
	{
		return CSharpOperatorNameHelper.mergeTwiceOperatorIfNeed(getFirstOperator());
	}

	public void resolveUserDefinedOperators(@NotNull IElementType elementType,
			@NotNull DotNetTypeRef originalTypeRef,
			@NotNull DotNetTypeRef typeRef,
			@NotNull List<Pair<MethodCalcResult, PsiElement>> last,
			@Nullable DotNetExpression implicitExpression)
	{
		DotNetTypeResolveResult typeResolveResult = typeRef.resolve(this);

		PsiElement element = typeResolveResult.getElement();
		if(element == null)
		{
			return;
		}

		AsPsiElementProcessor psiElementProcessor = new AsPsiElementProcessor();
		MemberResolveScopeProcessor processor = new MemberResolveScopeProcessor(CSharpResolveOptions.build().element(this), psiElementProcessor,
				new ExecuteTarget[]{ExecuteTarget.ELEMENT_GROUP});

		ResolveState state = ResolveState.initial();
		state = state.put(CSharpResolveUtil.SELECTOR, new OperatorByTokenSelector(elementType));
		state = state.put(CSharpResolveUtil.EXTRACTOR, typeResolveResult.getGenericExtractor());
		CSharpResolveUtil.walkChildren(processor, element, false, true, state);

		List<PsiElement> psiElements = psiElementProcessor.getElements();
		if(psiElements.isEmpty())
		{
			return;
		}

		CSharpCallArgument[] arguments = getCallArguments(originalTypeRef, implicitExpression, typeRef);

		List<DotNetLikeMethodDeclaration> elements = CSharpResolveUtil.mergeGroupsToIterable(psiElements);
		for(DotNetLikeMethodDeclaration psiElement : elements)
		{
			MethodCalcResult calc = MethodResolver.calc(arguments, psiElement, this);
			last.add(Pair.<MethodCalcResult, PsiElement>create(calc, psiElement));
		}
	}

	@NotNull
	@Override
	public String getCanonicalText()
	{
		String operatorName = CSharpOperatorNameHelper.getOperatorName(getOperatorElementType());
		assert operatorName != null : getOperatorElementType();
		return operatorName;
	}

	@Override
	public PsiElement handleElementRename(String s) throws IncorrectOperationException
	{
		return null;
	}

	@Override
	public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException
	{
		return null;
	}

	@Override
	public boolean isReferenceTo(PsiElement element)
	{
		return resolve() == element;
	}

	@NotNull
	@Override
	public Object[] getVariants()
	{
		return new Object[0];
	}

	@Override
	public boolean isSoft()
	{
		return resolve() == this;
	}

	@NotNull
	public DotNetTypeRef[] getTypeRefs()
	{
		DotNetExpression[] parameterExpressions = getParameterExpressions();
		DotNetTypeRef[] typeRefs = new DotNetTypeRef[parameterExpressions.length];
		for(int i = 0; i < parameterExpressions.length; i++)
		{
			DotNetExpression parameterExpression = parameterExpressions[i];
			typeRefs[i] = parameterExpression.toTypeRef(true);
		}
		return typeRefs;
	}

	@Override
	public boolean canResolve()
	{
		return true;
	}

	@Nullable
	@Override
	public CSharpCallArgumentList getParameterList()
	{
		return null;
	}

	@Nullable
	@Override
	public PsiElement resolveToCallable()
	{
		return resolve();
	}

	@NotNull
	@Override
	public ResolveResult[] multiResolve(boolean incompleteCode)
	{
		return ResolveCache.getInstance(getProject()).resolveWithCaching(this, OurResolver.INSTANCE, false, incompleteCode);
	}

	@NotNull
	@Override
	public DotNetExpression[] getParameterExpressions()
	{
		PsiElement parent = getParent();
		if(parent instanceof CSharpExpressionWithOperatorImpl)
		{
			return ((CSharpExpressionWithOperatorImpl) parent).getParameterExpressions();
		}
		return DotNetExpression.EMPTY_ARRAY;
	}

	@NotNull
	@Override
	public CSharpCallArgument[] getCallArguments()
	{
		return getCallArguments(null, null, null);
	}

	@NotNull
	public CSharpCallArgument[] getCallArguments(DotNetTypeRef originalTypeRef, DotNetExpression wrapExpression, DotNetTypeRef toTypeRef)
	{
		DotNetExpression[] parameterExpressions = getParameterExpressions();
		CSharpCallArgument[] array = new CSharpCallArgument[parameterExpressions.length];
		for(int i = 0; i < parameterExpressions.length; i++)
		{
			DotNetExpression parameterExpression = parameterExpressions[i];
			if(parameterExpression == wrapExpression)
			{
				val wrapper = new ImplicitOperatorArgumentAsCallArgumentWrapper(wrapExpression, toTypeRef);

				wrapper.putUserData(ImplicitCastInfo.IMPLICIT_CAST_INFO, new ImplicitCastInfo(originalTypeRef, toTypeRef));
				array[i] = wrapper;
			}
			else
			{
				array[i] = new CSharpLightCallArgument(parameterExpression);
			}
		}
		return array;
	}

	@Nullable
	@Override
	public String getReferenceName()
	{
		throw new UnsupportedOperationException();
	}

	@Nullable
	@Override
	public String getReferenceNameWithAt()
	{
		throw new UnsupportedOperationException();
	}

	@Nullable
	@Override
	public PsiElement getQualifier()
	{
		return null;
	}
}
