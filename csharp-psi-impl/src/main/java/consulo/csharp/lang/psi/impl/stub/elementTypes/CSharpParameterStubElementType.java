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

import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.intellij.lang.ASTNode;
import consulo.csharp.lang.psi.impl.source.CSharpStubParameterImpl;
import consulo.csharp.lang.psi.impl.stub.CSharpVariableDeclStub;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetParameter;

/**
 * @author VISTALL
 * @since 15.01.14
 */
public class CSharpParameterStubElementType extends CSharpVariableStubElementType<DotNetParameter>
{
	private static final Function<DotNetParameter, String> ourDefaultInitializerTextGetter = p ->
	{
		DotNetExpression initializer = p.getInitializer();
		if(initializer != null)
		{
			return initializer.getText();
		}
		return null;
	};

	public CSharpParameterStubElementType()
	{
		super("PARAMETER");
	}

	@Nonnull
	@Override
	public DotNetParameter createElement(@Nonnull ASTNode astNode)
	{
		return new CSharpStubParameterImpl(astNode);
	}

	@Override
	public DotNetParameter createPsi(@Nonnull CSharpVariableDeclStub<DotNetParameter> stub)
	{
		return new CSharpStubParameterImpl(stub);
	}

	@Nullable
	@Override
	protected Function<DotNetParameter, String> getInitializerGetter()
	{
		return ourDefaultInitializerTextGetter;
	}
}