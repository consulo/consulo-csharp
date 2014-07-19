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

import org.mustbe.consulo.csharp.lang.psi.CSharpStubElements;
import org.mustbe.consulo.dotnet.psi.DotNetXXXAccessor;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.ArrayUtil;
import com.intellij.util.io.StringRef;

/**
 * @author VISTALL
 * @since 20.05.14
 */
public class CSharpXXXAccessorStub extends MemberStub<DotNetXXXAccessor>
{
	public CSharpXXXAccessorStub(StubElement parent, StringRef name, int modifierMask, int otherModifiers)
	{
		super(parent, CSharpStubElements.XXX_ACCESSOR, name, null, modifierMask, otherModifiers);
	}

	public CSharpXXXAccessorStub(StubElement parent, String name, int modifierMask, int otherModifiers)
	{
		super(parent, CSharpStubElements.XXX_ACCESSOR, name, null, modifierMask, otherModifiers);
	}

	public static int getOtherModifiers(DotNetXXXAccessor accessor)
	{
		DotNetXXXAccessor.Kind accessorKind = accessor.getAccessorKind();
		if(accessorKind == null)
		{
			return -1;
		}
		int i = ArrayUtil.indexOf(DotNetXXXAccessor.Kind.VALUES, accessor.getAccessorKind());
		assert i != -1;
		return i;
	}

	public DotNetXXXAccessor.Kind getAccessorType()
	{
		return getOtherModifierMask() == -1 ? null : DotNetXXXAccessor.Kind.VALUES[getOtherModifierMask()];
	}
}
