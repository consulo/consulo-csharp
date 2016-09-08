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

package consulo.csharp.ide.codeInsight.actions;

import org.jetbrains.annotations.NotNull;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetModifierListOwner;

/**
 * @author VISTALL
 * @since 10.06.14
 */
public class AddModifierFix extends BaseModifierFix
{
	public AddModifierFix(DotNetModifier[] modifiers, DotNetModifierListOwner parent)
	{
		super(modifiers, parent);
	}

	public AddModifierFix(DotNetModifier modifier, DotNetModifierListOwner parent)
	{
		super(modifier, parent);
	}

	@Override
	public boolean isValidCondition(@NotNull DotNetModifierList modifierList, @NotNull DotNetModifier modifier)
	{
		return !modifierList.hasModifier(modifier);
	}

	@NotNull
	@Override
	public String getActionName()
	{
		return "Add";
	}

	@Override
	public void doAction(@NotNull DotNetModifierList modifierList, @NotNull DotNetModifier modifier)
	{
		modifierList.addModifier(modifier);
	}
}