package org.mustbe.consulo.csharp.lang.psi.impl.resolve;

import gnu.trove.THashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.consulo.lombok.annotations.LazyInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpArrayMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpConversionMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDefStatement;
import org.mustbe.consulo.csharp.lang.psi.CSharpUsingList;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetNamespaceAsElement;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
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
	public CSharpElementGroup<CSharpArrayMethodDeclaration> indexMethodGroup()
	{
		return null;
	}

	@Nullable
	@Override
	public CSharpElementGroup<CSharpConstructorDeclaration> constructorGroup()
	{
		return null;
	}

	@Nullable
	@Override
	public CSharpElementGroup<CSharpConstructorDeclaration> deConstructorGroup()
	{
		return null;
	}

	@Nullable
	@Override
	public CSharpElementGroup<CSharpMethodDeclaration> findOperatorGroupByTokenType(@NotNull IElementType type)
	{
		return null;
	}

	@Nullable
	@Override
	public CSharpElementGroup<CSharpConversionMethodDeclaration> findConversionMethodGroup(@NotNull DotNetTypeRef typeRef)
	{
		return null;
	}

	@Nullable
	@Override
	public CSharpElementGroup<CSharpMethodDeclaration> findExtensionMethodGroupByName(@NotNull String name)
	{
		return getCachedNamespaceContext().findExtensionMethodGroupByName(name);
	}

	@NotNull
	@Override
	public Collection<CSharpElementGroup<CSharpMethodDeclaration>> getExtensionMethodGroups()
	{
		return getCachedNamespaceContext().getExtensionMethodGroups();
	}

	@NotNull
	@Override
	public PsiElement[] findByName(@NotNull String name, @NotNull UserDataHolder holder)
	{
		Map<String, CSharpTypeDefStatement> defStatements = getDefStatements();

		CSharpTypeDefStatement typeDefStatement = defStatements.get(name);
		if(typeDefStatement != null)
		{
			return new PsiElement[] {typeDefStatement};
		}
		CSharpResolveContext cachedNamespaceContext = getCachedNamespaceContext();

		return cachedNamespaceContext.findByName(name, holder);
	}

	@NotNull
	@Override
	@LazyInstance
	public Collection<? extends PsiElement> getElements()
	{
		Map<String, CSharpTypeDefStatement> defStatements = getDefStatements();
		CSharpResolveContext cachedNamespaceContext = getCachedNamespaceContext();
		if(defStatements.isEmpty())
		{
			return cachedNamespaceContext.getElements();
		}
		Collection<? extends PsiElement> elements1 = cachedNamespaceContext.getElements();

		List<PsiElement> merge = new ArrayList<PsiElement>(defStatements.size() + elements1.size());
		merge.addAll(defStatements.values());
		merge.addAll(elements1);
		return merge;
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

	@NotNull
	@LazyInstance
	public Map<String, CSharpTypeDefStatement> getDefStatements()
	{
		CSharpTypeDefStatement[] typeDefs = myUsingList.getTypeDefs();
		if(typeDefs.length == 0)
		{
			return Collections.emptyMap();
		}
		Map<String, CSharpTypeDefStatement> map = new THashMap<String, CSharpTypeDefStatement>(typeDefs.length);
		for(CSharpTypeDefStatement typeDef : typeDefs)
		{
			map.put(typeDef.getName(), typeDef);
		}
		return map;
	}
}
