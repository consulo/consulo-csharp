/*
 * Copyright 2013-2017 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package consulo.csharp.lang.impl.psi.resolve;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.progress.ProgressManager;
import consulo.application.util.ConcurrentFactoryMap;
import consulo.application.util.function.Processor;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.impl.psi.ToNativeElementTransformers;
import consulo.csharp.lang.impl.psi.msil.CSharpTransformer;
import consulo.csharp.lang.impl.psi.msil.MsilToCSharpUtil;
import consulo.csharp.lang.impl.psi.partial.CSharpCompositeTypeDeclaration;
import consulo.csharp.lang.impl.psi.stub.CSharpMsilStubIndexer;
import consulo.csharp.lang.impl.psi.stub.index.CSharpIndexKeys;
import consulo.csharp.lang.impl.psi.stub.index.ExtensionMethodByNamespacePlusNameIndex;
import consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.impl.BaseDotNetNamespaceAsElement;
import consulo.dotnet.psi.impl.stub.DotNetNamespaceStubUtil;
import consulo.dotnet.psi.resolve.DotNetNamespaceAsElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.stub.StubIndex;
import consulo.msil.lang.psi.MsilClassEntry;
import consulo.msil.lang.psi.MsilEntry;
import consulo.project.Project;
import consulo.util.collection.ContainerUtil;
import consulo.util.collection.SmartList;
import consulo.util.dataholder.UserDataHolder;
import consulo.util.lang.ObjectUtil;
import consulo.util.lang.StringUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author VISTALL
 * @since 07.10.14
 */
public class CSharpNamespaceResolveContext implements CSharpResolveContext
{
	private record NamespaceByNameKey(String name,DotNetNamespaceAsElement.ChildrenFilter filter)
	{
	}

	private final DotNetNamespaceAsElement myNamespaceAsElement;
	private final GlobalSearchScope myResolveScope;

	private final ConcurrentMap<String, Object> myExtensionGroups = ConcurrentFactoryMap.<String, Object>createMap(name ->
	{
		CSharpElementGroup<CSharpMethodDeclaration> group = findExtensionMethodGroupByName0(name);
		return group == null ? ObjectUtil.NULL : group;
	});

	private final Map<NamespaceByNameKey, Collection<PsiElement>> myByNameCache = new ConcurrentHashMap<>();

	public CSharpNamespaceResolveContext(DotNetNamespaceAsElement namespaceAsElement, GlobalSearchScope resolveScope)
	{
		myNamespaceAsElement = namespaceAsElement;
		myResolveScope = resolveScope;
	}

	@RequiredReadAction
	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public CSharpElementGroup<CSharpMethodDeclaration> findExtensionMethodGroupByName(@Nonnull String name)
	{
		Object o = myExtensionGroups.get(name);
		return o == ObjectUtil.NULL ? null : (CSharpElementGroup<CSharpMethodDeclaration>) o;
	}

	@RequiredReadAction
	private CSharpElementGroup<CSharpMethodDeclaration> findExtensionMethodGroupByName0(@Nonnull String name)
	{
		int indexKey = CSharpMsilStubIndexer.makeExtensionMethodIndexKey(myNamespaceAsElement.getPresentableQName(), name);
		Collection<DotNetLikeMethodDeclaration> declarations = ExtensionMethodByNamespacePlusNameIndex.getInstance().get(indexKey, myNamespaceAsElement.getProject(), myResolveScope);

		if(declarations.isEmpty())
		{
			return null;
		}

		List<CSharpMethodDeclaration> methodDeclarations = new SmartList<>();
		for(DotNetLikeMethodDeclaration methodDeclaration : declarations)
		{
			ProgressManager.checkCanceled();

			ContainerUtil.addIfNotNull(methodDeclarations, convertMethodIfNeed(methodDeclaration));
		}
		return new CSharpElementGroupImpl<>(myNamespaceAsElement.getProject(), name, methodDeclarations);
	}

	@RequiredReadAction
	@Override
	public boolean processExtensionMethodGroups(@Nonnull final Processor<CSharpMethodDeclaration> processor)
	{
		int indexKey = DotNetNamespaceStubUtil.getIndexableNamespace(StringUtil.notNullize(myNamespaceAsElement.getPresentableQName())).hashCode();

		Project project = myNamespaceAsElement.getProject();
		return StubIndex.getInstance().processElements(CSharpIndexKeys.EXTENSION_METHOD_BY_NAMESPACE, indexKey, project, myResolveScope, DotNetLikeMethodDeclaration.class, it ->
		{
			CSharpMethodDeclaration declaration = convertMethodIfNeed(it);
			if(declaration == null)
			{
				return true;
			}

			// just return not grouped
			return processor.process(declaration);
		});
	}

	@Nullable
	@RequiredReadAction
	private CSharpMethodDeclaration convertMethodIfNeed(@Nonnull DotNetLikeMethodDeclaration methodDeclaration)
	{
		if(methodDeclaration instanceof MsilEntry)
		{
			PsiElement parent = methodDeclaration.getParent();

			if(parent instanceof MsilClassEntry)
			{
				PsiElement maybeClassEntry = ToNativeElementTransformers.transform(parent);

				if(maybeClassEntry instanceof CSharpTypeDeclaration)
				{
					for(DotNetNamedElement element : ((CSharpTypeDeclaration) maybeClassEntry).getMembers())
					{
						if(element instanceof CSharpMethodDeclaration && element.getOriginalElement().isEquivalentTo(methodDeclaration))
						{
							return (CSharpMethodDeclaration) element;
						}
					}
				}
			}
		}
		else if(methodDeclaration instanceof CSharpMethodDeclaration)
		{
			return (CSharpMethodDeclaration) methodDeclaration;
		}
		return null;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public Collection<PsiElement> findByName(@Nonnull String name, boolean deep, @Nonnull UserDataHolder holder)
	{
		DotNetNamespaceAsElement.ChildrenFilter filter = holder.getUserData(BaseDotNetNamespaceAsElement.FILTER);
		if(filter == null)
		{
			filter = DotNetNamespaceAsElement.ChildrenFilter.NONE;
		}

		return myByNameCache.computeIfAbsent(new NamespaceByNameKey(name, filter), key -> myNamespaceAsElement.findChildren(key.name(), myResolveScope, CSharpTransformer.INSTANCE, key.filter()));
	}

	@RequiredReadAction
	@Override
	public boolean processElements(@Nonnull Processor<PsiElement> processor, boolean deep)
	{
		Set<String> partialTypesVisit = new HashSet<>();
		return myNamespaceAsElement.processChildren(myResolveScope, CSharpTransformer.INSTANCE, DotNetNamespaceAsElement.ChildrenFilter.NONE, element ->
		{
			ProgressManager.checkCanceled();

			if(element instanceof MsilClassEntry)
			{
				if(!processor.process(MsilToCSharpUtil.wrap(element, null)))
				{
					return false;
				}
			}
			else if(element instanceof CSharpTypeDeclaration && ((CSharpTypeDeclaration) element).hasModifier(CSharpModifier.PARTIAL))
			{
				// already processed
				if(!partialTypesVisit.add(((CSharpTypeDeclaration) element).getVmQName()))
				{
					return true;
				}

				DotNetTypeDeclaration type = CSharpCompositeTypeDeclaration.selectCompositeOrSelfType((CSharpTypeDeclaration) element);

				if(!processor.process(type))
				{
					return false;
				}
			}

			return processor.process(element);
		});
	}

	@Nonnull
	@Override
	public PsiElement getElement()
	{
		return myNamespaceAsElement;
	}
}
