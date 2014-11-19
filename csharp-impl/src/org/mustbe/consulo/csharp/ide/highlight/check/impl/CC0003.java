package org.mustbe.consulo.csharp.ide.highlight.check.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentList;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpArrayAccessExpressionImpl;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.util.SmartList;

/**
 * @author VISTALL
 * @since 19.11.14
 */
public class CC0003 extends CompilerCheck<CSharpArrayAccessExpressionImpl>
{
	@NotNull
	@Override
	public List<HighlightInfoFactory> check(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpArrayAccessExpressionImpl expression)
	{
		PsiElement resolve = expression.resolveToCallable();
		if(resolve == null)
		{
			CSharpCallArgumentList parameterList = expression.getParameterList();
			if(parameterList == null)
			{
				return Collections.emptyList();
			}

			List<TextRange> list = new ArrayList<TextRange>();
			PsiElement temp = parameterList.getOpenElement();
			if(temp != null)
			{
				list.add(temp.getTextRange());
			}
			temp = parameterList.getCloseElement();
			if(temp != null)
			{
				list.add(temp.getTextRange());
			}

			List<HighlightInfoFactory> result = new SmartList<HighlightInfoFactory>();
			for(TextRange textRange : list)
			{
				result.add(newBuilder(textRange));
			}
			return result;
		}
		return Collections.emptyList();
	}
}
