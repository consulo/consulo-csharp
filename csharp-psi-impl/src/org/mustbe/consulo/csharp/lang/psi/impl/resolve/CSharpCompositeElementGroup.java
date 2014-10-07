package org.mustbe.consulo.csharp.lang.psi.impl.resolve;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.light.LightElement;

/**
 * @author VISTALL
 * @since 07.10.14
 */
public class CSharpCompositeElementGroup extends LightElement implements CSharpElementGroup
{
	@NotNull
	private final List<CSharpElementGroup> myGroups;

	public CSharpCompositeElementGroup(@NotNull Project project, @NotNull List<CSharpElementGroup> groups)
	{
		super(PsiManager.getInstance(project), CSharpLanguage.INSTANCE);
		myGroups = groups;
	}

	@Override
	public String toString()
	{
		return "CSharpCompositeElementGroup";
	}
}
