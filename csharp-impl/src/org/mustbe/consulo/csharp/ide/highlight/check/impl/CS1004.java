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

package org.mustbe.consulo.csharp.ide.highlight.check.impl;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import consulo.annotations.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.codeInsight.actions.RemoveModifierFix;
import org.mustbe.consulo.csharp.ide.highlight.CSharpHighlightContext;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetModifierListOwner;
import com.intellij.psi.PsiElement;
import com.intellij.util.SmartList;

/**
 * @author VISTALL
 * @since 10.06.14
 */
public class CS1004 extends CompilerCheck<DotNetModifierListOwner>
{
	@RequiredReadAction
	@NotNull
	@Override
	public List<CompilerCheckBuilder> check(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpHighlightContext highlightContext, @NotNull DotNetModifierListOwner element)
	{
		DotNetModifierList modifierList = element.getModifierList();
		if(modifierList == null)
		{
			return Collections.emptyList();
		}

		List<CompilerCheckBuilder> results = new SmartList<CompilerCheckBuilder>();
		for(CSharpModifier modifier : CSharpModifier.values())
		{
			List<PsiElement> modifierElements = modifierList.getModifierElements(modifier);
			if(modifierElements.size() <= 1)
			{
				continue;
			}

			for(PsiElement modifierElement : modifierElements)
			{
				results.add(newBuilder(modifierElement, modifier.getPresentableText()).addQuickFix(new RemoveModifierFix(modifier, element)));
			}
		}
		return results;
	}
}
