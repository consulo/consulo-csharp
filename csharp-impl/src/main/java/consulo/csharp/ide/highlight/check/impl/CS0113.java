/*
 * Copyright 2013-2018 consulo.io
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

package consulo.csharp.ide.highlight.check.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.ide.codeInsight.actions.RemoveModifierFix;
import consulo.csharp.ide.highlight.CSharpHighlightContext;
import consulo.csharp.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.psi.DotNetVirtualImplementOwner;
import consulo.language.psi.PsiElement;

/**
 * @author VISTALL
 * @since 2018-03-17
 */
public class CS0113 extends CompilerCheck<DotNetVirtualImplementOwner>
{
	private static final CSharpModifier[] ourIllegalModifiers = {
			CSharpModifier.NEW,
			CSharpModifier.VIRTUAL
	};

	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull DotNetVirtualImplementOwner element)
	{
		if(!(element instanceof DotNetModifierListOwner))
		{
			return null;
		}

		DotNetModifierListOwner owner = (DotNetModifierListOwner) element;

		if(owner.hasModifier(CSharpModifier.OVERRIDE))
		{
			DotNetModifierList modifierList = owner.getModifierList();
			if(modifierList == null)
			{
				return null;
			}

			for(CSharpModifier modifier : ourIllegalModifiers)
			{
				PsiElement modifierElement = modifierList.getModifierElement(modifier);
				if(modifierElement != null)
				{
					return newBuilder(modifierElement, formatElement(element)).withQuickFix(new RemoveModifierFix(modifier, owner));
				}
			}
		}
		return null;
	}
}
