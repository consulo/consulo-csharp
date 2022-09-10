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

import consulo.language.ast.ASTNode;
import consulo.util.lang.BitUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.source.CSharpFieldDeclarationImpl;
import consulo.csharp.lang.impl.psi.stub.CSharpVariableDeclStub;
import consulo.csharp.lang.impl.psi.stub.index.CSharpIndexKeys;
import consulo.dotnet.psi.DotNetFieldDeclaration;
import consulo.language.psi.stub.IndexSink;
import consulo.util.lang.StringUtil;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 21.12.13.
 */
public class CSharpFieldStubElementType extends CSharpQVariableStubElementType<DotNetFieldDeclaration>
{
	public CSharpFieldStubElementType()
	{
		super("FIELD_DECLARATION");
	}

	@Override
	protected boolean supportsInitializer(int modifiers)
	{
		return BitUtil.isSet(modifiers, CSharpVariableDeclStub.CONSTANT_MASK);
	}

	@Nonnull
	@Override
	public DotNetFieldDeclaration createElement(@Nonnull ASTNode astNode)
	{
		return new CSharpFieldDeclarationImpl(astNode);
	}

	@Override
	public DotNetFieldDeclaration createPsi(@Nonnull CSharpVariableDeclStub<DotNetFieldDeclaration> fieldStub)
	{
		return new CSharpFieldDeclarationImpl(fieldStub);
	}

	@Override
	@RequiredReadAction
	public void indexStub(@Nonnull CSharpVariableDeclStub<DotNetFieldDeclaration> stub, @Nonnull IndexSink indexSink)
	{
		String name = getNameWithoutAt(stub);
		if(!StringUtil.isEmpty(name))
		{
			indexSink.occurrence(CSharpIndexKeys.FIELD_INDEX, name);
		}
	}
}
