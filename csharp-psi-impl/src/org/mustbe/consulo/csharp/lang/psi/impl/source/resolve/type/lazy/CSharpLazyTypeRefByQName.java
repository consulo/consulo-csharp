package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.lazy;

import org.consulo.lombok.annotations.LazyInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 28.10.14
 */
public class CSharpLazyTypeRefByQName extends CSharpTypeRefByQName
{
	private PsiElement myScope;

	public CSharpLazyTypeRefByQName(PsiElement scope, @NotNull String qualifiedName)
	{
		super(qualifiedName);
		myScope = scope;
	}

	public CSharpLazyTypeRefByQName(PsiElement scope, @NotNull String qualifiedName, @Nullable Boolean nullable)
	{
		super(qualifiedName, nullable);
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
	public DotNetTypeResolveResult resolveImpl()
	{
		return CSharpLazyTypeRefByQName.super.resolve(myScope);
	}
}
