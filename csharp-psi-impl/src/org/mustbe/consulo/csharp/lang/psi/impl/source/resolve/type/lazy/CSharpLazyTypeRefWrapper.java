package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.lazy;

import org.consulo.lombok.annotations.LazyInstance;
import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 09.11.14
 */
public class CSharpLazyTypeRefWrapper extends DotNetTypeRef.Delegate
{
	@NotNull
	private final PsiElement myScope;

	public CSharpLazyTypeRefWrapper(@NotNull PsiElement scope, @NotNull DotNetTypeRef delegate)
	{
		super(delegate);
		myScope = scope;
	}

	@NotNull
	@Override
	public DotNetTypeResolveResult resolve(@NotNull PsiElement scope)
	{
		return resolveImpl();
	}

	@NotNull
	@LazyInstance
	private DotNetTypeResolveResult resolveImpl()
	{
		// need write CSharpLazyTypeRefWrapper due bad wrap via LazyInstance
		return CSharpLazyTypeRefWrapper.super.resolve(myScope);
	}
}
