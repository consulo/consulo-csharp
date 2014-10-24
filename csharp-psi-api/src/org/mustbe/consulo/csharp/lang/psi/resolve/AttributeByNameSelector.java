package org.mustbe.consulo.csharp.lang.psi.resolve;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.dotnet.lang.psi.impl.BaseDotNetNamespaceAsElement;
import org.mustbe.consulo.dotnet.resolve.DotNetNamespaceAsElement;
import com.intellij.openapi.util.UserDataHolderBase;
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
		UserDataHolderBase userDataHolderBase = new UserDataHolderBase();
		userDataHolderBase.putUserData(BaseDotNetNamespaceAsElement.FILTER, DotNetNamespaceAsElement.ChildrenFilter.ONLY_ELEMENTS);

		PsiElement byName = context.findByName(myName, userDataHolderBase);
		if(byName instanceof CSharpTypeDeclaration)
		{
			return byName;
		}

		if(!myName.endsWith(AttributeSuffix))
		{
			byName = context.findByName(myName + AttributeSuffix, userDataHolderBase);
			if(byName instanceof CSharpTypeDeclaration)
			{
				return byName;
			}
		}
		return null;
	}
}
