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

import gnu.trove.THashSet;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.util.ObjectUtil;
import com.intellij.util.Processor;
import com.intellij.util.Processors;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ConcurrentFactoryMap;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.psi.ToNativeElementTransformers;
import consulo.csharp.lang.psi.impl.msil.CSharpTransformer;
import consulo.csharp.lang.psi.impl.partial.CSharpCompositeTypeDeclaration;
import consulo.csharp.lang.psi.impl.stub.index.CSharpIndexKeys;
import consulo.csharp.lang.psi.impl.stub.index.TypeWithExtensionMethodsIndex;
import consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import consulo.dotnet.lang.psi.impl.BaseDotNetNamespaceAsElement;
import consulo.dotnet.lang.psi.impl.stub.DotNetNamespaceStubUtil;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import consulo.dotnet.resolve.DotNetNamespaceAsElement;

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

	private NotNullLazyValue<List<CSharpResolveContext>> myExtensionContexts = NotNullLazyValue.createValue(this::collectResolveContexts);

	public CSharpNamespaceResolveContext(DotNetNamespaceAsElement namespaceAsElement, GlobalSearchScope resolveScope)
	{
		myNamespaceAsElement = namespaceAsElement;
		myResolveScope = resolveScope;
	}

	@RequiredReadAction
	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public CSharpElementGroup<CSharpMethodDeclaration> findExtensionMethodGroupByName(@NotNull String name)
	{
		Object o = myExtensionGroups.get(name);
		return o == ObjectUtil.NULL ? null : (CSharpElementGroup<CSharpMethodDeclaration>) o;
	}

	@RequiredReadAction
	private CSharpElementGroup<CSharpMethodDeclaration> findExtensionMethodGroupByName0(@NotNull String name)
	{
		String presentableName = DotNetNamespaceStubUtil.getIndexableNamespace(myNamespaceAsElement.getPresentableQName());

		Collection<DotNetTypeDeclaration> decls = TypeWithExtensionMethodsIndex.getInstance().get(presentableName, myNamespaceAsElement.getProject(), myResolveScope);

		if(decls.isEmpty())
		{
			return null;
		}
		List<CSharpElementGroup<CSharpMethodDeclaration>> list = new SmartList<>();
		Set<String> processed = new THashSet<>();
		for(DotNetTypeDeclaration typeDeclaration : decls)
		{
			ProgressManager.checkCanceled();

			PsiElement wrappedDeclaration = ToNativeElementTransformers.transform(typeDeclaration);

			if(typeDeclaration instanceof CSharpTypeDeclaration && typeDeclaration.hasModifier(CSharpModifier.PARTIAL))
			{
				String vmQName = typeDeclaration.getVmQName();
				if(processed.contains(vmQName))
				{
					continue;
				}
				processed.add(vmQName);
			}

			CSharpResolveContext context = CSharpResolveContextUtil.createContext(DotNetGenericExtractor.EMPTY, myResolveScope, wrappedDeclaration);

			CSharpElementGroup<CSharpMethodDeclaration> extensionMethodByName = context.findExtensionMethodGroupByName(name);
			if(extensionMethodByName != null)
			{
				list.add(extensionMethodByName);
			}
		}
		return new CSharpCompositeElementGroupImpl<>(myNamespaceAsElement.getProject(), list);
	}

	@RequiredReadAction
	@Override
	public boolean processExtensionMethodGroups(@NotNull final Processor<CSharpElementGroup<CSharpMethodDeclaration>> processor)
	{
		for(CSharpResolveContext context : myExtensionContexts.getValue())
		{
			if(!context.processExtensionMethodGroups(processor))
			{
				return false;
			}
		}
		return true;
	}

	@RequiredReadAction
	public List<CSharpResolveContext> collectResolveContexts()
	{
		String qName = myNamespaceAsElement.getPresentableQName();
		if(qName == null)
		{
			return Collections.emptyList();
		}

		Project project = myNamespaceAsElement.getProject();
		String indexableName = DotNetNamespaceStubUtil.getIndexableNamespace(qName);

		Set<DotNetTypeDeclaration> result = new THashSet<>();

		StubIndex.getInstance().processElements(CSharpIndexKeys.TYPE_WITH_EXTENSION_METHODS_INDEX, indexableName, project, myResolveScope, DotNetTypeDeclaration.class, Processors
				.cancelableCollectProcessor(result));

		Set<String> processed = new THashSet<>();

		List<CSharpResolveContext> list = new SmartList<>();
		for(DotNetTypeDeclaration typeDeclaration : result)
		{
			ProgressManager.checkCanceled();

			PsiElement wrappedDeclaration = ToNativeElementTransformers.transform(typeDeclaration);

			if(typeDeclaration instanceof CSharpTypeDeclaration && typeDeclaration.hasModifier(CSharpModifier.PARTIAL))
			{
				String vmQName = typeDeclaration.getVmQName();
				if(processed.contains(vmQName))
				{
					continue;
				}
				processed.add(vmQName);
			}

			CSharpResolveContext context = CSharpResolveContextUtil.createContext(DotNetGenericExtractor.EMPTY, myResolveScope, wrappedDeclaration);

			list.add(context);
		}
		return list.isEmpty() ? Collections.emptyList() : list;
	}

	@RequiredReadAction
	@NotNull
	@Override
	public Collection<PsiElement> findByName(@NotNull String name, boolean deep, @NotNull UserDataHolder holder)
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
	public boolean processElements(@NotNull Processor<PsiElement> processor, boolean deep)
	{
		DotNetNamespaceAsElement.ChildrenFilter filter = DotNetNamespaceAsElement.ChildrenFilter.ONLY_ELEMENTS;
		if(StringUtil.isEmpty(myNamespaceAsElement.getPresentableQName()))
		{
			filter = DotNetNamespaceAsElement.ChildrenFilter.NONE;
		}
		Collection<PsiElement> children = myNamespaceAsElement.getChildren(myResolveScope, CSharpTransformer.INSTANCE, filter);
		children = CSharpCompositeTypeDeclaration.wrapPartialTypes(myResolveScope, myNamespaceAsElement.getProject(), children);

		for(PsiElement element : children)
		{
			ProgressManager.checkCanceled();
			if(!processor.process(element))
			{
				return false;
			}
		}
		return true;
	}

	@NotNull
	@Override
	public PsiElement getElement()
	{
		return myNamespaceAsElement;
	}
}
