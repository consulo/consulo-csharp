package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 24.10.14
 */
public interface CSharpLambdaResolveResult extends DotNetTypeResolveResult
{
	@NotNull
	DotNetTypeRef getReturnTypeRef();

	@NotNull
	DotNetTypeRef[] getParameterTypeRefs();

	@Nullable
	PsiElement getTarget();
}
