package org.mustbe.consulo.csharp.lang;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpConstantExpressionImpl;
import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 12.03.2015
 */
public class CSharpCfsMultiHostInjector implements MultiHostInjector
{
	@Override
	public void injectLanguages(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement context)
	{
		CSharpConstantExpressionImpl expression = (CSharpConstantExpressionImpl) context;

		IElementType literalType = expression.getLiteralType();
		if(literalType == CSharpTokens.INTERPOLATION_STRING_LITERAL)
		{
			TextRange range = new TextRange(2, expression.getTextLength() - 1);

			registrar.startInjecting(CSharpCfsLanguageVersion.getInstance()).addPlace(null, null, (PsiLanguageInjectionHost) context,
					range).doneInjecting();
		}
	}
}
