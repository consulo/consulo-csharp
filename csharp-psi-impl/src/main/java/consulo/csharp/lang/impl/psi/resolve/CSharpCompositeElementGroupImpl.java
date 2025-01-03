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

import consulo.application.util.function.Processor;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import consulo.language.impl.psi.LightElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiManager;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.resolve.ResolveState;
import consulo.language.util.IncorrectOperationException;
import consulo.navigation.Navigatable;
import consulo.project.Project;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author VISTALL
 * @since 07.10.14
 */
public class CSharpCompositeElementGroupImpl<T extends PsiElement> extends LightElement implements CSharpElementGroup<T>
{
	@Nonnull
	private final List<CSharpElementGroup<T>> myGroups;

	public CSharpCompositeElementGroupImpl(@Nonnull Project project, @Nonnull List<CSharpElementGroup<T>> groups)
	{
		super(PsiManager.getInstance(project), CSharpLanguage.INSTANCE);
		myGroups = groups;
	}

	@Override
	@Nonnull
	public String getName()
	{
		return myGroups.get(0).getName();
	}

	@Nonnull
	@Override
	public Object getKey()
	{
		return myGroups.get(0).getKey();
	}

	@Override
	public PsiElement setName(@NonNls @Nonnull String name) throws IncorrectOperationException
	{
		for(CSharpElementGroup<T> group : myGroups)
		{
			group.setName(name);
		}
		return this;
	}

	@Override
	public String toString()
	{
		return "CSharpCompositeElementGroup: " + getName();
	}

	@Override
	public boolean canNavigate()
	{
		return true;
	}

	@Override
	public void navigate(final boolean requestFocus)
	{
		process(new Processor<T>()
		{
			@Override
			public boolean process(T t)
			{
				if(t instanceof Navigatable)
				{
					((Navigatable) t).navigate(requestFocus);
					return false;
				}
				return true;
			}
		});
	}

	@Override
	public boolean processDeclarations(@Nonnull PsiScopeProcessor processor,
			@Nonnull ResolveState state,
			PsiElement lastParent,
			@Nonnull PsiElement place)
	{
		for(CSharpElementGroup<T> element : myGroups)
		{
			if(!processor.execute(element, state))
			{
				return false;
			}
		}
		return true;
	}


	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	public Collection<T> getElements()
	{
		List list = new ArrayList<PsiElement>();
		for(CSharpElementGroup<T> group : myGroups)
		{
			list.addAll(group.getElements());
		}
		return list;
	}

	@Override
	public boolean process(@Nonnull Processor<? super T> processor)
	{
		for(CSharpElementGroup<T> group : myGroups)
		{
			if(!group.process(processor))
			{
				return false;
			}
		}
		return true;
	}
}
