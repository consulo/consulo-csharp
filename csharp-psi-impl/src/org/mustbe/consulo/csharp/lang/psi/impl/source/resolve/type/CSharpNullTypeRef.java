package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.CSharpTransform;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetPsiSearcher;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRefWithCachedResult;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import org.mustbe.consulo.dotnet.resolve.SimpleTypeResolveResult;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;

/**
 * @author VISTALL
 * @since 23.10.14
 */
public class CSharpNullTypeRef extends DotNetTypeRefWithCachedResult
{
	private Project myProject;
	private GlobalSearchScope myScope;

	@RequiredReadAction
	public CSharpNullTypeRef(@NotNull PsiElement element)
	{
		this(element.getProject(), element.getResolveScope());
	}

	@RequiredReadAction
	public CSharpNullTypeRef(@NotNull Project project, @NotNull GlobalSearchScope scope)
	{
		myProject = project;
		myScope = scope;
	}

	@RequiredReadAction
	@NotNull
	@Override
	protected DotNetTypeResolveResult resolveResult()
	{
		DotNetTypeDeclaration type = DotNetPsiSearcher.getInstance(myProject).findType(DotNetTypes.System.Object, myScope, CSharpTransform.INSTANCE);
		if(type == null)
		{
			return DotNetTypeResolveResult.EMPTY;
		}
		return new SimpleTypeResolveResult(type, DotNetGenericExtractor.EMPTY);
	}

	@RequiredReadAction
	@NotNull
	@Override
	public String toString()
	{
		return "null";
	}
}
