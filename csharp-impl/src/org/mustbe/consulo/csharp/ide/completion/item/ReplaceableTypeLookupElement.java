package org.mustbe.consulo.csharp.ide.completion.item;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementDecorator;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 28.07.2015
 */
public class ReplaceableTypeLookupElement extends LookupElementDecorator<LookupElement>
{
	public ReplaceableTypeLookupElement(LookupElement delegate)
	{
		super(delegate);

		PsiElement psiElement = delegate.getPsiElement();
		assert psiElement instanceof DotNetTypeDeclaration;
	}

	@Override
	public boolean equals(Object o)
	{
		if(this == o)
		{
			return true;
		}
		if(o == null || getClass() != o.getClass())
		{
			return false;
		}
		return getOriginal() == ((ReplaceableTypeLookupElement)o).getOriginal();
	}

	@Override
	public int hashCode()
	{
		return getOriginal().hashCode();
	}

	@NotNull
	private DotNetTypeDeclaration getOriginal()
	{
		LookupElement delegate = getDelegate();
		PsiElement psiElement = delegate.getPsiElement();
		assert psiElement != null;
		return (DotNetTypeDeclaration) psiElement.getOriginalElement();
	}
}
