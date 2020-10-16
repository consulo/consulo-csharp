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

import com.intellij.psi.tree.IElementType;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.dotnet.lang.psi.impl.DotNetPsiCountUtil;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetTypeList;
import consulo.dotnet.resolve.DotNetTypeRef;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 04.12.13.
 */
public class CSharpTypeListImpl extends CSharpElementImpl implements DotNetTypeList
{
	public CSharpTypeListImpl(@Nonnull IElementType elementType)
	{
		super(elementType);
	}

	@RequiredReadAction
	@Override
	public int getTypesCount()
	{
		return DotNetPsiCountUtil.countChildrenOfType(getNode(), DotNetType.class);
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

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetType[] getTypes()
	{
		return findChildrenByClass(DotNetType.class);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitTypeList(this);
	}
}
