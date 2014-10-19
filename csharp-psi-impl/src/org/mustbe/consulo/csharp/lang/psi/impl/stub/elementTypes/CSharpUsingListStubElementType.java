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

package org.mustbe.consulo.csharp.lang.psi.impl.stub.elementTypes;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpUsingList;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpUsingListImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpEmptyStub;
import com.intellij.lang.ASTNode;

/**
 * @author VISTALL
 * @since 15.01.14
 */
public class CSharpUsingListStubElementType extends CSharpEmptyStubElementType<CSharpUsingList>
{
	public CSharpUsingListStubElementType()
	{
		super("USING_LIST");
	}

	@NotNull
	@Override
	public CSharpUsingListImpl createElement(@NotNull ASTNode astNode)
	{
		return new CSharpUsingListImpl(astNode);
	}

	@Override
	public CSharpUsingListImpl createPsi(@NotNull CSharpEmptyStub<CSharpUsingList> stub)
	{
		return new CSharpUsingListImpl(stub);
	}
}
