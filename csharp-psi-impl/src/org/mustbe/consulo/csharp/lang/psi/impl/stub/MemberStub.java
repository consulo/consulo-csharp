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
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierList;
import org.mustbe.consulo.dotnet.psi.DotNetModifierListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.NamedStubBase;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.BitUtil;
import com.intellij.util.io.StringRef;

/**
 * @author VISTALL
 * @since 19.12.13.
 */
public class MemberStub<T extends DotNetNamedElement> extends NamedStubBase<T>
{
	private final StringRef myParentQName;
	private final int myModifierMask;
	private final int myOtherModifierMask;

	public MemberStub(StubElement parent, IStubElementType elementType, @Nullable StringRef name, @Nullable StringRef namespaceQName, int modifierMask, int otherModifierMask)
	{
		super(parent, elementType, name);
		myParentQName = namespaceQName;
		myModifierMask = modifierMask;
		myOtherModifierMask = otherModifierMask;
	}

	public MemberStub(StubElement parent, IStubElementType elementType, @Nullable String name, @Nullable StringRef namespaceQName, int modifierMask,
			int otherModifierMask)
	{
		super(parent, elementType, name);
		myParentQName = namespaceQName;
		myModifierMask = modifierMask;
		myOtherModifierMask = otherModifierMask;
	}

	@Nullable
	public String getParentQName()
	{
		return StringRef.toString(myParentQName);
	}

	public boolean hasModifier(DotNetModifier modifier)
	{
		CSharpModifier as = CSharpModifier.as(modifier);
		return BitUtil.isSet(myModifierMask, as.mask());
	}

	public int getModifierMask()
	{
		return myModifierMask;
	}

	public int getOtherModifierMask()
	{
		return myOtherModifierMask;
	}

	public static int getModifierMask(@NotNull DotNetModifierListOwner list)
	{
		DotNetModifierList modifierList = list.getModifierList();
		if(modifierList == null)
		{
			return 0;
		}
		int val = 0;
		DotNetModifier[] modifierElementTypes = modifierList.getModifiers();
		for(DotNetModifier netModifier : modifierElementTypes)
		{
			CSharpModifier modifier = CSharpModifier.as(netModifier);
			val |= modifier.mask();
		}
		return val;
	}
}
