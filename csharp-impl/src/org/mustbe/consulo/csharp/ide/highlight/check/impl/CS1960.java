package org.mustbe.consulo.csharp.ide.highlight.check.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.codeInsight.actions.RemoveModifierFix;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetModifierList;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * @author VISTALL
 * @since 06.11.14
 */
public class CS1960 extends CompilerCheck<DotNetGenericParameter>
{
	private static final CSharpModifier[] ourModifiers = new CSharpModifier[] {CSharpModifier.OUT, CSharpModifier.IN};

	@Nullable
	@Override
	public CompilerCheckResult checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull DotNetGenericParameter element)
	{
		DotNetModifierList modifierList = element.getModifierList();
		if(modifierList == null)
		{
			return null;
		}

		for(CSharpModifier ourModifier : ourModifiers)
		{
			PsiElement modifierElement = modifierList.getModifierElement(ourModifier);
			if(modifierElement != null)
			{
				DotNetGenericParameterListOwner parameterListOwner = PsiTreeUtil.getParentOfType(element, DotNetGenericParameterListOwner.class);
				assert parameterListOwner != null;

				if(parameterListOwner instanceof CSharpTypeDeclaration && ((CSharpTypeDeclaration) parameterListOwner).isInterface() ||
						parameterListOwner instanceof CSharpMethodDeclaration && ((CSharpMethodDeclaration) parameterListOwner).isDelegate())
				{
					return null;
				}

				return result(modifierElement).addQuickFix(new RemoveModifierFix(ourModifier, element));
			}
		}
		return null;
	}
}
