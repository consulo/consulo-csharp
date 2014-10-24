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
import org.mustbe.consulo.csharp.lang.psi.CSharpAttribute;
import org.mustbe.consulo.csharp.lang.psi.CSharpAttributeList;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpStubElements;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpAttributeListStub;
import org.mustbe.consulo.dotnet.psi.DotNetAttributeTargetType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.IStubElementType;

/**
 * @author VISTALL
 * @since 19.12.13.
 */
public class CSharpAttributeListImpl extends CSharpStubElementImpl<CSharpAttributeListStub> implements CSharpAttributeList
{
	public CSharpAttributeListImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	public CSharpAttributeListImpl(@NotNull CSharpAttributeListStub stub, @NotNull IStubElementType<? extends CSharpAttributeListStub, ?> nodeType)
	{
		super(stub, nodeType);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitAttributeList(this);
	}

	@Nullable
	@Override
	public DotNetAttributeTargetType getTargetType()
	{
		return null;
	}

	@NotNull
	@Override
	public CSharpAttribute[] getAttributes()
	{
		return getStubOrPsiChildren(CSharpStubElements.ATTRIBUTE, CSharpAttribute.ARRAY_FACTORY);
	}
}
