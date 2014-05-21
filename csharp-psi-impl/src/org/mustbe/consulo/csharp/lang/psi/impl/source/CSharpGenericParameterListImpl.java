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
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpStubElements;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpGenericParameterListStub;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterList;
import com.intellij.lang.ASTNode;

/**
 * @author VISTALL
 * @since 30.11.13.
 */
public class CSharpGenericParameterListImpl extends CSharpStubElementImpl<CSharpGenericParameterListStub> implements DotNetGenericParameterList
{
	public CSharpGenericParameterListImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	public CSharpGenericParameterListImpl(@NotNull CSharpGenericParameterListStub stub)
	{
		super(stub, CSharpStubElements.GENERIC_PARAMETER_LIST);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitGenericParameterList(this);
	}

	@NotNull
	@Override
	public DotNetGenericParameter[] getParameters()
	{
		return getStubOrPsiChildren(CSharpStubElements.GENERIC_PARAMETER, DotNetGenericParameter.ARRAY_FACTORY);
	}

	@Override
	public int getGenericParametersCount()
	{
		return getParameters().length;
	}
}
