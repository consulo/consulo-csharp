package org.mustbe.consulo.csharp.lang.psi.impl.resolve;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.impl.light.LightElement;

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

	@Override
	public String toString()
	{
		return "CSharpCompositeElementGroup: " + getName();
	}

	@Override
	public void navigate(boolean requestFocus)
	{
		for(PsiElement element : getElements())
		{
			if(element instanceof Navigatable)
			{
				((Navigatable) element).navigate(requestFocus);
				break;
			}
		}
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
}
