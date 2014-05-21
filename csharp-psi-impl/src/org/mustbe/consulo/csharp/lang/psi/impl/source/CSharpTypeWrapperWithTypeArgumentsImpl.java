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
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpGenericWrapperTypeRef;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.psi.DotNetTypeList;
import org.mustbe.consulo.dotnet.psi.DotNetTypeWrapperWithTypeArguments;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;

/**
 * @author VISTALL
 * @since 13.12.13.
 */
public class CSharpTypeWrapperWithTypeArgumentsImpl extends CSharpElementImpl implements DotNetTypeWrapperWithTypeArguments
{
	public CSharpTypeWrapperWithTypeArgumentsImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@NotNull
	@Override
	public DotNetTypeRef toTypeRef()
	{
		DotNetType innerType = getInnerType();
		DotNetType[] arguments = getArguments();
		if(arguments.length == 0)
		{
			return innerType.toTypeRef();
		}

		DotNetTypeRef[] rArguments = new DotNetTypeRef[arguments.length];
		for(int i = 0; i < arguments.length; i++)
		{
			DotNetType argument = arguments[i];
			rArguments[i] = argument.toTypeRef();
		}

		return new CSharpGenericWrapperTypeRef(innerType.toTypeRef(), rArguments);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitTypeWrapperWithTypeArguments(this);
	}

	@NotNull
	@Override
	public DotNetType getInnerType()
	{
		return findNotNullChildByClass(DotNetType.class);
	}

	@Nullable
	@Override
	public DotNetTypeList getArgumentsList()
	{
		return findChildByClass(DotNetTypeList.class);
	}

	@NotNull
	@Override
	public DotNetType[] getArguments()
	{
		DotNetTypeList argumentsList = getArgumentsList();
		if(argumentsList == null)
		{
			return DotNetType.EMPTY_ARRAY;
		}
		return argumentsList.getTypes();
	}
}
