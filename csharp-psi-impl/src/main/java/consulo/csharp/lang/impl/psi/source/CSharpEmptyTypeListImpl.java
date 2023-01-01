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
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetTypeList;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.IElementType;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 05.02.15
 */
public class CSharpEmptyTypeListImpl extends CSharpElementImpl implements DotNetTypeList
{
	public CSharpEmptyTypeListImpl(@Nonnull IElementType elementType)
	{
		super(elementType);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitTypeList(this);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetType[] getTypes()
	{
		return DotNetType.EMPTY_ARRAY;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetTypeRef[] getTypeRefs()
	{
		return DotNetTypeRef.EMPTY_ARRAY;
	}

	@RequiredReadAction
	@Override
	public int getTypesCount()
	{
		return findChildrenByType(CSharpTokens.COMMA).size() + 1;
	}
}
