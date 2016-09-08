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

package org.mustbe.consulo.csharp.lang.psi.impl.stub.elementTypes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import consulo.annotations.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpStubElements;
import org.mustbe.consulo.csharp.lang.psi.CSharpUserType;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpStubTypeListImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpTypeListStub;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.index.CSharpIndexKeys;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetTypeList;
import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.io.StringRef;

/**
 * @author VISTALL
 * @since 10.01.14
 */
public class CSharpTypeListElementType extends CSharpAbstractStubElementType<CSharpTypeListStub, DotNetTypeList>
{
	public CSharpTypeListElementType(@NotNull @NonNls String debugName)
	{
		super(debugName);
	}

	@NotNull
	@Override
	public DotNetTypeList createElement(@NotNull ASTNode astNode)
	{
		return new CSharpStubTypeListImpl(astNode);
	}

	@Override
	public DotNetTypeList createPsi(@NotNull CSharpTypeListStub cSharpTypeListStub)
	{
		return new CSharpStubTypeListImpl(cSharpTypeListStub, this);
	}

	@RequiredReadAction
	@Override
	public CSharpTypeListStub createStub(@NotNull DotNetTypeList dotNetTypeList, StubElement stubElement)
	{
		DotNetType[] types = dotNetTypeList.getTypes();
		List<StringRef> typeRefs = new ArrayList<StringRef>(types.length);
		for(DotNetType type : types)
		{
			if(type instanceof CSharpUserType)
			{
				ContainerUtil.addIfNotNull(typeRefs, StringRef.fromString(((CSharpUserType) type).getReferenceExpression().getReferenceName()));
			}
		}

		return new CSharpTypeListStub(stubElement, this, ContainerUtil.toArray(typeRefs, StringRef.EMPTY_ARRAY));
	}

	@Override
	public void serialize(@NotNull CSharpTypeListStub cSharpTypeListStub, @NotNull StubOutputStream stubOutputStream) throws IOException
	{
		String[] references = cSharpTypeListStub.geShortReferences();
		stubOutputStream.writeByte(references.length);
		for(String reference : references)
		{
			stubOutputStream.writeName(reference);
		}
	}

	@NotNull
	@Override
	public CSharpTypeListStub deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException
	{
		byte value = stubInputStream.readByte();
		StringRef[] refs = new StringRef[value];
		for(int i = 0; i < value; i++)
		{
			refs[i] = stubInputStream.readName();
		}
		return new CSharpTypeListStub(stubElement, this, refs);
	}

	@Override
	public void indexStub(@NotNull CSharpTypeListStub cSharpTypeListStub, @NotNull IndexSink indexSink)
	{
		if(cSharpTypeListStub.getStubType() == CSharpStubElements.EXTENDS_LIST)
		{
			for(String ref : cSharpTypeListStub.geShortReferences())
			{
				indexSink.occurrence(CSharpIndexKeys.EXTENDS_LIST_INDEX, ref);
			}
		}
	}
}
