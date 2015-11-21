package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.lazy;

import org.consulo.lombok.annotations.LazyInstance;
import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
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

	@RequiredReadAction
	@NotNull
	@Override
	public DotNetTypeResolveResult resolve(@NotNull PsiElement scope)
	{
		return resolveImpl();
	}

	@NotNull
	@LazyInstance
	@RequiredReadAction
	public DotNetTypeResolveResult resolveImpl()
	{
		return CSharpLazyTypeRefByQName.super.resolve(myScope);
	}
}
