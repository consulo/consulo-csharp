package org.mustbe.consulo.csharp.lang.psi.impl.source;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.ExecuteTarget;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.ExecuteTargetUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.MemberResolveScopeProcessor;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;

/**
 * @author VISTALL
 * @since 09.10.14
 */
public class CSharpLikeMethodDeclarationImplUtil
{
	public static boolean processDeclarations(@NotNull DotNetLikeMethodDeclaration methodDeclaration,
			@NotNull PsiScopeProcessor processor,
			@NotNull ResolveState state,
			PsiElement lastParent,
			@NotNull PsiElement place)
	{
		if(processor instanceof MemberResolveScopeProcessor)
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
