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

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.codeInsight.actions.RemoveModifierFix;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierList;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 08.01.2016
 */
public class CS0441 extends CompilerCheck<DotNetTypeDeclaration>
{
	@RequiredReadAction
	@NotNull
	@Override
	public List<? extends HighlightInfoFactory> check(@NotNull CSharpLanguageVersion languageVersion, @NotNull DotNetTypeDeclaration element)
	{
		DotNetModifierList modifierList = element.getModifierList();
		if(modifierList == null)
		{
			return super.check(languageVersion, element);
		}
		PsiElement sealedModifierElement = modifierList.getModifierElement(CSharpModifier.SEALED);
		PsiElement staticModifierElement = modifierList.getModifierElement(CSharpModifier.STATIC);
		if(sealedModifierElement != null && staticModifierElement != null)
		{
			List<HighlightInfoFactory> list = new ArrayList<HighlightInfoFactory>(2);
			String name = formatElement(element);
			list.add(newBuilder(sealedModifierElement, name).addQuickFix(new RemoveModifierFix(DotNetModifier.SEALED, element)));
			list.add(newBuilder(staticModifierElement, name).addQuickFix(new RemoveModifierFix(DotNetModifier.STATIC, element)));
			return list;
		}
		return super.check(languageVersion, element);
	}
}
