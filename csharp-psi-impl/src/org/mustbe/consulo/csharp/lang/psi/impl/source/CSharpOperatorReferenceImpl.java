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
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpEventUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.CSharpTransform;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.MethodAcceptorImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.OperatorResolveScopeProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.ResolveResultWithWeight;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.WeightProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpOperatorHelper;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpOperatorHelperImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.wrapper.GenericUnwrapTool;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.lang.psi.impl.source.resolve.type.DotNetTypeRefByQName;
import org.mustbe.consulo.dotnet.psi.DotNetEventDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetTypeList;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Couple;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.ResolveState;
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
public class CSharpOperatorReferenceImpl extends CSharpElementImpl implements PsiReference, CSharpCallArgumentListOwner
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
		Object o = resolve0(null);
		if(o instanceof PsiElement)
		{
			return (PsiElement) o;
		}
		return null;
	}

	private Object resolve0(@Nullable Ref<PsiElement> last)
	{
		PsiElement parent = getParent();
		if(parent instanceof CSharpExpressionWithOperatorImpl)
		{
			Object returnTypeInStubs = findInStubs(last);
			if(returnTypeInStubs != null)
			{
				return returnTypeInStubs;
			}
		}

		DotNetEventDeclaration eventDeclaration = CSharpEventUtil.resolveEvent(parent);
		if(eventDeclaration != null)
		{
			return eventDeclaration;
		}

		Couple<PsiElement> resolveLayers = CSharpReferenceExpressionImpl.getResolveLayers(this, false);

		OperatorResolveScopeProcessor processor = new OperatorResolveScopeProcessor(new Condition<DotNetNamedElement>()
		{
			@Override
			public boolean value(DotNetNamedElement dotNetNamedElement)
			{
				return isAccepted(CSharpOperatorReferenceImpl.this, dotNetNamedElement);
			}
		}, new WeightProcessor<DotNetNamedElement>()
		{
			@Override
			public int getWeight(@NotNull DotNetNamedElement element)
			{
				return MethodAcceptorImpl.calcAcceptableWeight(CSharpOperatorReferenceImpl.this, CSharpOperatorReferenceImpl.this,
						(CSharpMethodDeclaration) element);
			}
		}
		);

		CSharpResolveUtil.walkChildren(processor, resolveLayers.getSecond(), true, null, ResolveState.initial());

		final ResolveResultWithWeight[] resultWithWeights = processor.toResolveResults();
		if(resultWithWeights.length > 0 && resultWithWeights[0].isGoodResult())
		{
			return resultWithWeights[0].getElement();
		}
		return null;
	}

	@NotNull
	public DotNetTypeRef resolveToTypeRef()
	{
		Object o = resolve0(null);
		if(o instanceof DotNetTypeRef)
		{
			return (DotNetTypeRef) o;
		}
		else if(o instanceof CSharpPseudoMethod)
		{
			return ((CSharpPseudoMethod) o).getReturnTypeRef();
		}
		else if(o instanceof PsiElement)
		{
			return CSharpReferenceExpressionImpl.toTypeRef((PsiElement) o);
		}
		return DotNetTypeRef.ERROR_TYPE;
	}

	@NotNull
	public IElementType getOperatorElementType()
	{
		return CSharpOperatorHelper.mergeTwiceOperatorIfNeed(getFirstOperator());
	}

	private Object findInStubs(@Nullable Ref<PsiElement> last)
	{
		IElementType elementType = getOperatorElementType();
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

		CSharpOperatorHelper operatorHelper = CSharpOperatorHelper.getInstance(getProject());

		List<DotNetNamedElement> stubMembers = operatorHelper.getStubMembers();
		for(DotNetNamedElement dotNetNamedElement : stubMembers)
		{
			if(!isAccepted(this, dotNetNamedElement))
			{
				continue;
			}

			int genericParametersCount = ((CSharpMethodDeclaration) dotNetNamedElement).getGenericParametersCount();
			if(genericParametersCount > 0)
			{
				val extractorFromCall = MethodAcceptorImpl.createExtractorFromCall(this, (DotNetGenericParameterListOwner) dotNetNamedElement);
				dotNetNamedElement = GenericUnwrapTool.extract(dotNetNamedElement, extractorFromCall, true);
			}

			int i = MethodAcceptorImpl.calcAcceptableWeight(this, this, (CSharpMethodDeclaration) dotNetNamedElement);
			if(i == WeightProcessor.MAX_WEIGHT)
			{
				return dotNetNamedElement;
			}

			if(i > 0 && last != null)
			{
				last.setIfNull(dotNetNamedElement);
			}
		}
		return null;
	}

	private static boolean isAccepted(CSharpOperatorReferenceImpl reference, PsiElement element)
	{
		if(!(element instanceof CSharpMethodDeclaration))
		{
			return false;
		}
		val methodDeclaration = (CSharpMethodDeclaration) element;
		if(!methodDeclaration.isOperator())
		{
			return false;
		}

		IElementType elementType = reference.getOperatorElementType();

		// normalize
		IElementType normalized = ourAssignmentOperatorMap.get(elementType);
		if(normalized != null)
		{
			elementType = normalized;
		}

		return methodDeclaration.getOperatorElementType() == elementType;
	}

	@NotNull
	@Override
	public String getCanonicalText()
	{
		String operatorName = CSharpOperatorHelperImpl.getInstance(getProject()).getOperatorName(getOperatorElementType());
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
		return resolve0(null) instanceof DotNetTypeRef;
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

	@Override
	public ResolveResult[] multiResolve(boolean incompleteCode)
	{
		Ref<PsiElement> psiElementRef = new Ref<PsiElement>();
		Object o = resolve0(psiElementRef);

		List<ResolveResult> elements = new SmartList<ResolveResult>();
		if(o instanceof PsiElement)
		{
			elements.add(new ResolveResultWithWeight((PsiElement) o, WeightProcessor.MAX_WEIGHT));
		}
		else if(o != null)
		{
			elements.add(new ResolveResultWithWeight(this, WeightProcessor.MAX_WEIGHT));
		}

		if(!incompleteCode)
		{
			PsiElement psiElement = psiElementRef.get();
			if(psiElement != null)
			{
				elements.add(new ResolveResultWithWeight(psiElement, 1));
			}
		}
		return ContainerUtil.toArray(elements, ResolveResult.EMPTY_ARRAY);
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
}
