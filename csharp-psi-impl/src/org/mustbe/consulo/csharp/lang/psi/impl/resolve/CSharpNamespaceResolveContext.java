package org.mustbe.consulo.csharp.lang.psi.impl.resolve;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.MsilToCSharpUtil;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import org.mustbe.consulo.dotnet.lang.psi.impl.BaseDotNetNamespaceAsElement;
import org.mustbe.consulo.dotnet.resolve.DotNetNamespaceAsElement;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 07.10.14
 */
public class CSharpNamespaceResolveContext implements CSharpResolveContext
{
	private final DotNetNamespaceAsElement myNamespaceAsElement;
	private final GlobalSearchScope myResolveScope;

	public CSharpNamespaceResolveContext(DotNetNamespaceAsElement namespaceAsElement, GlobalSearchScope resolveScope)
	{
		myNamespaceAsElement = namespaceAsElement;
		myResolveScope = resolveScope;
	}

	@Nullable
	@Override
	public CSharpElementGroup indexMethodGroup()
	{
		return null;
	}

	@Nullable
	@Override
	public CSharpElementGroup constructorGroup()
	{
		return null;
	}

	@Nullable
	@Override
	public CSharpElementGroup deConstructorGroup()
	{
		return null;
	}

	@Nullable
	@Override
	public CSharpElementGroup findOperatorGroupByTokenType(@NotNull IElementType type)
	{
		return null;
	}

	@Nullable
	@Override
	public CSharpElementGroup findExtensionMethodByName(@NotNull String name)
	{
		return null;
	}

	@Nullable
	@Override
	public PsiElement findByName(@NotNull String name, @NotNull UserDataHolder holder)
	{
		DotNetNamespaceAsElement.ChildrenFilter filter = holder.getUserData(BaseDotNetNamespaceAsElement.FILTER);
		if(filter == null)
		{
			filter = DotNetNamespaceAsElement.ChildrenFilter.NONE;
		}

		PsiElement[] children = myNamespaceAsElement.findChildren(name, myResolveScope, filter);
		if(children.length > 0)
		{
			PsiElement validWithGeneric = CSharpResolveContextUtil.findValidWithGeneric(holder, children);
			if(validWithGeneric != null)
			{
				return MsilToCSharpUtil.wrap(validWithGeneric);
			}

			return MsilToCSharpUtil.wrap(children[0]);
		}
		return null;
	}
}
