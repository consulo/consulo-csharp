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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.ide.codeInsight.actions.RemoveModifierFix;
import consulo.csharp.ide.highlight.CSharpHighlightContext;
import consulo.csharp.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpAccessModifier;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetModifierListOwner;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 10.06.14
 */
public class CS0107 extends CompilerCheck<DotNetModifierListOwner>
{
	@RequiredReadAction
	@Nonnull
	@Override
	public List<CompilerCheckBuilder> check(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull DotNetModifierListOwner element)
	{
		DotNetModifierList modifierList = element.getModifierList();
		if(modifierList == null)
		{
			return Collections.emptyList();
		}

		Map<CSharpAccessModifier, Map<CSharpModifier, PsiElement>> map = new LinkedHashMap<CSharpAccessModifier, Map<CSharpModifier, PsiElement>>(5);
		for(CSharpAccessModifier value : CSharpAccessModifier.VALUES)
		{
			collectModifierElements(value, modifierList, map);
		}

		if(map.size() <= 1)
		{
			return Collections.emptyList();
		}
		List<CompilerCheckBuilder> list = new ArrayList<CompilerCheckBuilder>(map.size());
		for(Map.Entry<CSharpAccessModifier, Map<CSharpModifier, PsiElement>> entry : map.entrySet())
		{
			RemoveModifierFix modifierFix = new RemoveModifierFix(entry.getValue().keySet().toArray(CSharpModifier.EMPTY_ARRAY), element);

			for(Map.Entry<CSharpModifier, PsiElement> psiElement : entry.getValue().entrySet())
			{
				list.add(newBuilder(psiElement.getValue()).withQuickFix(modifierFix));
			}
		}
		return list;
	}

	private static void collectModifierElements(CSharpAccessModifier accessModifier,
			DotNetModifierList modifierList,
			Map<CSharpAccessModifier, Map<CSharpModifier, PsiElement>> result)
	{
		if(accessModifier == CSharpAccessModifier.NONE)
		{
			return;
		}
		Map<CSharpModifier, PsiElement> map = new LinkedHashMap<CSharpModifier, PsiElement>();
		CSharpModifier[] modifiers = accessModifier.getModifiers();
		for(CSharpModifier modifier : modifiers)
		{
			if((modifier == CSharpModifier.INTERNAL || modifier == CSharpModifier.PROTECTED) && result.containsKey(CSharpAccessModifier
					.PROTECTED_INTERNAL))
			{
				continue;
			}
			PsiElement modifierElement = modifierList.getModifierElement(modifier);
			if(modifierElement != null)
			{
				map.put(modifier, modifierElement);
			}
		}
		// dont return array if size of elements if not equal tokens
		if(modifiers.length != map.size())
		{
			return;
		}

		result.put(accessModifier, map);
	}
}
