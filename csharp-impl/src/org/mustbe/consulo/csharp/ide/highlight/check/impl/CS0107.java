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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpAccessModifier;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.psi.DotNetModifierList;
import org.mustbe.consulo.dotnet.psi.DotNetModifierListOwner;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 10.06.14
 */
public class CS0107 extends CompilerCheck<DotNetModifierListOwner>
{
	@NotNull
	@Override
	public List<CompilerCheckResult> check(@NotNull CSharpLanguageVersion languageVersion, @NotNull DotNetModifierListOwner element)
	{
		DotNetModifierList modifierList = element.getModifierList();
		if(modifierList == null)
		{
			return Collections.emptyList();
		}

		Map<CSharpAccessModifier, PsiElement> map = new HashMap<CSharpAccessModifier, PsiElement>(4);
		for(CSharpAccessModifier value : CSharpAccessModifier.VALUES)
		{
			PsiElement modifierElement = modifierList.getModifierElement(value.toModifier());
			if(modifierElement == null)
			{
				continue;
			}
			map.put(value, modifierElement);
		}

		if(map.size() <= 1)
		{
			return Collections.emptyList();
		}
		List<CompilerCheckResult> list = new ArrayList<CompilerCheckResult>(map.size());
		for(PsiElement psiElement : map.values())
		{
			list.add(result(psiElement));
		}
		return list;
	}
}
