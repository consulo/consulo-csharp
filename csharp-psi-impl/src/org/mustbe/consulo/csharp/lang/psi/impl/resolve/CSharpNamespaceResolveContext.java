package org.mustbe.consulo.csharp.lang.psi.impl.resolve;

import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpArrayMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpConversionMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.CSharpTransformer;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.MsilToCSharpUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.index.CSharpIndexKeys;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.index.TypeWithExtensionMethodsIndex;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import org.mustbe.consulo.dotnet.lang.psi.impl.BaseDotNetNamespaceAsElement;
import org.mustbe.consulo.dotnet.lang.psi.impl.stub.DotNetNamespaceStubUtil;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetNamespaceAsElement;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.Processor;
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

	@Override
	public boolean processExtensionMethodGroups(@NotNull final Processor<CSharpElementGroup<CSharpMethodDeclaration>> processor)
	{
		return processExtensionMethodGroups(myNamespaceAsElement.getPresentableQName(), myNamespaceAsElement.getProject(), myResolveScope, processor);
	}

	public static boolean processExtensionMethodGroups(final String qName,
			final Project project,
			final GlobalSearchScope scope,
			final Processor<CSharpElementGroup<CSharpMethodDeclaration>> processor)
	{
		String indexableName = DotNetNamespaceStubUtil.getIndexableNamespace(qName);

		return StubIndex.getInstance().processElements(CSharpIndexKeys.TYPE_WITH_EXTENSION_METHODS_INDEX, indexableName, project, scope,
				DotNetTypeDeclaration.class, new Processor<DotNetTypeDeclaration>()
		{
			@Override
			public boolean process(DotNetTypeDeclaration typeDeclaration)
			{
				PsiElement wrappedDeclaration = MsilToCSharpUtil.wrap(typeDeclaration);

				CSharpResolveContext context = CSharpResolveContextUtil.createContext(DotNetGenericExtractor.EMPTY, scope, wrappedDeclaration);

				return context.processExtensionMethodGroups(processor);
			}
		});
	}

	@NotNull
	@Override
	public PsiElement[] findByName(@NotNull String name, boolean deep, @NotNull UserDataHolder holder)
	{
		DotNetNamespaceAsElement.ChildrenFilter filter = holder.getUserData(BaseDotNetNamespaceAsElement.FILTER);
		if(filter == null)
		{
			filter = DotNetNamespaceAsElement.ChildrenFilter.NONE;
		}

		return myNamespaceAsElement.findChildren(name, myResolveScope, CSharpTransformer.INSTANCE, filter);
	}

	@Override
	public boolean processElements(@NotNull Processor<PsiElement> processor, boolean deep)
	{
		for(PsiElement element : myNamespaceAsElement.getChildren(myResolveScope, CSharpTransformer.INSTANCE,
				DotNetNamespaceAsElement.ChildrenFilter.NONE))
		{
			if(!processor.process(element))
			{
				return false;
			}
		}
		return true;
	}
}
