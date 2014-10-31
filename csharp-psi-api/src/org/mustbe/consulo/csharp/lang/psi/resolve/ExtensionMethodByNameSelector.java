package org.mustbe.consulo.csharp.lang.psi.resolve;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 07.10.14
 */
public class ExtensionMethodByNameSelector implements CSharpResolveSelector
{
	private final String myName;

	public ExtensionMethodByNameSelector(String name)
	{
		myName = name;
	}

	@NotNull
	@Override
	public PsiElement[] doSelectElement(@NotNull CSharpResolveContext context, boolean deep)
	{
		CSharpElementGroup<CSharpMethodDeclaration> groupByName = context.findExtensionMethodGroupByName(myName);
		if(groupByName == null)
		{
			return PsiElement.EMPTY_ARRAY;
		}
		return new PsiElement[] {groupByName};
	}
}
