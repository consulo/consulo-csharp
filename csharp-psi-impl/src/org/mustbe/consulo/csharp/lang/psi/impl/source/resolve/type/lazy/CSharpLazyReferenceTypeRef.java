package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.lazy;

import org.consulo.lombok.annotations.LazyInstance;
import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpReferenceTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 13.11.14
 */
public class CSharpLazyReferenceTypeRef extends CSharpReferenceTypeRef
{
	public CSharpLazyReferenceTypeRef(CSharpReferenceExpression referenceExpression)
	{
		super(referenceExpression);
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
		return CSharpLazyReferenceTypeRef.super.resolve(myReferenceExpression);
	}
}
