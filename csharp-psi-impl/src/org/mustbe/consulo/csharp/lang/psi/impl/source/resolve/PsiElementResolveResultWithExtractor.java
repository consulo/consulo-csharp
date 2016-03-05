package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 28.10.14
 */
public class PsiElementResolveResultWithExtractor extends CSharpResolveResult
{
	@NotNull
	private final DotNetGenericExtractor myExtractor;

	public PsiElementResolveResultWithExtractor(@NotNull PsiElement element, @NotNull DotNetGenericExtractor extractor)
	{
		super(element);
		myExtractor = extractor;
	}

	public PsiElementResolveResultWithExtractor(@NotNull PsiElement element, @NotNull DotNetGenericExtractor extractor, boolean validResult)
	{
		super(element, validResult);
		myExtractor = extractor;
	}

	@NotNull
	public DotNetGenericExtractor getExtractor()
	{
		return myExtractor;
	}
}
