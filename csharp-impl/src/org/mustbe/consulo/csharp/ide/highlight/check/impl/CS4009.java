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
	public void checkImpl(@NotNull CSharpMethodDeclaration element, @NotNull CompilerCheckResult checkResult)
	{
		PsiElement modifierElement = element.getModifierList().getModifierElement(CSharpModifier.ASYNC);
		assert modifierElement != null;
		checkResult.setTextRange(modifierElement.getTextRange());
		checkResult.setText(CSharpErrorBundle.message(myId, formatElement(element)));
		checkResult.addQuickFix(new RemoveModifierFix(CSharpModifier.ASYNC, element));
	}
}
