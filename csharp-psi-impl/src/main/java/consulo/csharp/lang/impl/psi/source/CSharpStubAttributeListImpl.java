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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import consulo.csharp.lang.psi.CSharpAttribute;
import consulo.csharp.lang.psi.CSharpAttributeList;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.impl.psi.CSharpStubElements;
import consulo.csharp.lang.impl.psi.CSharpTokenSets;
import consulo.csharp.lang.impl.psi.stub.CSharpAttributeListStub;
import consulo.dotnet.psi.DotNetAttributeTargetType;
import consulo.language.ast.ASTNode;
import consulo.language.psi.stub.IStubElementType;

/**
 * @author VISTALL
 * @since 19.12.13.
 */
public class CSharpStubAttributeListImpl extends CSharpStubElementImpl<CSharpAttributeListStub> implements CSharpAttributeList
{
	public CSharpStubAttributeListImpl(@Nonnull ASTNode node)
	{
		super(node);
	}

	public CSharpStubAttributeListImpl(@Nonnull CSharpAttributeListStub stub, @Nonnull IStubElementType<? extends CSharpAttributeListStub, ?> nodeType)
	{
		super(stub, nodeType);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitAttributeList(this);
	}

	@Nullable
	@Override
	public DotNetAttributeTargetType getTargetType()
	{
		CSharpAttributeListStub stub = getGreenStub();
		if(stub != null)
		{
			return stub.getTargetType();
		}
		return CSharpAttributeListImpl.getAttributeType(findChildByType(CSharpTokenSets.ATTRIBUTE_TARGETS));
	}

	@Nonnull
	@Override
	public CSharpAttribute[] getAttributes()
	{
		return getStubOrPsiChildren(CSharpStubElements.ATTRIBUTE, CSharpAttribute.ARRAY_FACTORY);
	}
}
