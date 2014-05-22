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

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaTypeRef;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetReferenceExpression;
import org.mustbe.consulo.dotnet.psi.DotNetReferenceType;
import org.mustbe.consulo.dotnet.resolve.DotNetPsiFacade;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 28.11.13.
 */
public class CSharpReferenceTypeImpl extends CSharpElementImpl implements DotNetReferenceType
{
	public CSharpReferenceTypeImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitReferenceType(this);
	}

	@NotNull
	@Override
	public DotNetTypeRef toTypeRef()
	{
		DotNetReferenceExpression referenceExpression = getReferenceExpression();

		PsiElement resolve = referenceExpression.resolve();
		if(resolve instanceof DotNetLikeMethodDeclaration)
		{
			DotNetLikeMethodDeclaration methodDeclaration = (DotNetLikeMethodDeclaration) resolve;
			return new CSharpLambdaTypeRef(resolve, methodDeclaration.getParameterTypesForRuntime(), methodDeclaration.getReturnTypeRef());
		}
		return CSharpReferenceExpressionImpl.toTypeRef(resolve);
	}

	@NotNull
	@Override
	public DotNetPsiFacade.TypeResoleKind getTypeResoleKind()
	{
		return DotNetPsiFacade.TypeResoleKind.UNKNOWN;
	}

	@NotNull
	@Override
	public String getReferenceText()
	{
		DotNetReferenceExpression referenceExpression = getReferenceExpression();
		return referenceExpression.getText();
	}

	@NotNull
	@Override
	public DotNetReferenceExpression getReferenceExpression()
	{
		return findNotNullChildByClass(DotNetReferenceExpression.class);
	}
}
