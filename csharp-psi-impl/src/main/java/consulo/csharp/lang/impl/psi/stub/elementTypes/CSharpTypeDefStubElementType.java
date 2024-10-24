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

import jakarta.annotation.Nonnull;
import consulo.csharp.lang.psi.CSharpTypeDefStatement;
import consulo.csharp.lang.impl.psi.source.CSharpTypeDefStatementImpl;
import consulo.language.ast.ASTNode;
import consulo.language.psi.stub.EmptyStub;

/**
 * @author VISTALL
 * @since 11.02.14
 */
public class CSharpTypeDefStubElementType extends CSharpEmptyStubElementType<CSharpTypeDefStatement>
{
	public CSharpTypeDefStubElementType()
	{
		super("TYPE_DEF");
	}

	@Nonnull
	@Override
	public CSharpTypeDefStatement createElement(@Nonnull ASTNode astNode)
	{
		return new CSharpTypeDefStatementImpl(astNode);
	}

	@Override
	public CSharpTypeDefStatement createPsi(@Nonnull EmptyStub<CSharpTypeDefStatement> stub)
	{
		return new CSharpTypeDefStatementImpl(stub);
	}
}
