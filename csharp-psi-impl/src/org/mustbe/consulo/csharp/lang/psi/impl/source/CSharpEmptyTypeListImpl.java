/*
 * Copyright 2013-2015 must-be.org
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
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.psi.DotNetTypeList;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import com.intellij.util.ArrayUtil;

/**
 * @author VISTALL
 * @since 05.02.15
 */
public class CSharpEmptyTypeListImpl extends CSharpElementImpl implements DotNetTypeList
{
	public CSharpEmptyTypeListImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitTypeList(this);
	}

	@NotNull
	@Override
	public DotNetType[] getTypes()
	{
		return DotNetType.EMPTY_ARRAY;
	}

	@NotNull
	@Override
	public DotNetTypeRef[] getTypeRefs()
	{
		return DotNetTypeRef.EMPTY_ARRAY;
	}

	@NotNull
	@Override
	public String[] getTypeTexts()
	{
		return ArrayUtil.EMPTY_STRING_ARRAY;
	}

	@Override
	public int getTypesCount()
	{
		int size = findChildrenByType(CSharpTokens.COMMA).size();
		return size == 0 ? 0 : size + 1;
	}
}
