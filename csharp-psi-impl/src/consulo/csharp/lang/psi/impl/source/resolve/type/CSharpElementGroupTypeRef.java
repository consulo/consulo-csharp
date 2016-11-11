package consulo.csharp.lang.psi.impl.source.resolve.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.impl.source.CSharpLikeMethodDeclarationImplUtil;
import consulo.csharp.lang.psi.impl.source.resolve.methodResolving.MethodCalcResult;
import consulo.csharp.lang.psi.impl.source.resolve.methodResolving.MethodResolver;
import consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.resolve.DotNetTypeRefWithCachedResult;
import consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 26.10.14
 */
public class CSharpElementGroupTypeRef extends DotNetTypeRefWithCachedResult implements CSharpFastImplicitTypeRef
{
	private final CSharpElementGroup<?> myElementGroup;

	public CSharpElementGroupTypeRef(CSharpElementGroup<?> elementGroup)
	{
		myElementGroup = elementGroup;
	}

	@RequiredReadAction
	@NotNull
	@Override
	protected DotNetTypeResolveResult resolveResult()
	{
		return DotNetTypeResolveResult.EMPTY;
	}

	@RequiredReadAction
	@NotNull
	@Override
	public String toString()
	{
		return myElementGroup.getName();
	}

	@RequiredReadAction
	@Nullable
	@Override
	public DotNetTypeRef doMirror(@NotNull DotNetTypeRef another, PsiElement scope)
	{
		DotNetTypeResolveResult typeResolveResult = another.resolve();
		if(typeResolveResult instanceof CSharpLambdaResolveResult)
		{
			DotNetTypeRef[] parameterTypeRefs = ((CSharpLambdaResolveResult) typeResolveResult).getParameterTypeRefs();

			for(PsiElement psiElement : myElementGroup.getElements())
			{
				if(psiElement instanceof DotNetLikeMethodDeclaration)
				{
					DotNetTypeRef[] methodParameterTypeRef = ((DotNetLikeMethodDeclaration) psiElement).getParameterTypeRefs();

					MethodCalcResult calc = MethodResolver.calc(parameterTypeRefs, methodParameterTypeRef, scope);

					if(calc.isValidResult())
					{
						return new CSharpLambdaTypeRef(scope, null, CSharpLikeMethodDeclarationImplUtil.getParametersInfos((DotNetLikeMethodDeclaration) psiElement),
								((DotNetLikeMethodDeclaration) psiElement).getReturnTypeRef());
					}
				}
			}
		}
		return null;
	}

	@Override
	public boolean isConversion()
	{
		return false;
	}
}