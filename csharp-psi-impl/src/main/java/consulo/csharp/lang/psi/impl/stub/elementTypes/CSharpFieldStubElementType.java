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

import org.jetbrains.annotations.NotNull;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.impl.source.CSharpFieldDeclarationImpl;
import consulo.csharp.lang.psi.impl.stub.CSharpVariableDeclStub;
import consulo.csharp.lang.psi.impl.stub.index.CSharpIndexKeys;
import consulo.dotnet.psi.DotNetFieldDeclaration;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.stubs.IndexSink;

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

	@NotNull
	@Override
	public DotNetFieldDeclaration createElement(@NotNull ASTNode astNode)
	{
		return new CSharpFieldDeclarationImpl(astNode);
	}

	@Override
	public DotNetFieldDeclaration createPsi(@NotNull CSharpVariableDeclStub<DotNetFieldDeclaration> fieldStub)
	{
		return new CSharpFieldDeclarationImpl(fieldStub);
	}

	@Override
	@RequiredReadAction
	public void indexStub(@NotNull CSharpVariableDeclStub<DotNetFieldDeclaration> stub, @NotNull IndexSink indexSink)
	{
		String name = getName(stub);
		if(!StringUtil.isEmpty(name))
		{
			indexSink.occurrence(CSharpIndexKeys.FIELD_INDEX, name);
		}
	}
}