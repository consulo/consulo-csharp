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

package org.mustbe.consulo.csharp.ide.highlight.util;

import org.mustbe.consulo.csharp.ide.CSharpErrorBundle;
import org.mustbe.consulo.csharp.ide.codeInsight.actions.RemoveModifierFix;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetModifierList;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder;
import com.intellij.codeInsight.daemon.impl.quickfix.QuickFixAction;
import com.intellij.psi.PsiElement;

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
		builder.descriptionAndTooltip(CSharpErrorBundle.message("conflicting.0.modifier.with.1.modifier", modifier.getPresentableText(),
				revertModifier.getPresentableText()));
		builder.range(modifierElement);

		HighlightInfo info = builder.create();
		if(info != null)
		{
			QuickFixAction.registerQuickFixAction(info, modifierElement.getTextRange(), new RemoveModifierFix(modifier, parameter));
			highlightInfoHolder.add(info);
		}
	}
}
