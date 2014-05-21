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
import org.mustbe.consulo.csharp.lang.psi.impl.stub.typeStub.CSharpStubTypeInfo;
import org.mustbe.consulo.dotnet.psi.DotNetTypeList;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.io.StringRef;

/**
 * @author VISTALL
 * @since 10.01.14
 */
public class CSharpTypeListStub extends StubBase<DotNetTypeList>
{
	private final StringRef[] myReferences;
	private final CSharpStubTypeInfo[] myTypeRefs;

	public CSharpTypeListStub(StubElement parent, IStubElementType elementType, StringRef[] references, CSharpStubTypeInfo[] typeRefs)
	{
		super(parent, elementType);
		myReferences = references;
		myTypeRefs = typeRefs;
	}

	@NotNull
	public String[] getReferences()
	{
		String[] ar = new String[myReferences.length];
		for(int i = 0; i < myReferences.length; i++)
		{
			StringRef reference = myReferences[i];
			ar[i] = StringRef.toString(reference);
		}
		return ar;
	}

	public CSharpStubTypeInfo[] getTypeRefs()
	{
		return myTypeRefs;
	}
}
