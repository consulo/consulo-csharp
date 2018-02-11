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

import javax.annotation.Nonnull;

import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpStubElements;
import consulo.dotnet.lang.psi.impl.DotNetPsiCountUtil;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetParameterList;
import consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.EmptyStub;

/**
 * @author VISTALL
 * @since 28.11.13.
 */
public class CSharpStubParameterListImpl extends CSharpStubElementImpl<EmptyStub<DotNetParameterList>> implements DotNetParameterList
{
	public CSharpStubParameterListImpl(@Nonnull ASTNode node)
	{
		super(node);
	}

	public CSharpStubParameterListImpl(@Nonnull EmptyStub<DotNetParameterList> stub)
	{
		super(stub, CSharpStubElements.PARAMETER_LIST);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitParameterList(this);
	}

	@Override
	public int getParametersCount()
	{
		return DotNetPsiCountUtil.countChildrenOfType(this, CSharpStubElements.PARAMETER);
	}

	@Nonnull
	@Override
	public DotNetParameter[] getParameters()
	{
		return getStubOrPsiChildren(CSharpStubElements.PARAMETER, DotNetParameter.ARRAY_FACTORY);
	}

	@Nonnull
	@Override
	public DotNetTypeRef[] getParameterTypeRefs()
	{
		DotNetParameter[] parameters = getParameters();
		if(parameters.length == 0)
		{
			return DotNetTypeRef.EMPTY_ARRAY;
		}
		DotNetTypeRef[] dotNetTypeRefs = new DotNetTypeRef[parameters.length];
		for(int i = 0; i < dotNetTypeRefs.length; i++)
		{
			dotNetTypeRefs[i] = parameters[i].toTypeRef(true);
		}
		return dotNetTypeRefs;
	}
}
