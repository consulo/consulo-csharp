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
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.Processor;
import com.intellij.util.SmartList;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.CSharpCastType;
import consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import consulo.csharp.lang.psi.CSharpConversionMethodDeclaration;
import consulo.csharp.lang.psi.CSharpIndexMethodDeclaration;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import consulo.csharp.lang.util.ContainerUtil2;
import consulo.util.dataholder.UserDataHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

/**
 * @author VISTALL
 * @since 07.10.14
 */
public class CSharpCompositeResolveContext implements CSharpResolveContext
{
	private final Project myProject;
	private final CSharpResolveContext[] myContexts;

	public CSharpCompositeResolveContext(Project project, CSharpResolveContext... contexts)
	{
		myProject = project;
		myContexts = contexts;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public CSharpElementGroup<CSharpIndexMethodDeclaration> indexMethodGroup(boolean deep)
	{
		List<CSharpElementGroup<CSharpIndexMethodDeclaration>> groups = new SmartList<CSharpElementGroup<CSharpIndexMethodDeclaration>>();
		for(CSharpResolveContext context : myContexts)
		{
			CSharpElementGroup<CSharpIndexMethodDeclaration> elementGroup = context.indexMethodGroup(deep);
			if(elementGroup != null)
			{
				groups.add(elementGroup);
			}
		}
		return groups.isEmpty() ? null : new CSharpCompositeElementGroupImpl<CSharpIndexMethodDeclaration>(myProject, groups);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public CSharpElementGroup<CSharpConstructorDeclaration> constructorGroup()
	{
		List<CSharpElementGroup<CSharpConstructorDeclaration>> groups = new SmartList<CSharpElementGroup<CSharpConstructorDeclaration>>();
		for(CSharpResolveContext context : myContexts)
		{
			CSharpElementGroup<CSharpConstructorDeclaration> elementGroup = context.constructorGroup();
			if(elementGroup != null)
			{
				groups.add(elementGroup);
			}
		}
		return groups.isEmpty() ? null : new CSharpCompositeElementGroupImpl<CSharpConstructorDeclaration>(myProject, groups);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public CSharpElementGroup<CSharpConstructorDeclaration> deConstructorGroup()
	{
		List<CSharpElementGroup<CSharpConstructorDeclaration>> groups = new SmartList<CSharpElementGroup<CSharpConstructorDeclaration>>();
		for(CSharpResolveContext context : myContexts)
		{
			CSharpElementGroup<CSharpConstructorDeclaration> elementGroup = context.deConstructorGroup();
			if(elementGroup != null)
			{
				groups.add(elementGroup);
			}
		}
		return groups.isEmpty() ? null : new CSharpCompositeElementGroupImpl<CSharpConstructorDeclaration>(myProject, groups);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public CSharpElementGroup<CSharpMethodDeclaration> findOperatorGroupByTokenType(@Nonnull IElementType type, boolean deep)
	{
		List<CSharpElementGroup<CSharpMethodDeclaration>> groups = new SmartList<CSharpElementGroup<CSharpMethodDeclaration>>();
		for(CSharpResolveContext context : myContexts)
		{
			CSharpElementGroup<CSharpMethodDeclaration> elementGroup = context.findOperatorGroupByTokenType(type, deep);
			if(elementGroup != null)
			{
				groups.add(elementGroup);
			}
		}
		return groups.isEmpty() ? null : new CSharpCompositeElementGroupImpl<CSharpMethodDeclaration>(myProject, groups);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public CSharpElementGroup<CSharpConversionMethodDeclaration> findConversionMethodGroup(@Nonnull CSharpCastType castType, boolean deep)
	{
		List<CSharpElementGroup<CSharpConversionMethodDeclaration>> groups = new SmartList<CSharpElementGroup<CSharpConversionMethodDeclaration>>();
		for(CSharpResolveContext context : myContexts)
		{
			CSharpElementGroup<CSharpConversionMethodDeclaration> elementGroup = context.findConversionMethodGroup(castType, deep);
			if(elementGroup != null)
			{
				groups.add(elementGroup);
			}
		}
		return groups.isEmpty() ? null : new CSharpCompositeElementGroupImpl<CSharpConversionMethodDeclaration>(myProject, groups);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public CSharpElementGroup<CSharpMethodDeclaration> findExtensionMethodGroupByName(@Nonnull String name)
	{
		List<CSharpElementGroup<CSharpMethodDeclaration>> groups = new SmartList<CSharpElementGroup<CSharpMethodDeclaration>>();
		for(CSharpResolveContext context : myContexts)
		{
			CSharpElementGroup<CSharpMethodDeclaration> elementGroup = context.findExtensionMethodGroupByName(name);
			if(elementGroup != null)
			{
				groups.add(elementGroup);
			}
		}
		return groups.isEmpty() ? null : new CSharpCompositeElementGroupImpl<CSharpMethodDeclaration>(myProject, groups);
	}

	@RequiredReadAction
	@Override
	public boolean processExtensionMethodGroups(@Nonnull Processor<CSharpMethodDeclaration> processor)
	{
		for(CSharpResolveContext context : myContexts)
		{
			if(!context.processExtensionMethodGroups(processor))
			{
				return false;
			}
		}
		return true;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public Collection<PsiElement> findByName(@Nonnull String name, boolean deep, @Nonnull UserDataHolder holder)
	{
		List<Collection<? extends PsiElement>> list = new SmartList<>();
		for(CSharpResolveContext context : myContexts)
		{
			Collection<PsiElement> byName = context.findByName(name, deep, holder);
			list.add(byName);
		}
		return ContainerUtil2.concat(list);
	}

	@RequiredReadAction
	@Override
	public boolean processElements(@Nonnull Processor<PsiElement> processor, boolean deep)
	{
		for(CSharpResolveContext context : myContexts)
		{
			ProgressManager.checkCanceled();

			if(!context.processElements(processor, deep))
			{
				return false;
			}
		}
		return true;
	}

	@Nonnull
	@Override
	public PsiElement getElement()
	{
		throw new IllegalArgumentException("Composite context");
	}

	@Nonnull
	public CSharpResolveContext[] getContexts()
	{
		return myContexts;
	}
}
