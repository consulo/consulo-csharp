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
import consulo.csharp.lang.psi.impl.source.CSharpStubParameterListImpl;
import consulo.dotnet.psi.DotNetParameterList;
import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.EmptyStub;

/**
 * @author VISTALL
 * @since 15.01.14
 */
public class CSharpParameterListStubElementType extends CSharpEmptyStubElementType<DotNetParameterList>
{
	public CSharpParameterListStubElementType()
	{
		super("PARAMETER_LIST");
	}

	@Nonnull
	@Override
	public DotNetParameterList createElement(@Nonnull ASTNode astNode)
	{
		return new CSharpStubParameterListImpl(astNode);
	}

	@Override
	public DotNetParameterList createPsi(@Nonnull EmptyStub<DotNetParameterList> cSharpParameterListStub)
	{
		return new CSharpStubParameterListImpl(cSharpParameterListStub);
	}
}
