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
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpPseudoMethod;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokenSets;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpEventUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.MethodAcceptorImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.OperatorResolveScopeProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.ResolveResultWithWeight;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.WeightProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpNativeTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpOperatorHelper;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.dotnet.psi.DotNetEventDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Couple;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveState;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.IncorrectOperationException;
import lombok.val;

/**
 * @author VISTALL
 * @since 12.03.14
 */
public class CSharpOperatorReferenceImpl extends CSharpElementImpl implements PsiReference, CSharpExpressionWithParameters
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
		PsiElement operator = getOperator();
		return new TextRange(0, operator.getTextLength());
	}

	@NotNull
	public PsiElement getOperator()
	{
		return findNotNullChildByFilter(ourMergeSet);
	}

	@Nullable
	@Override
	public PsiElement resolve()
	{
		Object o = resolve0();
		if(o instanceof PsiElement)
		{
			return (PsiElement) o;
		}
		return null;
	}

	private Object resolve0()
	{
		PsiElement parent = getParent();
		if(parent instanceof CSharpBinaryExpressionImpl ||
				parent instanceof CSharpAssignmentExpressionImpl ||
				parent instanceof CSharpPrefixExpressionImpl ||
				parent instanceof CSharpPostfixExpressionImpl)
		{
			DotNetTypeRef returnTypeInStubs = findReturnTypeInStubs();
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
				return MethodAcceptorImpl.calcAcceptableWeight(CSharpOperatorReferenceImpl.this, (CSharpMethodDeclaration) element);
			}
		});

		CSharpResolveUtil.walkChildren(processor, resolveLayers.getSecond(), false, null, ResolveState.initial());

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
		Object o = resolve0();
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
		return getOperator().getNode().getElementType();
	}

	private DotNetTypeRef findReturnTypeInStubs()
	{
		IElementType elementType = getOperatorElementType();
		if(elementType == CSharpTokenSets.OROR || elementType == CSharpTokens.ANDAND)
		{
			return CSharpNativeTypeRef.BOOL;
		}
		if(elementType == CSharpTokenSets.EQ)
		{
			return CSharpNativeTypeRef.VOID;
		}

		CSharpOperatorHelper operatorHelper = CSharpOperatorHelper.getInstance(getProject());

		for(DotNetNamedElement dotNetNamedElement : operatorHelper.getStubMembers())
		{
			if(!isAccepted(this, dotNetNamedElement))
			{
				continue;
			}

			if(MethodAcceptorImpl.calcAcceptableWeight(this, (CSharpMethodDeclaration) dotNetNamedElement) == WeightProcessor.MAX_WEIGHT)
			{
				return ((CSharpMethodDeclaration) dotNetNamedElement).getReturnTypeRef();
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
		return getOperator().getText();
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
		return resolve0() instanceof DotNetTypeRef;
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

	@NotNull
	@Override
	public DotNetExpression[] getParameterExpressions()
	{
		PsiElement parent = getParent();
		if(parent instanceof CSharpBinaryExpressionImpl)
		{
			DotNetExpression leftExpression = ((CSharpBinaryExpressionImpl) parent).getLeftExpression();
			DotNetExpression rightExpression = ((CSharpBinaryExpressionImpl) parent).getRightExpression();
			if(rightExpression == null)
			{
				return new DotNetExpression[]{leftExpression};
			}
			return new DotNetExpression[]{
					leftExpression,
					rightExpression
			};
		}
		else if(parent instanceof CSharpPrefixExpressionImpl)
		{
			DotNetExpression expression = ((CSharpPrefixExpressionImpl) parent).getExpression();
			if(expression != null)
			{
				return new DotNetExpression[] {expression};
			}
		}
		else if(parent instanceof CSharpPostfixExpressionImpl)
		{
			DotNetExpression expression = ((CSharpPostfixExpressionImpl) parent).getExpression();
			if(expression != null)
			{
				return new DotNetExpression[] {expression};
			}
		}
		else if(parent instanceof CSharpAssignmentExpressionImpl)
		{
			return ((CSharpAssignmentExpressionImpl) parent).getExpressions();
		}
		return DotNetExpression.EMPTY_ARRAY;
	}
}
