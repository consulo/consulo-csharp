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

package consulo.csharp.lang.psi.impl.source;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpGenericConstraint;
import consulo.csharp.lang.psi.CSharpGenericConstraintValue;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpStubElements;
import consulo.csharp.lang.psi.impl.stub.CSharpWithStringValueStub;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IStubElementType;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetGenericParameterListOwner;

/**
 * @author VISTALL
 * @since 30.11.13.
 */
public class CSharpGenericConstraintImpl extends CSharpStubElementImpl<CSharpWithStringValueStub<CSharpGenericConstraint>> implements
		CSharpGenericConstraint
{
	public CSharpGenericConstraintImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	public CSharpGenericConstraintImpl(@NotNull CSharpWithStringValueStub<CSharpGenericConstraint> stub,
			@NotNull IStubElementType<? extends CSharpWithStringValueStub<CSharpGenericConstraint>, ?> nodeType)
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

	@NotNull
	@Override
	public CSharpGenericConstraintValue[] getGenericConstraintValues()
	{
		return getStubOrPsiChildren(CSharpStubElements.GENERIC_CONSTRAINT_VALUES, CSharpGenericConstraintValue.ARRAY_FACTORY);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitGenericConstraint(this);
	}
}
