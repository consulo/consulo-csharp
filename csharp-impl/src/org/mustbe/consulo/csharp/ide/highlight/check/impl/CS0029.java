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

package org.mustbe.consulo.csharp.ide.highlight.check.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpArrayMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.CSharpTransform;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpAssignmentExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpOperatorReferenceImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpReturnStatementImpl;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.lang.psi.impl.source.resolve.type.DotNetTypeRefByQName;
import org.mustbe.consulo.dotnet.psi.DotNetConstructorDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetModifierListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import org.mustbe.consulo.dotnet.psi.DotNetXXXAccessor;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.util.Trinity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * @author VISTALL
 * @since 15.05.14
 */
public class CS0029 extends CompilerCheck<PsiElement>
{
	@Nullable
	@Override
	public CompilerCheckResult checkImpl(
			@NotNull CSharpLanguageVersion languageVersion, @NotNull PsiElement element)
	{
		Trinity<DotNetTypeRef, DotNetTypeRef, ? extends PsiElement> resolve = resolve(element);
		if(resolve == null)
		{
			return null;
		}

		if(resolve.getFirst() == DotNetTypeRef.AUTO_TYPE ||
				CSharpTypeUtil.haveErrorType(resolve.getFirst()) ||
				CSharpTypeUtil.haveErrorType(resolve.getSecond()))
		{
			return null;
		}

		if(!CSharpTypeUtil.isInheritable(resolve.getFirst(), resolve.getSecond(), element))
		{
			return result(resolve.getThird(), resolve.getSecond().getQualifiedText(), resolve.getFirst().getQualifiedText());
		}

		return null;
	}

	private Trinity<DotNetTypeRef, DotNetTypeRef, ? extends PsiElement> resolve(PsiElement element)
	{
		if(element instanceof DotNetVariable)
		{
			DotNetExpression initializer = ((DotNetVariable) element).getInitializer();
			if(initializer == null)
			{
				return null;
			}
			return Trinity.create(((DotNetVariable) element).toTypeRef(false), initializer.toTypeRef(false), initializer);
		}
		else if(element instanceof CSharpAssignmentExpressionImpl)
		{
			CSharpOperatorReferenceImpl operatorElement = ((CSharpAssignmentExpressionImpl) element).getOperatorElement();
			if(operatorElement.getOperatorElementType() != CSharpTokens.EQ)
			{
				return null;
			}
			DotNetExpression[] expressions = ((CSharpAssignmentExpressionImpl) element).getParameterExpressions();
			if(expressions.length != 2)
			{
				return null;
			}
			return Trinity.create(expressions[0].toTypeRef(false), expressions[1].toTypeRef(false), expressions[1]);
		}
		else if(element instanceof CSharpReturnStatementImpl)
		{
			DotNetModifierListOwner modifierListOwner = PsiTreeUtil.getParentOfType(element, DotNetModifierListOwner.class);
			if(modifierListOwner == null)
			{
				return null;
			}

			DotNetTypeRef expected = null;
			if(modifierListOwner instanceof DotNetConstructorDeclaration)
			{
				expected = new DotNetTypeRefByQName(DotNetTypes.System.Void, CSharpTransform.INSTANCE, false);
			}
			else if(modifierListOwner instanceof DotNetMethodDeclaration)
			{
				expected = ((DotNetMethodDeclaration) modifierListOwner).getReturnTypeRef();
			}
			else if(modifierListOwner instanceof DotNetXXXAccessor)
			{
				PsiElement parentOfAccessor = modifierListOwner.getParent();
				if(parentOfAccessor instanceof DotNetVariable)
				{
					expected = ((DotNetVariable) parentOfAccessor).toTypeRef(false);
				}
				else if(parentOfAccessor instanceof CSharpArrayMethodDeclaration)
				{
					expected = ((CSharpArrayMethodDeclaration) parentOfAccessor).getReturnTypeRef();
				}
			}

			if(expected == null)
			{
				return null;
			}

			DotNetTypeRef actual = null;
			DotNetExpression expression = ((CSharpReturnStatementImpl) element).getExpression();
			if(expression == null)
			{
				actual = new DotNetTypeRefByQName(DotNetTypes.System.Void, CSharpTransform.INSTANCE, false);
			}
			else
			{
				actual = expression.toTypeRef(false);
			}
			return Trinity.create(expected, actual, expression == null ? element : expression);
		}
		return null;
	}
}
