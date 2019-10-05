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

package consulo.csharp.lang.psi.impl.stub;

import java.util.List;

import javax.annotation.Nonnull;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import consulo.csharp.lang.psi.impl.stub.index.CSharpIndexKeys;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.lang.psi.impl.stub.DotNetNamespaceStubUtil;
import consulo.internal.dotnet.msil.decompiler.util.MsilHelper;
import consulo.msil.lang.psi.impl.elementType.stub.MsilClassEntryStub;
import consulo.msil.lang.psi.impl.elementType.stub.MsilCustomAttributeStub;
import consulo.msil.lang.psi.impl.elementType.stub.MsilMethodEntryStub;
import consulo.msil.lang.psi.impl.elementType.stub.MsilStubIndexer;

/**
 * @author VISTALL
 * @since 22.05.14
 */
public class CSharpMsilStubIndexer extends MsilStubIndexer
{
	@Override
	public void indexClass(@Nonnull MsilClassEntryStub stub, @Nonnull IndexSink indexSink)
	{
		String name = stub.getName();
		if(StringUtil.isEmpty(name))
		{
			return;
		}

		if(stub.isNested())
		{
			return;
		}

		List<StubElement> childrenStubs = stub.getChildrenStubs();
		for(StubElement childrenStub : childrenStubs)
		{
			if(childrenStub instanceof MsilCustomAttributeStub && Comparing.equal(((MsilCustomAttributeStub) childrenStub).getTypeRef(), DotNetTypes.System.Runtime.CompilerServices
					.ExtensionAttribute))
			{
				indexSink.occurrence(CSharpIndexKeys.TYPE_WITH_EXTENSION_METHODS_INDEX, DotNetNamespaceStubUtil.getIndexableNamespace(stub.getNamespace()));
				break;
			}
		}
	}

	@Override
	public void indexMethod(@Nonnull MsilMethodEntryStub stub, @Nonnull IndexSink indexSink)
	{
		List<StubElement> childrenStubs = stub.getChildrenStubs();
		for(StubElement childrenStub : childrenStubs)
		{
			if(childrenStub instanceof MsilCustomAttributeStub && Comparing.equal(((MsilCustomAttributeStub) childrenStub).getTypeRef(), DotNetTypes.System.Runtime.CompilerServices
					.ExtensionAttribute))
			{
				indexSink.occurrence(CSharpIndexKeys.EXTENSION_METHOD_BY_NAME_INDEX, stub.getNameFromBytecode());
				break;
			}
		}
	}

	@Override
	public int getVersion()
	{
		return 4;
	}
}
