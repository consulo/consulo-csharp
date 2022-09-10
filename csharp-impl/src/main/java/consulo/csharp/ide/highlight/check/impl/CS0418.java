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

package consulo.csharp.ide.highlight.check.impl;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.ide.codeInsight.actions.RemoveModifierFix;
import consulo.csharp.ide.highlight.CSharpHighlightContext;
import consulo.csharp.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.language.psi.PsiElement;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

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
	@Nonnull
	@Override
	public List<? extends HighlightInfoFactory> check(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull DotNetTypeDeclaration element)
	{
		if(element.hasModifier(CSharpModifier.ABSTRACT))
		{
			DotNetModifierList modifierList = element.getModifierList();
			if(modifierList == null)
			{
				return super.check(languageVersion, highlightContext, element);
			}

			List<HighlightInfoFactory> factories = new ArrayList<>();
			for(CSharpModifier modifier : ourNotAllowedModifiers)
			{
				PsiElement modifierElement = modifierList.getModifierElement(modifier);
				if(modifierElement == null)
				{
					continue;
				}
				factories.add(newBuilder(modifierElement, formatElement(element)).withQuickFix(new RemoveModifierFix(modifier, element)));
			}
			return factories;
		}
		return super.check(languageVersion, highlightContext, element);
	}
}
