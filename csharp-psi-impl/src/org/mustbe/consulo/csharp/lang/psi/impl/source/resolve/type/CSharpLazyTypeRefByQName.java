package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type;

import org.consulo.lombok.annotations.LazyInstance;
import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.CSharpTransform;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetPsiSearcher;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import org.mustbe.consulo.dotnet.resolve.SimpleTypeResolveResult;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;

/**
 * @author VISTALL
 * @since 28.10.14
 */
public class CSharpLazyTypeRefByQName extends DotNetTypeRef.Adapter
{
	@NotNull
	private final Project myProject;
	@NotNull
	private final GlobalSearchScope myScope;
	@NotNull
	private final String myQualifiedName;
	private final Boolean myNullable;

	public CSharpLazyTypeRefByQName(@NotNull Project project, @NotNull GlobalSearchScope scope, @NotNull String qualifiedName)
	{
		myProject = project;
		myScope = scope;
		myQualifiedName = qualifiedName;
		myNullable = null;
	}

	public CSharpLazyTypeRefByQName(@NotNull Project project, @NotNull GlobalSearchScope scope, @NotNull String qualifiedName, boolean nullable)
	{
		myProject = project;
		myScope = scope;
		myQualifiedName = qualifiedName;
		myNullable = nullable;
	}

	@NotNull
	@Override
	public String getQualifiedText()
	{
		return myQualifiedName;
	}

	@NotNull
	@Override
	public String getPresentableText()
	{
		return StringUtil.getShortName(myQualifiedName);
	}

	@NotNull
	@Override
	public DotNetTypeResolveResult resolve(@NotNull PsiElement scope)
	{
		return resolveImpl();
	}

	@LazyInstance
	private DotNetTypeResolveResult resolveImpl()
	{
		DotNetTypeDeclaration type = DotNetPsiSearcher.getInstance(myProject).findType(myQualifiedName, myScope,
				DotNetPsiSearcher.TypeResoleKind.UNKNOWN, CSharpTransform.INSTANCE);

		if(type == null)
		{
			return DotNetTypeResolveResult.EMPTY;
		}

		return new SimpleTypeResolveResult(type, myNullable == Boolean.TRUE || CSharpTypeUtil.isElementIsNullable(type));
	}
}
