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

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.ide.CSharpErrorBundle;
import org.mustbe.consulo.csharp.ide.codeInsight.actions.RemoveModifierFix;
import org.mustbe.consulo.csharp.ide.highlight.check.AbstractCompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.dotnet.DotNetRunUtil;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 09.09.14
 */
public class CS4009 extends AbstractCompilerCheck<CSharpMethodDeclaration>
{
	@Override
	public boolean accept(@NotNull CSharpMethodDeclaration element)
	{
		return element.hasModifier(CSharpModifier.ASYNC) && DotNetRunUtil.isEntryPoint(element);
	}

	@Override
	public void checkImpl(@NotNull CSharpMethodDeclaration element, @NotNull CompilerCheckBuilder checkResult)
	{
		PsiElement modifierElement = element.getModifierList().getModifierElement(CSharpModifier.ASYNC);
		assert modifierElement != null;
		checkResult.setTextRange(modifierElement.getTextRange());
		checkResult.setText(CSharpErrorBundle.message(myId, formatElement(element)));
		checkResult.addQuickFix(new RemoveModifierFix(CSharpModifier.ASYNC, element));
	}
}
