package org.mustbe.consulo.csharp.lang.psi.resolve;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 07.10.14
 */
public class MemberByNameSelector extends UserDataHolderBase implements CSharpNamedResolveSelector
{
	private String myName;

	public MemberByNameSelector(String name)
	{
		myName = name;
	}

	@NotNull
	@Override
	@SuppressWarnings("unchecked")
	public PsiElement[] doSelectElement(@NotNull CSharpResolveContext context, boolean deep)
	{
		return context.findByName(myName, deep, this);
	}

	@Override
	public boolean isNameEqual(@NotNull String name)
	{
		return Comparing.equal(myName, name);
	}
}
