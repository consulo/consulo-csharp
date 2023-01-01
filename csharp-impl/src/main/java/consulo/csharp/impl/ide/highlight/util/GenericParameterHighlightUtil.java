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

package consulo.csharp.impl.ide.highlight.util;

import consulo.language.editor.rawHighlight.HighlightInfo;
import consulo.language.editor.rawHighlight.HighlightInfoHolder;
import consulo.language.editor.intention.QuickFixAction;
import consulo.csharp.impl.ide.codeInsight.actions.RemoveModifierFix;
import consulo.csharp.impl.localize.CSharpErrorLocalize;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.language.editor.rawHighlight.HighlightInfoType;
import consulo.language.psi.PsiElement;

/**
 * @author VISTALL
 * @since 06.11.14
 */
public class GenericParameterHighlightUtil
{
	public static void checkInAndOutModifiers(DotNetGenericParameter parameter, HighlightInfoHolder highlightInfoHolder)
	{
		DotNetModifierList modifierList = parameter.getModifierList();
		if(modifierList == null)
		{
			return;
		}

		PsiElement outModifierElement = modifierList.getModifierElement(CSharpModifier.OUT);
		PsiElement inModifierElement = modifierList.getModifierElement(CSharpModifier.IN);
		if(outModifierElement != null && inModifierElement != null)
		{
			registerInOutProblem(parameter, highlightInfoHolder, outModifierElement, CSharpModifier.OUT);
			registerInOutProblem(parameter, highlightInfoHolder, inModifierElement, CSharpModifier.IN);
		}
	}

	private static void registerInOutProblem(DotNetGenericParameter parameter,
			HighlightInfoHolder highlightInfoHolder,
			PsiElement modifierElement,
			CSharpModifier modifier)
	{
		CSharpModifier revertModifier = modifier == CSharpModifier.IN ? CSharpModifier.OUT : CSharpModifier.IN;
		HighlightInfo.Builder builder = HighlightInfo.newHighlightInfo(HighlightInfoType.ERROR);
		builder.descriptionAndTooltip(CSharpErrorLocalize.conflicting0ModifierWith1Modifier(modifier.getPresentableText(), revertModifier.getPresentableText()));
		builder.range(modifierElement);

		HighlightInfo info = builder.create();
		if(info != null)
		{
			QuickFixAction.registerQuickFixAction(info, modifierElement.getTextRange(), new RemoveModifierFix(modifier, parameter));
			highlightInfoHolder.add(info);
		}
	}
}
