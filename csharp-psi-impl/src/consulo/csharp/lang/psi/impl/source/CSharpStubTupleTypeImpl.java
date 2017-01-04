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

package consulo.csharp.lang.psi.impl.source;

import org.jetbrains.annotations.NotNull;
import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.EmptyStub;
import com.intellij.psi.stubs.IStubElementType;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpStubElements;
import consulo.csharp.lang.psi.CSharpTupleType;
import consulo.csharp.lang.psi.CSharpTupleVariable;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTupleTypeRef;
import consulo.dotnet.resolve.DotNetTypeRef;

/**
 * @author VISTALL
 * @since 26-Nov-16.
 */
public class CSharpStubTupleTypeImpl extends CSharpStubTypeElementImpl<EmptyStub<CSharpTupleType>> implements CSharpTupleType
{
	public CSharpStubTupleTypeImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	public CSharpStubTupleTypeImpl(@NotNull EmptyStub<CSharpTupleType> stub,
			@NotNull IStubElementType<? extends EmptyStub<CSharpTupleType>, ?> nodeType)
	{
		super(stub, nodeType);
	}

	@RequiredReadAction
	@NotNull
	@Override
	protected DotNetTypeRef toTypeRefImpl()
	{
		CSharpTupleVariable[] variables = getVariables();
		DotNetTypeRef[] typeRefs = new DotNetTypeRef[variables.length];
		for(int i = 0; i < variables.length; i++)
		{
			CSharpTupleVariable variable = variables[i];
			typeRefs[i] = variable.toTypeRef(true);
		}
		return new CSharpTupleTypeRef(this, typeRefs, variables);
	}

	@NotNull
	@Override
	public CSharpTupleVariable[] getVariables()
	{
		return getStubOrPsiChildren(CSharpStubElements.TUPLE_VARIABLE, CSharpTupleVariable.ARRAY_FACTORY);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitTupleType(this);
	}
}
