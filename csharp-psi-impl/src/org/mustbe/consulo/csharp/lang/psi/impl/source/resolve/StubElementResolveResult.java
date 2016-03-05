package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 22.10.14
 */
public class StubElementResolveResult extends CSharpResolveResult
{
	private final DotNetTypeRef myTypeRef;

	public StubElementResolveResult(@NotNull PsiElement element, boolean validResult, @NotNull DotNetTypeRef typeRef)
	{
		super(element, validResult);
		myTypeRef = typeRef;
	}

	@NotNull
	public DotNetTypeRef getTypeRef()
	{
		return myTypeRef;
	}
}
