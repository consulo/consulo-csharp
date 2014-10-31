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

package org.mustbe.consulo.csharp.ide.codeInsight.actions;

import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpStubElements;
import org.mustbe.consulo.dotnet.psi.DotNetMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetModifierList;
import org.mustbe.consulo.dotnet.psi.DotNetModifierListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import com.intellij.openapi.util.Comparing;

/**
 * @author VISTALL
 * @since 31.08.14
 */
public class AddAccessModifierFix extends AddXModifierFix
{
	public AddAccessModifierFix(CSharpModifier... modifiers)
	{
		super(modifiers);
	}

	@Override
	public boolean isAllow(DotNetModifierListOwner owner, CSharpModifier[] modifiers)
	{
		if(owner instanceof DotNetTypeDeclaration || owner instanceof DotNetMethodDeclaration && !(owner.getParent() instanceof
				DotNetTypeDeclaration))
		{
			return equal(modifiers, CSharpModifier.PROTECTED, CSharpModifier.INTERNAL) ||
					equal(modifiers, CSharpModifier.INTERNAL) ||
					equal(modifiers, CSharpModifier.PUBLIC);
		}

		return CSharpStubElements.QUALIFIED_MEMBERS.contains(owner.getNode().getElementType());
	}

	private static boolean equal(CSharpModifier[] modifiers, CSharpModifier... required)
	{
		return Comparing.equal(modifiers, required);
	}

	@Override
	protected void beforeAdd(DotNetModifierList modifierList)
	{
		modifierList.removeModifier(CSharpModifier.INTERNAL);
		modifierList.removeModifier(CSharpModifier.PUBLIC);
		modifierList.removeModifier(CSharpModifier.PROTECTED);
		modifierList.removeModifier(CSharpModifier.PRIVATE);
	}
}
