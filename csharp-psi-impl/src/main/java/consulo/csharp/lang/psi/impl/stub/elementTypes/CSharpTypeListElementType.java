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

package consulo.csharp.lang.psi.impl.stub.elementTypes;

import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.io.StringRef;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpStubElements;
import consulo.csharp.lang.psi.CSharpUserType;
import consulo.csharp.lang.psi.impl.source.CSharpStubTypeListImpl;
import consulo.csharp.lang.psi.impl.stub.CSharpTypeListStub;
import consulo.csharp.lang.psi.impl.stub.index.CSharpIndexKeys;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetTypeList;
import org.jetbrains.annotations.NonNls;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 10.01.14
 */
public class CSharpTypeListElementType extends CSharpAbstractStubElementType<CSharpTypeListStub, DotNetTypeList>
{
	public CSharpTypeListElementType(@Nonnull @NonNls String debugName)
	{
		super(debugName);
	}

	@Nonnull
	@Override
	public DotNetTypeList createElement(@Nonnull ASTNode astNode)
	{
		return new CSharpStubTypeListImpl(astNode);
	}

	@Override
	public DotNetTypeList createPsi(@Nonnull CSharpTypeListStub cSharpTypeListStub)
	{
		return new CSharpStubTypeListImpl(cSharpTypeListStub, this);
	}

	@Nonnull
	@RequiredReadAction
	@Override
	public CSharpTypeListStub createStub(@Nonnull DotNetTypeList dotNetTypeList, StubElement stubElement)
	{
		DotNetType[] types = dotNetTypeList.getTypes();
		List<String> typeRefs = new ArrayList<>(types.length);
		for(DotNetType type : types)
		{
			if(type instanceof CSharpUserType)
			{
				ContainerUtil.addIfNotNull(typeRefs, ((CSharpUserType) type).getReferenceExpression().getReferenceName());
			}
		}

		return new CSharpTypeListStub(stubElement, this, ArrayUtil.toStringArray(typeRefs));
	}

	@Override
	public void serialize(@Nonnull CSharpTypeListStub cSharpTypeListStub, @Nonnull StubOutputStream stubOutputStream) throws IOException
	{
		String[] references = cSharpTypeListStub.geShortReferences();
		stubOutputStream.writeByte(references.length);
		for(String reference : references)
		{
			stubOutputStream.writeName(reference);
		}
	}

	@Nonnull
	@Override
	public CSharpTypeListStub deserialize(@Nonnull StubInputStream stubInputStream, StubElement stubElement) throws IOException
	{
		byte value = stubInputStream.readByte();
		String[] refs = new String[value];
		for(int i = 0; i < value; i++)
		{
			refs[i] = StringRef.toString(stubInputStream.readName());
		}
		return new CSharpTypeListStub(stubElement, this, refs);
	}

	@Override
	public void indexStub(@Nonnull CSharpTypeListStub cSharpTypeListStub, @Nonnull IndexSink indexSink)
	{
		if(cSharpTypeListStub.getStubType() == CSharpStubElements.EXTENDS_LIST)
		{
			for(String ref : cSharpTypeListStub.geShortReferences())
			{
				indexSink.occurrence(CSharpIndexKeys.EXTENDS_LIST_INDEX, ref.hashCode());
			}
		}
	}
}
