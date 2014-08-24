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
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpParameterImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpVariableStub;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import com.intellij.lang.ASTNode;

/**
 * @author VISTALL
 * @since 15.01.14
 */
public class CSharpParameterStubElementType extends CSharpVariableStubElementType<DotNetParameter>
{
	public CSharpParameterStubElementType()
	{
		super("PARAMETER");
	}

	@Override
	public DotNetParameter createElement(@NotNull ASTNode astNode)
	{
		return new CSharpParameterImpl(astNode);
	}

	@Override
	public DotNetParameter createPsi(@NotNull CSharpVariableStub<DotNetParameter> stub)
	{
		return new CSharpParameterImpl(stub);
	}
}