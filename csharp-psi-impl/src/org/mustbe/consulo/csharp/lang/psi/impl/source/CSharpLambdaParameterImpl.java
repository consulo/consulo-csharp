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
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
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

		return CSharpLambdaExpressionImplUtil.resolveTypeForParameter(lambdaExpression, i);
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
