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

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.impl.source.CSharpPropertyDeclarationImpl;
import consulo.csharp.lang.psi.impl.stub.CSharpVariableDeclStub;
import consulo.csharp.lang.psi.impl.stub.index.CSharpIndexKeys;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.stubs.IndexSink;

/**
 * @author VISTALL
 * @since 21.12.13.
 */
public class CSharpPropertyElementType extends CSharpQVariableStubElementType<CSharpPropertyDeclarationImpl>
{
	public CSharpPropertyElementType()
	{
		super("PROPERTY_DECLARATION");
	}

	@Nonnull
	@Override
	public CSharpPropertyDeclarationImpl createElement(@Nonnull ASTNode astNode)
	{
		return new CSharpPropertyDeclarationImpl(astNode);
	}

	@Override
	public CSharpPropertyDeclarationImpl createPsi(@Nonnull CSharpVariableDeclStub<CSharpPropertyDeclarationImpl> cSharpPropertyStub)
	{
		return new CSharpPropertyDeclarationImpl(cSharpPropertyStub);
	}

	@Override
	@RequiredReadAction
	public void indexStub(@Nonnull CSharpVariableDeclStub<CSharpPropertyDeclarationImpl> stub, @Nonnull IndexSink indexSink)
	{
		String name = getName(stub);
		if(!StringUtil.isEmpty(name))
		{
			indexSink.occurrence(CSharpIndexKeys.PROPERTY_INDEX, name);
		}
	}
}
