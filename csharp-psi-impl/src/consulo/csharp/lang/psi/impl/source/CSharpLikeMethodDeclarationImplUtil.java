package consulo.csharp.lang.psi.impl.source;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpElementCompareUtil;
import consulo.csharp.lang.psi.CSharpSimpleParameterInfo;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.psi.impl.source.resolve.ExecuteTarget;
import consulo.csharp.lang.psi.impl.source.resolve.ExecuteTargetUtil;
import consulo.csharp.lang.psi.impl.source.resolve.type.wrapper.GenericUnwrapTool;
import consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetParameterListOwner;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetVirtualImplementOwner;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;

/**
 * @author VISTALL
 * @since 09.10.14
 */
public class CSharpLikeMethodDeclarationImplUtil
{
	public static enum ResolveVirtualImplementResult
	{
		CANT_HAVE,
		FOUND,
		NOT_FOUND
	}

	public static boolean isEquivalentTo(@NotNull PsiElement o1, @Nullable PsiElement o2)
	{
		if(o2 == null)
		{
			return false;
		}
		PsiElement originalElement1 = o1.getOriginalElement();
		PsiElement originalElement2 = o2.getOriginalElement();

		if(o1.getUserData(CSharpResolveUtil.EXTENSION_METHOD_WRAPPER) == originalElement2)
		{
			return true;
		}
		return originalElement1 == originalElement2;
	}

	@NotNull
	@RequiredReadAction
	public static Pair<ResolveVirtualImplementResult, PsiElement> resolveVirtualImplementation(@NotNull DotNetVirtualImplementOwner owner, @NotNull PsiElement scope)
	{
		DotNetType typeForImplement = owner.getTypeForImplement();
		if(typeForImplement == null)
		{
			return Pair.create(ResolveVirtualImplementResult.CANT_HAVE, null);
		}

		DotNetTypeRef typeRefForImplement = typeForImplement.toTypeRef();

		DotNetTypeResolveResult typeResolveResult = typeRefForImplement.resolve();

		PsiElement resolvedElement = typeResolveResult.getElement();
		DotNetGenericExtractor genericExtractor = typeResolveResult.getGenericExtractor();
		if(!(resolvedElement instanceof CSharpTypeDeclaration))
		{
			return Pair.create(ResolveVirtualImplementResult.CANT_HAVE, null);
		}

		for(DotNetNamedElement namedElement : ((CSharpTypeDeclaration) resolvedElement).getMembers())
		{
			namedElement = GenericUnwrapTool.extract(namedElement, genericExtractor);

			if(CSharpElementCompareUtil.isEqual(namedElement, owner, scope))
			{
				return Pair.<ResolveVirtualImplementResult, PsiElement>create(ResolveVirtualImplementResult.FOUND, namedElement);
			}
		}
		return Pair.<ResolveVirtualImplementResult, PsiElement>create(ResolveVirtualImplementResult.NOT_FOUND, null);
	}

	@NotNull
	@RequiredReadAction
	public static CSharpSimpleParameterInfo[] getParametersInfos(@NotNull DotNetParameterListOwner parameterListOwner)
	{
		DotNetParameter[] parameters = parameterListOwner.getParameters();

		CSharpSimpleParameterInfo[] parameterInfos = new CSharpSimpleParameterInfo[parameters.length];
		for(int i = 0; i < parameters.length; i++)
		{
			DotNetParameter parameter = parameters[i];
			parameterInfos[i] = new CSharpSimpleParameterInfo(i, parameter, parameter.toTypeRef(true));
		}
		return parameterInfos;
	}

	public static boolean processDeclarations(@NotNull DotNetLikeMethodDeclaration methodDeclaration,
			@NotNull PsiScopeProcessor processor,
			@NotNull ResolveState state,
			PsiElement lastParent,
			@NotNull PsiElement place)
	{
		if(ExecuteTargetUtil.canProcess(processor, ExecuteTarget.GENERIC_PARAMETER))
		{
			for(DotNetGenericParameter dotNetGenericParameter : methodDeclaration.getGenericParameters())
			{
				if(!processor.execute(dotNetGenericParameter, state))
				{
					return false;
				}
			}
		}

		if(ExecuteTargetUtil.canProcess(processor, ExecuteTarget.LOCAL_VARIABLE_OR_PARAMETER))
		{
			for(DotNetParameter parameter : methodDeclaration.getParameters())
			{
				if(!processor.execute(parameter, state))
				{
					return false;
				}
			}
		}

		return true;
	}
}