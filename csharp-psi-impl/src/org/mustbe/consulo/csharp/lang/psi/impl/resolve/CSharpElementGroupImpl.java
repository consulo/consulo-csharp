package org.mustbe.consulo.csharp.lang.psi.impl.resolve;

import java.util.Collection;

import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.light.LightElement;

/**
 * @author VISTALL
 * @since 29.09.14
 */
public class CSharpElementGroupImpl extends LightElement implements CSharpElementGroup
{
	private final Collection<? extends PsiElement> myElements;

	public CSharpElementGroupImpl(Project project, Collection<? extends PsiElement> elements)
	{
		super(PsiManager.getInstance(project), CSharpLanguage.INSTANCE);
		myElements = elements;
	}

	@Override
	public String toString()
	{
		return "CSharpElementGroup";
	}
}
