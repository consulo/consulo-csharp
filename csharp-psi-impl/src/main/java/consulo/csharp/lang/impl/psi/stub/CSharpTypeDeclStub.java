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

import consulo.language.psi.stub.StubElement;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.CSharpStubElements;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.util.lang.BitUtil;

import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 15.12.13.
 */
public class CSharpTypeDeclStub extends MemberStub<CSharpTypeDeclaration>
{
	public static final int INTERFACE = 1 << 0;
	public static final int STRUCT = 1 << 1;
	public static final int ENUM = 1 << 2;

	@RequiredReadAction
	public static int getOtherModifiers(CSharpTypeDeclaration typeDeclaration)
	{
		int mask = 0;
		if(typeDeclaration.isInterface())
		{
			mask |= INTERFACE;
		}
		else if(typeDeclaration.isEnum())
		{
			mask |= ENUM;
		}
		else if(typeDeclaration.isStruct())
		{
			mask |= STRUCT;
		}
		return mask;
	}

	private final String myVmQName;

	public CSharpTypeDeclStub(StubElement parent, @Nullable String parentQName, String vmQName, int otherMask)
	{
		super(parent, CSharpStubElements.TYPE_DECLARATION, parentQName, otherMask);
		myVmQName = vmQName;
	}

	public String getVmQName()
	{
		return myVmQName;
	}

	public boolean isInterface()
	{
		return BitUtil.isSet(getOtherModifierMask(), INTERFACE);
	}

	public boolean isEnum()
	{
		return BitUtil.isSet(getOtherModifierMask(), ENUM);
	}

	public boolean isStruct()
	{
		return BitUtil.isSet(getOtherModifierMask(), STRUCT);
	}

	public boolean isNested()
	{
		return getParentStub() instanceof CSharpTypeDeclStub;
	}
}
