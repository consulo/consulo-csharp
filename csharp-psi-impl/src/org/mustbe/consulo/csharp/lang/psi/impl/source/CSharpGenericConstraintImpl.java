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
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraint;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraintValue;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpGenericConstraintStub;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterListOwner;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IStubElementType;
import lombok.val;

/**
 * @author VISTALL
 * @since 30.11.13.
 */
public class CSharpGenericConstraintImpl extends CSharpStubElementImpl<CSharpGenericConstraintStub> implements CSharpGenericConstraint
{
	public CSharpGenericConstraintImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	public CSharpGenericConstraintImpl(
			@NotNull CSharpGenericConstraintStub stub, @NotNull IStubElementType<? extends CSharpGenericConstraintStub, ?> nodeType)
	{
		super(stub, nodeType);
	}

	@Override
	public DotNetGenericParameter resolve()
	{
		CSharpGenericConstraintStub stub = getStub();
		if(stub != null)
		{
			DotNetGenericParameterListOwner parentOfType = getStubOrPsiParentOfType(DotNetGenericParameterListOwner.class);
			if(parentOfType == null)
			{
				return null;
			}

			for(DotNetGenericParameter parameter : parentOfType.getGenericParameters())
			{
				if(Comparing.equal(parameter.getName(), stub.getReferenceText()))
				{
					return parameter;
				}
			}
			return null;
		}
		val genericParameterReference = getGenericParameterReference();
		if(genericParameterReference == null)
		{
			return null;
		}
		PsiElement resolve = genericParameterReference.resolve();
		if(resolve instanceof DotNetGenericParameter)
		{
			return (DotNetGenericParameter) resolve;
		}
		else
		{
			return null;
		}
	}

	@Override
	@Nullable
	public CSharpReferenceExpression getGenericParameterReference()
	{
		return findChildByClass(CSharpReferenceExpression.class);
	}

	@NotNull
	@Override
	public CSharpGenericConstraintValue[] getGenericConstraintValues()
	{
		return findChildrenByClass(CSharpGenericConstraintValue.class);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitGenericConstraint(this);
	}
}
