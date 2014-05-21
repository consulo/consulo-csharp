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
import org.mustbe.consulo.csharp.lang.psi.CSharpStubElements;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.BitUtil;
import com.intellij.util.io.StringRef;

/**
 * @author VISTALL
 * @since 15.12.13.
 */
public class CSharpTypeStub extends MemberStub<CSharpTypeDeclaration>
{
	public static final int HAS_EXTENSIONS = 1 << 0;
	public static final int INTERFACE = 1 << 1;
	public static final int STRUCT = 1 << 2;
	public static final int ENUM = 1 << 3;

	public CSharpTypeStub(StubElement parent, @Nullable StringRef name, @Nullable StringRef parentQName, int modifierMask, int otherMask)
	{
		super(parent, CSharpStubElements.TYPE_DECLARATION, name, parentQName, modifierMask, otherMask);
	}

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
		else if(typeDeclaration.hasExtensions())
		{
			mask |= HAS_EXTENSIONS;
		}
		return mask;
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

	public boolean hasExtensions()
	{
		return BitUtil.isSet(getOtherModifierMask(), HAS_EXTENSIONS);
	}
}
