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
import consulo.annotations.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpEnumConstantDeclarationImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpVariableDeclStub;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.index.CSharpIndexKeys;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.stubs.IndexSink;

/**
 * @author VISTALL
 * @since 08.01.13
 */
public class CSharpEnumConstantStubElementType extends CSharpQVariableStubElementType<CSharpEnumConstantDeclarationImpl>
{
	public CSharpEnumConstantStubElementType()
	{
		super("ENUM_CONSTANT_DECLARATION");
	}

	@NotNull
	@Override
	public CSharpEnumConstantDeclarationImpl createElement(@NotNull ASTNode astNode)
	{
		return new CSharpEnumConstantDeclarationImpl(astNode);
	}

	@Override
	public CSharpEnumConstantDeclarationImpl createPsi(@NotNull CSharpVariableDeclStub<CSharpEnumConstantDeclarationImpl> stub)
	{
		return new CSharpEnumConstantDeclarationImpl(stub);
	}

	@Override
	@RequiredReadAction
	public void indexStub(@NotNull CSharpVariableDeclStub<CSharpEnumConstantDeclarationImpl> stub, @NotNull IndexSink indexSink)
	{
		String name = getName(stub);
		if(!StringUtil.isEmpty(name))
		{
			indexSink.occurrence(CSharpIndexKeys.FIELD_INDEX, name);
		}
	}
}
