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

package consulo.csharp.lang.psi.impl.resolve;

import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.util.ObjectUtil;
import com.intellij.util.Processor;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ConcurrentFactoryMap;
import com.intellij.util.containers.ContainerUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.psi.ToNativeElementTransformers;
import consulo.csharp.lang.psi.impl.msil.CSharpTransformer;
import consulo.csharp.lang.psi.impl.msil.MsilToCSharpUtil;
import consulo.csharp.lang.psi.impl.partial.CSharpCompositeTypeDeclaration;
import consulo.csharp.lang.psi.impl.stub.CSharpMsilStubIndexer;
import consulo.csharp.lang.psi.impl.stub.index.CSharpIndexKeys;
import consulo.csharp.lang.psi.impl.stub.index.ExtensionMethodByNamespacePlusNameIndex;
import consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import consulo.dotnet.lang.psi.impl.BaseDotNetNamespaceAsElement;
import consulo.dotnet.lang.psi.impl.stub.DotNetNamespaceStubUtil;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.resolve.DotNetNamespaceAsElement;
import consulo.msil.lang.psi.MsilClassEntry;
import consulo.msil.lang.psi.MsilEntry;
import consulo.util.dataholder.UserDataHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

/**
 * @author VISTALL
 * @since 07.10.14
 */
public class CSharpNamespaceResolveContext implements CSharpResolveContext
{
	private final DotNetNamespaceAsElement myNamespaceAsElement;
	private final GlobalSearchScope myResolveScope;

	private ConcurrentMap<String, Object> myExtensionGroups = ConcurrentFactoryMap.<String, Object>createMap(name ->
	{
		CSharpElementGroup<CSharpMethodDeclaration> group = findExtensionMethodGroupByName0(name);
		return group == null ? ObjectUtil.NULL : group;
	});

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

		return myNamespaceAsElement.findChildren(name, myResolveScope, CSharpTransformer.INSTANCE, filter);
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
