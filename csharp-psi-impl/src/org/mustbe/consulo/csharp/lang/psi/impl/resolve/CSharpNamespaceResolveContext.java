package org.mustbe.consulo.csharp.lang.psi.impl.resolve;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.consulo.lombok.annotations.LazyInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpArrayMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpConversionMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.CSharpTransformer;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.MsilToCSharpUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.index.TypeWithExtensionMethodsIndex;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import org.mustbe.consulo.dotnet.lang.psi.impl.BaseDotNetNamespaceAsElement;
import org.mustbe.consulo.dotnet.lang.psi.impl.stub.DotNetNamespaceStubUtil;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetNamespaceAsElement;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.SmartList;

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
		String presentableName = DotNetNamespaceStubUtil.getIndexableNamespace(myNamespaceAsElement.getPresentableQName());

		Collection<DotNetTypeDeclaration> decls = TypeWithExtensionMethodsIndex.getInstance().get(presentableName,
				myNamespaceAsElement.getProject(), myResolveScope);

		if(decls.isEmpty())
		{
			return null;
		}
		List<CSharpElementGroup<CSharpMethodDeclaration>> list = new SmartList<CSharpElementGroup<CSharpMethodDeclaration>>();
		for(DotNetTypeDeclaration decl : decls)
		{
			PsiElement wrappedDeclaration = MsilToCSharpUtil.wrap(decl);

			CSharpResolveContext context = CSharpResolveContextUtil.createContext(DotNetGenericExtractor.EMPTY, myResolveScope, wrappedDeclaration);

			CSharpElementGroup<CSharpMethodDeclaration> extensionMethodByName = context.findExtensionMethodGroupByName(name);
			if(extensionMethodByName != null)
			{
				list.add(extensionMethodByName);
			}
		}
		return new CSharpCompositeElementGroupImpl<CSharpMethodDeclaration>(myNamespaceAsElement.getProject(), list);
	}

	@NotNull
	@Override
	public Collection<CSharpElementGroup<CSharpMethodDeclaration>> getExtensionMethodGroups()
	{
		String presentableName = DotNetNamespaceStubUtil.getIndexableNamespace(myNamespaceAsElement.getPresentableQName());

		Collection<DotNetTypeDeclaration> decls = TypeWithExtensionMethodsIndex.getInstance().get(presentableName,
				myNamespaceAsElement.getProject(), myResolveScope);

		if(decls.isEmpty())
		{
			return Collections.emptyList();
		}
		List<CSharpElementGroup<CSharpMethodDeclaration>> list = new SmartList<CSharpElementGroup<CSharpMethodDeclaration>>();
		for(DotNetTypeDeclaration decl : decls)
		{
			PsiElement wrappedDeclaration = MsilToCSharpUtil.wrap(decl);

			CSharpResolveContext context = CSharpResolveContextUtil.createContext(DotNetGenericExtractor.EMPTY, myResolveScope, wrappedDeclaration);

			list.addAll(context.getExtensionMethodGroups());
		}
		return list;
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

		PsiElement[] children = myNamespaceAsElement.findChildren(name, myResolveScope, CSharpTransformer.INSTANCE, filter);
		if(children.length > 0)
		{
			PsiElement validWithGeneric = CSharpResolveContextUtil.findValidWithGeneric(holder, children);
			if(validWithGeneric != null)
			{
				return validWithGeneric;
			}

			return children[0];
		}
		return null;
	}

	@NotNull
	@Override
	@LazyInstance
	public Collection<? extends PsiElement> getElements()
	{
		return Arrays.asList(myNamespaceAsElement.getChildren(myResolveScope, CSharpTransformer.INSTANCE,
				DotNetNamespaceAsElement.ChildrenFilter.NONE));
	}
}
