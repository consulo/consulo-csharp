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

import org.jetbrains.annotations.Nullable;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import consulo.dotnet.psi.DotNetNamedElement;

/**
 * @author VISTALL
 * @since 19.12.13.
 */
public class MemberStub<T extends DotNetNamedElement> extends StubBase<T>
{
	private final String myParentQName;
	private final int myOtherModifierMask;

	public MemberStub(StubElement parent, IStubElementType elementType, @Nullable String namespaceQName, int otherModifierMask)
	{
		super(parent, elementType);
		myParentQName = namespaceQName;
		myOtherModifierMask = otherModifierMask;
	}

	@Nullable
	public String getParentQName()
	{
		return myParentQName;
	}

	public int getOtherModifierMask()
	{
		return myOtherModifierMask;
	}
}
