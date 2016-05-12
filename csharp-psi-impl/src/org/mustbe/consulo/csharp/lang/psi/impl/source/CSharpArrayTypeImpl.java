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

package org.mustbe.consulo.csharp.lang.psi.impl.source;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpArrayType;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokenSets;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpArrayTypeRef;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;

/**
 * @author VISTALL
 * @since 05.12.14
 */
public class CSharpArrayTypeImpl extends CSharpTypeElementImpl implements CSharpArrayType
{
	public CSharpArrayTypeImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@RequiredReadAction
	@NotNull
	@Override
	public DotNetTypeRef toTypeRefImpl()
	{
		DotNetType innerType = getInnerType();

		return new CSharpArrayTypeRef(this, innerType.toTypeRef(), getDimensions());
	}

	@RequiredReadAction
	@Override
	public int getDimensions()
	{
		return findChildrenByType(CSharpTokenSets.COMMA).size();
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitArrayType(this);
	}

	@RequiredReadAction
	@NotNull
	@Override
	public DotNetType getInnerType()
	{
		return findNotNullChildByClass(DotNetType.class);
	}
}
