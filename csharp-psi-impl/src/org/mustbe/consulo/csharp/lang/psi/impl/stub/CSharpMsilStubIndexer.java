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

package org.mustbe.consulo.csharp.lang.psi.impl.stub;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpNamespaceHelper;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.index.CSharpIndexKeys;
import org.mustbe.consulo.msil.MsilHelper;
import org.mustbe.consulo.msil.lang.psi.impl.elementType.stub.MsilClassEntryStub;
import org.mustbe.consulo.msil.lang.psi.impl.elementType.stub.MsilStubIndexer;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.stubs.IndexSink;

/**
 * @author VISTALL
 * @since 22.05.14
 */
public class CSharpMsilStubIndexer extends MsilStubIndexer
{
	@Override
	public void indexClass(@NotNull MsilClassEntryStub stub, @NotNull IndexSink indexSink)
	{
		String namespaceForIndexing = CSharpNamespaceHelper.getNamespaceForIndexing(stub.getNamespace());

		String name = stub.getName();
		if(StringUtil.isEmpty(name))
		{
			return;
		}

		indexSink.occurrence(CSharpIndexKeys.TYPE_INDEX, name);

		indexSink.occurrence(CSharpIndexKeys.MEMBER_BY_NAMESPACE_QNAME_INDEX, namespaceForIndexing);

		indexSink.occurrence(CSharpIndexKeys.NAMESPACE_BY_QNAME_INDEX, namespaceForIndexing);

		indexSink.occurrence(CSharpIndexKeys.TYPE_BY_QNAME_INDEX, MsilHelper.appendNoGeneric(stub.getNamespace(), stub.getName()));
	}
}
