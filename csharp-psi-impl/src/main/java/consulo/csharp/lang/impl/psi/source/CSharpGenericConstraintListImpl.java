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

import javax.annotation.Nonnull;

import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpGenericConstraint;
import consulo.csharp.lang.psi.CSharpGenericConstraintList;
import consulo.csharp.lang.impl.psi.CSharpStubElements;
import consulo.language.psi.stub.EmptyStub;
import consulo.language.ast.ASTNode;
import consulo.language.psi.stub.IStubElementType;

/**
 * @author VISTALL
 * @since 30.11.13.
 */
public class CSharpGenericConstraintListImpl extends CSharpStubElementImpl<EmptyStub<CSharpGenericConstraintList>> implements
		CSharpGenericConstraintList
{
	public CSharpGenericConstraintListImpl(@Nonnull ASTNode node)
	{
		super(node);
	}

	public CSharpGenericConstraintListImpl(@Nonnull EmptyStub<CSharpGenericConstraintList> stub,
			@Nonnull IStubElementType<? extends EmptyStub<CSharpGenericConstraintList>, ?> nodeType)
	{
		super(stub, nodeType);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitGenericConstraintList(this);
	}

	@Nonnull
	@Override
	public CSharpGenericConstraint[] getGenericConstraints()
	{
		return getStubOrPsiChildren(CSharpStubElements.GENERIC_CONSTRAINT, CSharpGenericConstraint.ARRAY_FACTORY);
	}
}
