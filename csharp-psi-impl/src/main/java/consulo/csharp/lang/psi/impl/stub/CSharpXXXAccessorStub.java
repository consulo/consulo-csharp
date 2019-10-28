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

import consulo.csharp.lang.psi.CSharpStubElements;
import consulo.dotnet.psi.DotNetXAccessor;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.ArrayUtil;

/**
 * @author VISTALL
 * @since 20.05.14
 */
public class CSharpXXXAccessorStub extends MemberStub<DotNetXAccessor>
{
	public CSharpXXXAccessorStub(StubElement parent, int otherModifiers)
	{
		super(parent, CSharpStubElements.XACCESSOR, null, otherModifiers);
	}

	public static int getOtherModifiers(DotNetXAccessor accessor)
	{
		DotNetXAccessor.Kind accessorKind = accessor.getAccessorKind();
		if(accessorKind == null)
		{
			return -1;
		}
		int i = ArrayUtil.indexOf(DotNetXAccessor.Kind.VALUES, accessor.getAccessorKind());
		assert i != -1;
		return i;
	}

	public DotNetXAccessor.Kind getAccessorType()
	{
		return getOtherModifierMask() == -1 ? null : DotNetXAccessor.Kind.VALUES[getOtherModifierMask()];
	}
}
