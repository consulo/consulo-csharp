package consulo.csharp.ide.highlight.check.impl;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ReferenceRange;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.ide.highlight.CSharpHighlightContext;
import consulo.csharp.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpReferenceWithValidation;
import consulo.csharp.lang.psi.impl.source.CSharpConstantExpressionImpl;
import consulo.csharp.module.extension.CSharpLanguageVersion;

/**
 * @author VISTALL
 * @since 07-Jan-17
 */
public class CC0006 extends CompilerCheck<CSharpConstantExpressionImpl>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull CSharpConstantExpressionImpl element)
	{
		PsiReference[] references = element.getReferences();
		for(PsiReference reference : references)
		{
			PsiElement resolved = reference.resolve();
			if(resolved != null)
			{
				continue;
			}

			if(reference instanceof CSharpReferenceWithValidation)
			{
				String errorMessage = ((CSharpReferenceWithValidation) reference).getErrorMessage(element);
				List<TextRange> ranges = ReferenceRange.getAbsoluteRanges(reference);
				return newBuilder(ranges.get(0)).setText(errorMessage);
			}
		}
		return null;
	}
}
