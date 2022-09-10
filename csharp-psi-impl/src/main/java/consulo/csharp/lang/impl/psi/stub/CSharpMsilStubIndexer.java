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

package consulo.csharp.lang.impl.psi.stub;

import consulo.csharp.lang.impl.psi.stub.index.CSharpIndexKeys;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.impl.stub.DotNetNamespaceStubUtil;
import consulo.internal.dotnet.msil.decompiler.util.MsilHelper;
import consulo.language.psi.stub.IndexSink;
import consulo.language.psi.stub.StubElement;
import consulo.msil.impl.lang.psi.impl.elementType.stub.MsilClassEntryStub;
import consulo.msil.impl.lang.psi.impl.elementType.stub.MsilCustomAttributeStub;
import consulo.msil.impl.lang.psi.impl.elementType.stub.MsilMethodEntryStub;
import consulo.msil.impl.lang.psi.impl.elementType.stub.MsilStubIndexer;
import consulo.util.lang.Comparing;
import consulo.util.lang.StringUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @author VISTALL
 * @since 22.05.14
 */
public class CSharpMsilStubIndexer extends MsilStubIndexer
{
	public static int makeExtensionMethodIndexKey(@Nullable String namespace, @Nonnull String name)
	{
		return (DotNetNamespaceStubUtil.getIndexableNamespace(StringUtil.notNullize(namespace)) + "|" + name).hashCode();
	}

	@Override
	public void indexMethod(@Nonnull MsilMethodEntryStub stub, @Nonnull IndexSink indexSink)
	{
		boolean isExtension = false;

		List<StubElement> childrenStubs = stub.getChildrenStubs();
		for(StubElement childrenStub : childrenStubs)
		{
			if(childrenStub instanceof MsilCustomAttributeStub && Comparing.equal(((MsilCustomAttributeStub) childrenStub).getTypeRef(), DotNetTypes.System.Runtime.CompilerServices
					.ExtensionAttribute))
			{
				isExtension = true;
				break;
			}
		}

		if(!isExtension)
		{
			return;
		}

		indexSink.occurrence(CSharpIndexKeys.EXTENSION_METHOD_BY_NAME_INDEX, stub.getNameFromBytecode().hashCode());

		StubElement parentStub = stub.getParentStub();
		if(!(parentStub instanceof MsilClassEntryStub))
		{
			return;
		}

		if(((MsilClassEntryStub) parentStub).isNested())
		{
			return;
		}

		String namespace = ((MsilClassEntryStub) parentStub).getNamespace();

		indexSink.occurrence(CSharpIndexKeys.EXTENSION_METHOD_BY_NAMESPACE, DotNetNamespaceStubUtil.getIndexableNamespace(StringUtil.notNullize(namespace)).hashCode());

		int key = makeExtensionMethodIndexKey(namespace, MsilHelper.cutGenericMarker(stub.getNameFromBytecode()));
		indexSink.occurrence(CSharpIndexKeys.EXTENSION_METHOD_BY_NAMESPACE_AND_NAME_INDEX, key);
	}

	@Override
	public int getVersion()
	{
		return 6;
	}
}
