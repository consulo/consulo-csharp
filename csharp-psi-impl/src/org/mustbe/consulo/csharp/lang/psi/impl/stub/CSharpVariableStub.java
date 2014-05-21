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

import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.elementTypes.CSharpAbstractStubElementType;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.typeStub.CSharpStubTypeInfo;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.io.StringRef;

/**
 * @author VISTALL
 * @since 21.12.13.
 */
public class CSharpVariableStub<V extends DotNetVariable> extends MemberStub<V>
{
	private final boolean myConstant;
	private final CSharpStubTypeInfo myTypeInfo;

	public CSharpVariableStub(StubElement parent, CSharpAbstractStubElementType<?, ?> elementType, @Nullable StringRef name,
			@Nullable StringRef namespaceQName, int modifierMask, boolean constant, CSharpStubTypeInfo typeInfo)
	{
		super(parent, elementType, name, namespaceQName, modifierMask, 0);
		myConstant = constant;
		myTypeInfo = typeInfo;
	}

	public boolean isConstant()
	{
		return myConstant;
	}

	public CSharpStubTypeInfo getTypeInfo()
	{
		return myTypeInfo;
	}
}
