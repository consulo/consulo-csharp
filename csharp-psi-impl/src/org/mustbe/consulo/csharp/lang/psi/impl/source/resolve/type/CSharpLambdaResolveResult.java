package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpSimpleLikeMethod;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.resolve.DotNetTypeResolveResult;

/**
 * @author VISTALL
 * @since 24.10.14
 */
public interface CSharpLambdaResolveResult extends DotNetTypeResolveResult, CSharpSimpleLikeMethod
{
	@RequiredReadAction
	boolean isInheritParameters();

	@NotNull
	@RequiredReadAction
	DotNetTypeRef[] getParameterTypeRefs();

	@Nullable
	CSharpMethodDeclaration getTarget();
}
