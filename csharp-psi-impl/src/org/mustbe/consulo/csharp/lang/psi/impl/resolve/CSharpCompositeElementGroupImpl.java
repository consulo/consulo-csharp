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
import com.intellij.psi.impl.light.LightElement;

/**
 * @author VISTALL
 * @since 07.10.14
 */
public class CSharpCompositeElementGroupImpl extends LightElement implements CSharpElementGroup
{
	@NotNull
	private final List<CSharpElementGroup> myGroups;

	public CSharpCompositeElementGroupImpl(@NotNull Project project, @NotNull List<CSharpElementGroup> groups)
	{
		super(PsiManager.getInstance(project), CSharpLanguage.INSTANCE);
		myGroups = groups;
	}

	@Override
	public String toString()
	{
		return "CSharpCompositeElementGroup";
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
	public Collection<? extends PsiElement> getElements()
	{
		List list = new ArrayList<PsiElement>();
		for(CSharpElementGroup group : myGroups)
		{
			list.addAll(group.getElements());
		}
		return list;
	}
}
