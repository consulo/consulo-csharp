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

import consulo.language.ast.IElementType;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpArrayType;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.impl.psi.CSharpTokenSets;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpArrayTypeRef;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.resolve.DotNetTypeRef;


/**
 * @author VISTALL
 * @since 05.12.14
 */
public class CSharpArrayTypeImpl extends CSharpTypeElementImpl implements CSharpArrayType
{
	public CSharpArrayTypeImpl(IElementType elementType)
	{
		super(elementType);
	}

	@RequiredReadAction
	@Override
	public DotNetTypeRef toTypeRefImpl()
	{
		DotNetType innerType = getInnerType();

		return new CSharpArrayTypeRef(getProject(), getResolveScope(), innerType.toTypeRef(), getDimensions());
	}

	@RequiredReadAction
	@Override
	public int getDimensions()
	{
		return findChildrenByType(CSharpTokenSets.COMMA).size();
	}

	@Override
	public void accept(CSharpElementVisitor visitor)
	{
		visitor.visitArrayType(this);
	}

	@RequiredReadAction
	@Override
	public DotNetType getInnerType()
	{
		return findNotNullChildByClass(DotNetType.class);
	}
}
