package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;

/**
 * @author VISTALL
 * @since 28.10.14
 */
public class CSharpResolveResultWithExtractor extends CSharpResolveResult
{
	@NotNull
	public static CSharpResolveResultWithExtractor withExtractor(@NotNull ResolveResult resolveResult, @NotNull DotNetGenericExtractor extractor)
	{
		PsiElement providerElement = null;
		if(resolveResult instanceof CSharpResolveResult)
		{
			providerElement = ((CSharpResolveResult) resolveResult).getProviderElement();
		}
		CSharpResolveResultWithExtractor withExtractor = new CSharpResolveResultWithExtractor(resolveResult.getElement(), extractor);
		withExtractor.setProvider(providerElement);
		return withExtractor;
	}

	@NotNull
	private final DotNetGenericExtractor myExtractor;

	public CSharpResolveResultWithExtractor(@NotNull PsiElement element, @NotNull DotNetGenericExtractor extractor)
	{
		super(element);
		myExtractor = extractor;
	}

	@NotNull
	public DotNetGenericExtractor getExtractor()
	{
		return myExtractor;
	}
}
