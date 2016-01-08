/*
 * Copyright 2013-2016 must-be.org
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

package org.mustbe.consulo.csharp.ide.highlight.check.impl;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.codeInsight.actions.RemoveModifierFix;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.psi.DotNetModifierList;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import com.intellij.psi.PsiElement;
import com.intellij.util.SmartList;

/**
 * @author VISTALL
 * @since 08.01.2016
 */
public class CS0418 extends CompilerCheck<DotNetTypeDeclaration>
{
	private static final CSharpModifier[] ourNotAllowedModifiers = new CSharpModifier[]{
			CSharpModifier.STATIC,
			CSharpModifier.SEALED
	};

	@RequiredReadAction
	@NotNull
	@Override
	public List<? extends HighlightInfoFactory> check(@NotNull CSharpLanguageVersion languageVersion, @NotNull DotNetTypeDeclaration element)
	{
		if(element.hasModifier(CSharpModifier.ABSTRACT))
		{
			DotNetModifierList modifierList = element.getModifierList();
			if(modifierList == null)
			{
				return super.check(languageVersion, element);
			}

			List<HighlightInfoFactory> factories = new SmartList<HighlightInfoFactory>();
			for(CSharpModifier modifier : ourNotAllowedModifiers)
			{
				PsiElement modifierElement = modifierList.getModifierElement(modifier);
				if(modifierElement == null)
				{
					continue;
				}
				factories.add(newBuilder(modifierElement, formatElement(element)).addQuickFix(new RemoveModifierFix(modifier, element)));
			}
			return factories;
		}
		return super.check(languageVersion, element);
	}
}
