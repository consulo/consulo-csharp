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

package consulo.csharp.lang.psi.impl.stub.elementTypes;

import javax.annotation.Nonnull;

import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.EmptyStub;
import consulo.csharp.lang.psi.CSharpTupleType;
import consulo.csharp.lang.psi.impl.source.CSharpStubTupleTypeImpl;

/**
 * @author VISTALL
 * @since 26-Nov-16.
 */
public class CSharpTupleTypeStubElementType extends CSharpEmptyStubElementType<CSharpTupleType>
{
	public CSharpTupleTypeStubElementType()
	{
		super("TUPLE_TYPE");
	}

	@Nonnull
	@Override
	public CSharpTupleType createElement(@Nonnull ASTNode astNode)
	{
		return new CSharpStubTupleTypeImpl(astNode);
	}

	@Override
	public CSharpTupleType createPsi(@Nonnull EmptyStub<CSharpTupleType> stub)
	{
		return new CSharpStubTupleTypeImpl(stub, this);
	}
}