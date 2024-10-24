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

import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.impl.psi.CSharpStubElementSets;
import consulo.csharp.lang.impl.psi.stub.CSharpWithStringValueStub;
import consulo.csharp.lang.psi.CSharpGenericConstraint;
import consulo.csharp.lang.psi.CSharpGenericConstraintValue;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetGenericParameterListOwner;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.stub.IStubElementType;
import consulo.util.lang.Comparing;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 30.11.13.
 */
public class CSharpGenericConstraintImpl extends CSharpStubElementImpl<CSharpWithStringValueStub<CSharpGenericConstraint>> implements
		CSharpGenericConstraint
{
	public CSharpGenericConstraintImpl(@Nonnull ASTNode node)
	{
		super(node);
	}

	public CSharpGenericConstraintImpl(@Nonnull CSharpWithStringValueStub<CSharpGenericConstraint> stub,
			@Nonnull IStubElementType<? extends CSharpWithStringValueStub<CSharpGenericConstraint>, ?> nodeType)
	{
		super(stub, nodeType);
	}

	@Override
	public DotNetGenericParameter resolve()
	{
		CSharpWithStringValueStub<CSharpGenericConstraint> stub = getGreenStub();
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
		CSharpReferenceExpression genericParameterReference = getGenericParameterReference();
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

	@Nonnull
	@Override
	public CSharpGenericConstraintValue[] getGenericConstraintValues()
	{
		return getStubOrPsiChildren(CSharpStubElementSets.GENERIC_CONSTRAINT_VALUES, CSharpGenericConstraintValue.ARRAY_FACTORY);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitGenericConstraint(this);
	}
}
