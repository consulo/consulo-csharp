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

package consulo.csharp.lang.impl.psi.source;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.impl.psi.CSharpStubElementSets;
import consulo.csharp.lang.impl.psi.stub.CSharpTypeListStub;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetTypeList;
import consulo.dotnet.psi.impl.DotNetPsiCountUtil;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.ASTNode;
import consulo.language.psi.stub.IStubElementType;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 10.01.14
 */
public class CSharpStubTypeListImpl extends CSharpStubElementImpl<CSharpTypeListStub> implements DotNetTypeList
{
	public CSharpStubTypeListImpl(@Nonnull ASTNode node)
	{
		super(node);
	}

	public CSharpStubTypeListImpl(@Nonnull CSharpTypeListStub stub, @Nonnull IStubElementType<? extends CSharpTypeListStub, ?> nodeType)
	{
		super(stub, nodeType);
	}

	@RequiredReadAction
	@Override
	public int getTypesCount()
	{
		return DotNetPsiCountUtil.countChildrenOfType(this, CSharpStubElementSets.TYPE_SET);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetType[] getTypes()
	{
		return getStubOrPsiChildren(CSharpStubElementSets.TYPE_SET, DotNetType.ARRAY_FACTORY);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetTypeRef[] getTypeRefs()
	{
		DotNetType[] types = getTypes();
		if(types.length == 0)
		{
			return DotNetTypeRef.EMPTY_ARRAY;
		}
		DotNetTypeRef[] array = new DotNetTypeRef[types.length];
		for(int i = 0; i < types.length; i++)
		{
			DotNetType type = types[i];
			array[i] = type.toTypeRef();
		}
		return array;
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitTypeList(this);
	}
}
