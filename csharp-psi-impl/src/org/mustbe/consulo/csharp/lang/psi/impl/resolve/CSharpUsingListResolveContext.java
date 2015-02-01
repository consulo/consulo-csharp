package org.mustbe.consulo.csharp.lang.psi.impl.resolve;

import gnu.trove.THashMap;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.mustbe.consulo.csharp.lang.psi.CSharpUsingListChild;
import org.mustbe.consulo.csharp.lang.psi.CSharpUsingNamespaceStatement;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetNamespaceAsElement;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;

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
	public CSharpElementGroup<CSharpArrayMethodDeclaration> indexMethodGroup(boolean deep)
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
	public CSharpElementGroup<CSharpMethodDeclaration> findOperatorGroupByTokenType(@NotNull IElementType type, boolean deep)
	{
		return null;
	}

	@Nullable
	@Override
	public CSharpElementGroup<CSharpConversionMethodDeclaration> findConversionMethodGroup(@NotNull DotNetTypeRef typeRef, boolean deep)
	{
		return null;
	}

	@Nullable
	@Override
	public CSharpElementGroup<CSharpMethodDeclaration> findExtensionMethodGroupByName(@NotNull String name)
	{
		CSharpElementGroup<CSharpMethodDeclaration> groupByName1 = getCachedNamespaceContext().findExtensionMethodGroupByName(name);
		CSharpElementGroup<CSharpMethodDeclaration> groupByName2 = getCachedTypeContext().findExtensionMethodGroupByName(name);
		if(groupByName1 == null && groupByName2 == null)
		{
			return null;
		}
		if(groupByName1 == null)
		{
			return groupByName2;
		}
		if(groupByName2 == null)
		{
			return groupByName1;
		}
		return new CSharpCompositeElementGroupImpl<CSharpMethodDeclaration>(myUsingList.getProject(), Arrays.asList(groupByName1, groupByName2));
	}

	@Override
	public boolean processExtensionMethodGroups(@NotNull Processor<CSharpElementGroup<CSharpMethodDeclaration>> processor)
	{
		CSharpUsingListChild[] statements = myUsingList.getStatements();
		for(CSharpUsingListChild statement : statements)
		{
			if(statement instanceof CSharpUsingNamespaceStatement)
			{
				if(!CSharpNamespaceResolveContext.processExtensionMethodGroups(((CSharpUsingNamespaceStatement) statement).getReferenceText(),
						myUsingList.getProject(), myUsingList.getResolveScope(), processor))
				{
					return false;
				}
			}
		}

		return getCachedTypeContext().processExtensionMethodGroups(processor);
	}

	@NotNull
	@Override
	public PsiElement[] findByName(@NotNull String name, boolean deep, @NotNull UserDataHolder holder)
	{
		Map<String, CSharpTypeDefStatement> defStatements = getDefStatements();

		CSharpTypeDefStatement typeDefStatement = defStatements.get(name);
		if(typeDefStatement != null)
		{
			return new PsiElement[] {typeDefStatement};
		}

		CSharpResolveContext cachedTypeContext = getCachedTypeContext();

		PsiElement[] selectedMembers = cachedTypeContext.findByName(name, deep, holder);
		if(selectedMembers.length > 0)
		{
			return selectedMembers;
		}

		CSharpResolveContext cachedNamespaceContext = getCachedNamespaceContext();

		return cachedNamespaceContext.findByName(name, deep, holder);
	}

	@Override
	public boolean processElements(@NotNull Processor<PsiElement> processor, boolean deep)
	{
		if(!ContainerUtil.process(myUsingList.getTypeDefs(), processor))
		{
			return false;
		}

		CSharpResolveContext cachedTypeContext = getCachedTypeContext();
		if(!cachedTypeContext.processElements(processor, deep))
		{
			return false;
		}
		return getCachedNamespaceContext().processElements(processor, deep);
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
	public CSharpResolveContext getCachedTypeContext()
	{
		DotNetTypeRef[] usingTypeRefs = myUsingList.getUsingTypeRefs();
		if(usingTypeRefs.length == 0)
		{
			return EMPTY;
		}

		List<CSharpResolveContext> contexts = new ArrayList<CSharpResolveContext>(usingTypeRefs.length);
		for(DotNetTypeRef usingTypeRef : usingTypeRefs)
		{
			DotNetTypeResolveResult typeResolveResult = usingTypeRef.resolve(myUsingList);

			PsiElement typeResolveResultElement = typeResolveResult.getElement();
			if(typeResolveResultElement == null)
			{
				continue;
			}
			contexts.add(CSharpResolveContextUtil.createContext(typeResolveResult.getGenericExtractor(), myUsingList.getResolveScope(),
					typeResolveResultElement));
		}
		return new CSharpCompositeResolveContext(myUsingList.getProject(), ContainerUtil.toArray(contexts, ARRAY_FACTORY));
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
