package org.mustbe.consulo.csharp.lang.psi.resolve;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 07.10.14
 */
public class AttributeByNameSelector implements CSharpResolveSelector
{
	public static final String AttributeSuffix = "Attribute";

	private String myName;

	public AttributeByNameSelector(String name)
	{
		myName = name;
	}

	@Nullable
	@Override
	public PsiElement doSelectElement(@NotNull CSharpResolveContext context)
	{
		PsiElement byName = context.findByName(myName);
		if(byName instanceof CSharpTypeDeclaration)
		{
			return byName;
		}

		if(!myName.endsWith(AttributeSuffix))
		{
			byName = context.findByName(myName + AttributeSuffix);
			if(byName instanceof CSharpTypeDeclaration)
			{
				return byName;
			}
		}
		return null;
	}
}
