package org.mustbe.consulo.csharp.lang.psi.impl.source;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementCompareUtil;
import org.mustbe.consulo.csharp.lang.psi.CSharpSimpleParameterInfo;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.ExecuteTarget;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.ExecuteTargetUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.wrapper.GenericUnwrapTool;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.psi.DotNetVirtualImplementOwner;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
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

	@NotNull
	public static Pair<ResolveVirtualImplementResult, PsiElement> resolveVirtualImplementation(@NotNull DotNetVirtualImplementOwner owner,
			@NotNull PsiElement scope)
	{
		DotNetType typeForImplement = owner.getTypeForImplement();
		if(typeForImplement == null)
		{
			return Pair.create(ResolveVirtualImplementResult.CANT_HAVE, null);
		}

		DotNetTypeRef typeRefForImplement = typeForImplement.toTypeRef();

		DotNetTypeResolveResult typeResolveResult = typeRefForImplement.resolve(owner);

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
	public static CSharpSimpleParameterInfo[] getParametersInfos(@NotNull DotNetLikeMethodDeclaration methodDeclaration)
	{
		DotNetParameter[] parameters = methodDeclaration.getParameters();

		CSharpSimpleParameterInfo[] parameterInfos = new CSharpSimpleParameterInfo[parameters.length];
		for(int i = 0; i < parameters.length; i++)
		{
			DotNetParameter parameter = parameters[i];
			parameterInfos[i] = new CSharpSimpleParameterInfo(i, parameter.getName(), parameter, parameter.toTypeRef(true));
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
