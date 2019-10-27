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
import com.intellij.psi.PsiElement;
import consulo.csharp.lang.psi.CSharpTupleVariable;
import consulo.csharp.lang.psi.impl.source.CSharpStubTupleVariableImpl;
import consulo.csharp.lang.psi.impl.stub.CSharpVariableDeclStub;

/**
 * @author VISTALL
 * @since 26-Nov-16.
 */
public class CSharpTupleVariableStubElementType extends CSharpBaseVariableStubElementType<CSharpTupleVariable>
{
	public CSharpTupleVariableStubElementType()
	{
		super("TUPLE_VARIABLE");
	}

	@Override
	public CSharpTupleVariable createPsi(@Nonnull CSharpVariableDeclStub<CSharpTupleVariable> stub)
	{
		return new CSharpStubTupleVariableImpl(stub, this);
	}

	@Nonnull
	@Override
	public PsiElement createElement(@Nonnull ASTNode astNode)
	{
		return new CSharpStubTupleVariableImpl(astNode);
	}
}
