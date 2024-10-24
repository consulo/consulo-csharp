package consulo.csharp.impl.ide.highlight.check.impl;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.impl.ide.highlight.CSharpHighlightContext;
import consulo.csharp.impl.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpReferenceWithValidation;
import consulo.csharp.lang.impl.psi.source.CSharpConstantExpressionImpl;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.document.util.TextRange;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.ReferenceRange;
import consulo.localize.LocalizeValue;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.List;

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
				LocalizeValue errorMessage = ((CSharpReferenceWithValidation) reference).getErrorMessage(element);
				List<TextRange> ranges = ReferenceRange.getAbsoluteRanges(reference);
				return newBuilder(ranges.get(0)).withText(errorMessage);
			}
		}
		return null;
	}
}
