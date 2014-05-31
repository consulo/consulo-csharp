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

import org.consulo.lombok.annotations.ArrayFactoryFields;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.reflactoring.CSharpRefactoringUtil;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpLambdaParameter;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaTypeRef;
import org.mustbe.consulo.dotnet.psi.DotNetEventDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.util.ArrayUtil2;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 19.01.14
 */
@ArrayFactoryFields
public class CSharpLambdaParameterImpl extends CSharpVariableImpl implements CSharpLambdaParameter
{
	public CSharpLambdaParameterImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitLambdaParameter(this);
	}

	@NotNull
	@Override
	public DotNetTypeRef toTypeRef(boolean resolveFromInitializer)
	{
		DotNetType type = getType();
		if(type == null)
		{
			return resolveFromInitializer ? resolveTypeForParameter() : DotNetTypeRef.AUTO_TYPE;
		}

		return type.toTypeRef();
	}

	@Nullable
	@Override
	public DotNetType getType()
	{
		return findChildByClass(DotNetType.class);
	}

	@NotNull
	private DotNetTypeRef resolveTypeForParameter()
	{
		CSharpLambdaExpressionImpl lambdaExpression = PsiTreeUtil.getParentOfType(this, CSharpLambdaExpressionImpl.class);
		if(lambdaExpression == null)
		{
			return DotNetTypeRef.UNKNOWN_TYPE;
		}
		CSharpLambdaParameter[] parameters = ((CSharpLambdaParameterListImpl)getParent()).getParameters();

		int i = ArrayUtil.indexOf(parameters, this);

		DotNetTypeRef typeRef = resolveTypeForParameter(lambdaExpression, i);
		if(typeRef == null)
		{
			typeRef = DotNetTypeRef.UNKNOWN_TYPE;
		}
		return typeRef;
	}

	@Nullable
	private static DotNetTypeRef resolveTypeForParameter(CSharpLambdaExpressionImpl lambdaExpression, int parameterIndex)
	{
		PsiElement parent = lambdaExpression.getParent();
		if(parent instanceof DotNetVariable)
		{
			DotNetVariable variable = (DotNetVariable) parent;
			if(variable.getInitializer() != lambdaExpression)
			{
				return DotNetTypeRef.UNKNOWN_TYPE;
			}
			return fromVariable(variable, parameterIndex);
		}
		else if(parent instanceof CSharpMethodCallParameterListImpl)
		{
			CSharpMethodCallExpressionImpl methodCallExpression = (CSharpMethodCallExpressionImpl) parent.getParent();
			DotNetExpression callExpression = methodCallExpression.getCallExpression();
			if(!(callExpression instanceof CSharpReferenceExpressionImpl))
			{
				return DotNetTypeRef.UNKNOWN_TYPE;
			}
			PsiElement resolve = ((CSharpReferenceExpressionImpl) callExpression).resolve();
			if(!(resolve instanceof DotNetLikeMethodDeclaration))
			{
				return DotNetTypeRef.UNKNOWN_TYPE;
			}
			DotNetExpression[] parameterExpressions = methodCallExpression.getParameterExpressions();
			int index = ArrayUtil.indexOf(parameterExpressions, lambdaExpression);
			if(index == -1)
			{
				return DotNetTypeRef.UNKNOWN_TYPE;
			}

			return fromVariable(((DotNetLikeMethodDeclaration) resolve).getParameters()[index], parameterIndex);
		}
		else if(parent instanceof CSharpAssignmentExpressionImpl)
		{
			CSharpOperatorReferenceImpl operatorElement = ((CSharpAssignmentExpressionImpl) parent).getOperatorElement();
			if(operatorElement.getOperatorElementType() == CSharpTokens.PLUSEQ || operatorElement.getOperatorElementType() == CSharpTokens.MINUSEQ)
			{
				DotNetExpression[] expressions = ((CSharpAssignmentExpressionImpl) parent).getExpressions();

				DotNetExpression expression = expressions[0];
				if(expression instanceof CSharpReferenceExpression)
				{
					PsiElement resolve = ((CSharpReferenceExpression) expression).resolve();
					if(resolve instanceof DotNetEventDeclaration)
					{
						return fromVariable((DotNetVariable) resolve, parameterIndex);
					}
				}
			}
		}
		return DotNetTypeRef.UNKNOWN_TYPE;
	}

	@Nullable
	private static DotNetTypeRef fromVariable(DotNetVariable variable, int index)
	{
		DotNetTypeRef leftTypeRef = variable.toTypeRef(false);
		if(!(leftTypeRef instanceof CSharpLambdaTypeRef))
		{
			return DotNetTypeRef.UNKNOWN_TYPE;
		}
		DotNetTypeRef[] leftTypeParameters = ((CSharpLambdaTypeRef) leftTypeRef).getParameterTypes();
		return ArrayUtil2.safeGet(leftTypeParameters, index);
	}

	@Override
	public PsiElement setName(@NonNls @NotNull String s) throws IncorrectOperationException
	{
		CSharpRefactoringUtil.replaceNameIdentifier(this, s);
		return this;
	}

	@NotNull
	@Override
	public SearchScope getUseScope()
	{
		return new LocalSearchScope(getParent().getParent());
	}
}
