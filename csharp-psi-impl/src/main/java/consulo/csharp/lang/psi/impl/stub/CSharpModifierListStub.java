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

import java.util.List;

import javax.annotation.Nonnull;

import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.BitUtil;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierList;

/**
 * @author VISTALL
 * @since 17.10.14
 */
public class CSharpModifierListStub extends StubBase<DotNetModifierList>
{
	private final int myModifierMask;

	public CSharpModifierListStub(StubElement parent, IStubElementType elementType, int modifierMask)
	{
		super(parent, elementType);
		myModifierMask = modifierMask;
	}

	public int getModifierMask()
	{
		return myModifierMask;
	}

	public static int getModifierMask(@Nonnull DotNetModifierList list)
	{
		int val = 0;
		for(CSharpModifier modifier : CSharpModifier.values())
		{
			if(list.hasModifierInTree(modifier))
			{
				val |= modifier.mask();
			}
		}
		return val;
	}

	@Nonnull
	public CSharpModifier[] getModifiers()
	{
		List<CSharpModifier> list = new SmartList<>();
		for(CSharpModifier modifier : CSharpModifier.values())
		{
			if(hasModifier(modifier))
			{
				list.add(modifier);
			}
		}
		return ContainerUtil.toArray(list, CSharpModifier.ARRAY_FACTORY);
	}

	public boolean hasModifier(DotNetModifier modifier)
	{
		CSharpModifier as = CSharpModifier.as(modifier);
		return BitUtil.isSet(myModifierMask, as.mask());
	}
}
