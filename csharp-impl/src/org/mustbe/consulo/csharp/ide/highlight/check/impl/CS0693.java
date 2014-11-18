package org.mustbe.consulo.csharp.ide.highlight.check.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterListOwner;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * @author VISTALL
 * @since 18.11.14
 */
public class CS0693 extends CompilerCheck<DotNetGenericParameter>
{
	@Nullable
	@Override
	public CompilerCheckResult checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull DotNetGenericParameter element)
	{
		PsiElement nameIdentifier = element.getNameIdentifier();
		if(nameIdentifier == null)
		{
			return null;
		}

		DotNetGenericParameterListOwner firstParent = PsiTreeUtil.getParentOfType(element, DotNetGenericParameterListOwner.class);

		DotNetGenericParameterListOwner secondParent = PsiTreeUtil.getParentOfType(firstParent, DotNetGenericParameterListOwner.class);

		if(secondParent != null)
		{
			for(DotNetGenericParameter genericParameter : secondParent.getGenericParameters())
			{
				if(Comparing.equal(genericParameter.getName(), nameIdentifier.getText()))
				{
					return result(nameIdentifier, formatElement(element), formatElement(secondParent));
				}
			}
		}
		return null;
	}
}
