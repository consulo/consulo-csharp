package org.mustbe.consulo.csharp.ide.completion.item;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.MsilMethodAsCSharpMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementDecorator;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 28.07.2015
 */
public class ReplaceableTypeLikeLookupElement extends LookupElementDecorator<LookupElement>
{
	public ReplaceableTypeLikeLookupElement(LookupElement delegate)
	{
		super(delegate);

		PsiElement psiElement = delegate.getPsiElement();
		assert psiElement instanceof DotNetQualifiedElement;
	}

	@Override
	@RequiredReadAction
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

		return Comparing.equal(getOriginal().getPresentableQName(), ((ReplaceableTypeLikeLookupElement) o).getOriginal().getPresentableQName());
	}

	@Override
	public int hashCode()
	{
		return getOriginal().hashCode();
	}

	@NotNull
	private DotNetQualifiedElement getOriginal()
	{
		LookupElement delegate = getDelegate();
		PsiElement psiElement = delegate.getPsiElement();
		assert psiElement != null;
		if(psiElement instanceof MsilMethodAsCSharpMethodDeclaration)
		{
			return ((MsilMethodAsCSharpMethodDeclaration) psiElement).getDelegate();
		}
		return (DotNetQualifiedElement) psiElement.getOriginalElement();
	}
}
