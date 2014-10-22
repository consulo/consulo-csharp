package org.mustbe.consulo.csharp.lang.psi.impl.resolve;

import org.consulo.lombok.annotations.LazyInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpUsingList;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetNamespaceAsElement;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 20.10.14
 */
public class CSharpUsingListResolveContext implements CSharpResolveContext
{
	private final CSharpUsingList myUsingList;

	public CSharpUsingListResolveContext(CSharpUsingList usingList)
	{
		myUsingList = usingList;
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
		CSharpResolveContext cachedNamespaceContext = getCachedNamespaceContext();

		PsiElement byName = cachedNamespaceContext.findByName(name, holder);
		if(byName != null)
		{
			return byName;
		}
		return null;
	}

	@NotNull
	@LazyInstance
	public CSharpResolveContext getCachedNamespaceContext()
	{
		DotNetNamespaceAsElement[] usingNamespaces = myUsingList.getUsingNamespaces();
		if(usingNamespaces.length == 0)
		{
			return EMPTY;
		}
		return CSharpResolveContextUtil.createContext(DotNetGenericExtractor.EMPTY, myUsingList.getResolveScope(), usingNamespaces);
	}
}
