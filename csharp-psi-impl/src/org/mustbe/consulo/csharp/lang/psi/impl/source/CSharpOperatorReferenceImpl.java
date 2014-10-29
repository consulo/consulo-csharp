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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentList;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpPseudoMethod;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokenSets;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.CSharpTransform;
import org.mustbe.consulo.csharp.lang.psi.impl.resolve.CSharpResolveContextUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.MethodAcceptorImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.StubElementResolveResult;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.WeightUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpOperatorNameHelper;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.lang.psi.impl.source.resolve.type.DotNetTypeRefByQName;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetTypeList;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 12.03.14
 */
public class CSharpOperatorReferenceImpl extends CSharpElementImpl implements PsiReference, PsiPolyVariantReference, CSharpCallArgumentListOwner,
		CSharpQualifiedNonReference
{
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

	private Object resolveImpl(@Nullable Ref<PsiElement> last)
	{
		IElementType elementType = getOperatorElementType();
		// normalize
		IElementType normalized = ourAssignmentOperatorMap.get(elementType);
		if(normalized != null)
		{
			elementType = normalized;
		}

		PsiElement parent = getParent();
		if(parent instanceof CSharpExpressionWithOperatorImpl)
		{
			if(elementType == CSharpTokenSets.OROR || elementType == CSharpTokens.ANDAND)
			{
				return new DotNetTypeRefByQName(DotNetTypes.System.Boolean, CSharpTransform.INSTANCE, false);
			}

			if(elementType == CSharpTokenSets.EQ)
			{
				DotNetExpression[] parameterExpressions = getParameterExpressions();
				if(parameterExpressions.length > 0)
				{
					return parameterExpressions[0].toTypeRef(false);
				}
				return new DotNetTypeRefByQName(DotNetTypes.System.Void, CSharpTransform.INSTANCE, false);
			}

			for(DotNetExpression dotNetExpression : ((CSharpExpressionWithOperatorImpl) parent).getParameterExpressions())
			{
				DotNetTypeRef dotNetTypeRef = dotNetExpression.toTypeRef(false);
				DotNetTypeResolveResult typeResolveResult = dotNetTypeRef.resolve(this);

				PsiElement element = typeResolveResult.getElement();

				if(element != null)
				{
					CSharpResolveContext context = CSharpResolveContextUtil.createContext(DotNetGenericExtractor.EMPTY, getResolveScope(), element);

					PsiElement resolvedElement = resolveByContext(elementType, context, last);
					if(resolvedElement != null)
					{
						return resolvedElement;
					}
				}
			}
		}

		DotNetVariable variable = CSharpLambdaExpressionImplUtil.resolveLambdaVariableInsideAssignmentExpression(parent);
		if(variable != null)
		{
			return variable;
		}

		return null;
	}

	@NotNull
	public DotNetTypeRef resolveToTypeRef()
	{
		ResolveResult[] resolveResults = multiResolve(true);
		if(resolveResults.length == 0)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}

		ResolveResult resolveResult = resolveResults[0];
		if(resolveResult instanceof StubElementResolveResult)
		{
			return ((StubElementResolveResult) resolveResult).getTypeRef();
		}

		PsiElement element = resolveResult.getElement();

		if(element instanceof CSharpPseudoMethod)
		{
			return ((CSharpPseudoMethod) element).getReturnTypeRef();
		}
		else
		{
			return CSharpReferenceExpressionImpl.toTypeRef(element);
		}
	}

	@NotNull
	public IElementType getOperatorElementType()
	{
		return CSharpOperatorNameHelper.mergeTwiceOperatorIfNeed(getFirstOperator());
	}

	@Nullable
	public PsiElement resolveByContext(IElementType elementType, CSharpResolveContext context, Ref<PsiElement> last)
	{
		CSharpElementGroup<CSharpMethodDeclaration> operatorGroupByTokenType = context.findOperatorGroupByTokenType(elementType);
		if(operatorGroupByTokenType == null)
		{
			return null;
		}

		List<Pair<Integer, DotNetLikeMethodDeclaration>> list = new ArrayList<Pair<Integer, DotNetLikeMethodDeclaration>>();
		for(PsiElement psiElement : operatorGroupByTokenType.getElements())
		{
			if(psiElement instanceof DotNetLikeMethodDeclaration)
			{
				int i = MethodAcceptorImpl.calcAcceptableWeight(this, this, (DotNetLikeMethodDeclaration) psiElement);

				list.add(Pair.create(i, (DotNetLikeMethodDeclaration) psiElement));
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

		Pair<Integer, DotNetLikeMethodDeclaration> pair = ContainerUtil.getFirstItem(list);
		if(pair == null)
		{
			return null;
		}

		if(pair.getFirst() != WeightUtil.MAX_WEIGHT)
		{
			if(last != null)
			{
				last.setIfNull(pair.getSecond());
			}
		}
		else
		{
			return pair.getSecond();
		}
		return null;
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
	public DotNetTypeList getTypeArgumentList()
	{
		return null;
	}

	@NotNull
	@Override
	public DotNetTypeRef[] getTypeArgumentListRefs()
	{
		DotNetExpression[] parameterExpressions = getParameterExpressions();
		if(parameterExpressions.length == 0)
		{
			return DotNetTypeRef.EMPTY_ARRAY;
		}
		return new DotNetTypeRef[]{parameterExpressions[0].toTypeRef(false)};
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
		return ResolveCache.getInstance(getProject()).resolveWithCaching(this, new ResolveCache.PolyVariantResolver<CSharpOperatorReferenceImpl>()
		{
			@NotNull
			@Override
			public ResolveResult[] resolve(@NotNull CSharpOperatorReferenceImpl reference, boolean incompleteCode)
			{
				Ref<PsiElement> psiElementRef = new Ref<PsiElement>();
				Object o = resolveImpl(psiElementRef);

				List<ResolveResult> elements = new SmartList<ResolveResult>();
				if(o instanceof PsiElement)
				{
					elements.add(new PsiElementResolveResult((PsiElement) o, true));
				}
				else if(o instanceof DotNetTypeRef)
				{
					elements.add(new StubElementResolveResult(reference, true, (DotNetTypeRef) o));
				}

				if(!incompleteCode)
				{
					PsiElement psiElement = psiElementRef.get();
					if(psiElement != null)
					{
						elements.add(new PsiElementResolveResult(psiElement, false));
					}
				}
				return ContainerUtil.toArray(elements, ResolveResult.EMPTY_ARRAY);
			}
		}, false, incompleteCode);
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

	@Nullable
	@Override
	public String getReferenceName()
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
