package org.mustbe.consulo.csharp.lang.psi.impl.resolve;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.ResolveState;
import com.intellij.psi.impl.light.LightElement;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.Processor;

/**
 * @author VISTALL
 * @since 07.10.14
 */
public class CSharpCompositeElementGroupImpl<T extends PsiElement> extends LightElement implements CSharpElementGroup<T>
{
	@NotNull
	private final List<CSharpElementGroup<T>> myGroups;

	public CSharpCompositeElementGroupImpl(@NotNull Project project, @NotNull List<CSharpElementGroup<T>> groups)
	{
		super(PsiManager.getInstance(project), CSharpLanguage.INSTANCE);
		myGroups = groups;
	}

	@Override
	@NotNull
	public String getName()
	{
		return myGroups.get(0).getName();
	}

	@NotNull
	@Override
	public Object getKey()
	{
		return myGroups.get(0).getKey();
	}

	@Override
	public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException
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
	public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
			@NotNull ResolveState state,
			PsiElement lastParent,
			@NotNull PsiElement place)
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


	@NotNull
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
	public boolean process(@NotNull Processor<T> processor)
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
