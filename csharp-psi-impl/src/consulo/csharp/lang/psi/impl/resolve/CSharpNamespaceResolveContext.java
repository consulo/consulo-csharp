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
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.Processor;
import com.intellij.util.Processors;
import com.intellij.util.SmartList;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.CSharpCastType;
import consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import consulo.csharp.lang.psi.CSharpConversionMethodDeclaration;
import consulo.csharp.lang.psi.CSharpIndexMethodDeclaration;
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

	public CSharpNamespaceResolveContext(DotNetNamespaceAsElement namespaceAsElement, GlobalSearchScope resolveScope)
	{
		myNamespaceAsElement = namespaceAsElement;
		myResolveScope = resolveScope;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public CSharpElementGroup<CSharpIndexMethodDeclaration> indexMethodGroup(boolean deep)
	{
		return null;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public CSharpElementGroup<CSharpConstructorDeclaration> constructorGroup()
	{
		return null;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public CSharpElementGroup<CSharpConstructorDeclaration> deConstructorGroup()
	{
		return null;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public CSharpElementGroup<CSharpMethodDeclaration> findOperatorGroupByTokenType(@NotNull IElementType type, boolean deep)
	{
		return null;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public CSharpElementGroup<CSharpConversionMethodDeclaration> findConversionMethodGroup(@NotNull CSharpCastType castType, boolean deep)
	{
		return null;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public CSharpElementGroup<CSharpMethodDeclaration> findExtensionMethodGroupByName(@NotNull String name)
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
		return processExtensionMethodGroups(myNamespaceAsElement.getPresentableQName(), myNamespaceAsElement.getProject(), myResolveScope, processor);
	}

	@RequiredReadAction
	public static boolean processExtensionMethodGroups(@Nullable final String qName,
			@NotNull final Project project,
			@NotNull final GlobalSearchScope scope,
			@NotNull final Processor<CSharpElementGroup<CSharpMethodDeclaration>> processor)
	{
		if(qName == null)
		{
			return true;
		}
		String indexableName = DotNetNamespaceStubUtil.getIndexableNamespace(qName);

		Set<DotNetTypeDeclaration> result = new THashSet<>();

		StubIndex.getInstance().processElements(CSharpIndexKeys.TYPE_WITH_EXTENSION_METHODS_INDEX, indexableName, project, scope, DotNetTypeDeclaration.class, Processors.cancelableCollectProcessor
				(result));

		Set<String> processed = new THashSet<>();

		for(DotNetTypeDeclaration typeDeclaration : result)
		{
			ProgressManager.checkCanceled();

			PsiElement wrappedDeclaration = ToNativeElementTransformers.transform(typeDeclaration);

			if(typeDeclaration instanceof CSharpTypeDeclaration && typeDeclaration.hasModifier(CSharpModifier.PARTIAL))
			{
				String vmQName = typeDeclaration.getVmQName();
				if(processed.contains(vmQName))
				{
					return true;
				}
				processed.add(vmQName);
			}

			CSharpResolveContext context = CSharpResolveContextUtil.createContext(DotNetGenericExtractor.EMPTY, scope, wrappedDeclaration);

			if(!context.processExtensionMethodGroups(processor))
			{
				return false;
			}
		}
		return true;
	}

	@RequiredReadAction
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

	@RequiredReadAction
	@Override
	public boolean processElements(@NotNull Processor<PsiElement> processor, boolean deep)
	{
		DotNetNamespaceAsElement.ChildrenFilter filter = DotNetNamespaceAsElement.ChildrenFilter.ONLY_ELEMENTS;
		if(StringUtil.isEmpty(myNamespaceAsElement.getPresentableQName()))
		{
			filter = DotNetNamespaceAsElement.ChildrenFilter.NONE;
		}
		PsiElement[] children = myNamespaceAsElement.getChildren(myResolveScope, CSharpTransformer.INSTANCE, filter);
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
