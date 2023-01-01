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

package consulo.csharp.lang.impl.psi.stub.elementTypes;

import javax.annotation.Nonnull;

import consulo.csharp.lang.psi.CSharpGenericConstraintList;
import consulo.csharp.lang.impl.psi.source.CSharpGenericConstraintListImpl;
import consulo.language.ast.ASTNode;
import consulo.language.psi.stub.EmptyStub;

/**
 * @author VISTALL
 * @since 10.06.14
 */
public class CSharpGenericConstraintListStubElementType extends CSharpEmptyStubElementType<CSharpGenericConstraintList>
{
	public CSharpGenericConstraintListStubElementType()
	{
		super("GENERIC_CONSTRAINT_LIST");
	}

	@Nonnull
	@Override
	public CSharpGenericConstraintList createElement(@Nonnull ASTNode astNode)
	{
		return new CSharpGenericConstraintListImpl(astNode);
	}

	@Override
	public CSharpGenericConstraintList createPsi(@Nonnull EmptyStub<CSharpGenericConstraintList> cSharpGenericConstraintListStub)
	{
		return new CSharpGenericConstraintListImpl(cSharpGenericConstraintListStub, this);
	}
}
