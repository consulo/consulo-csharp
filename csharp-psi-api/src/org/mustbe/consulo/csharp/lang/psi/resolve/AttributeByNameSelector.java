package org.mustbe.consulo.csharp.lang.psi.resolve;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.dotnet.lang.psi.impl.BaseDotNetNamespaceAsElement;
import org.mustbe.consulo.dotnet.resolve.DotNetNamespaceAsElement;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.psi.PsiElement;
import com.intellij.util.ArrayUtil;

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

	@NotNull
	@Override
	public PsiElement[] doSelectElement(@NotNull CSharpResolveContext context, boolean deep)
	{
		UserDataHolderBase userDataHolderBase = new UserDataHolderBase();
		userDataHolderBase.putUserData(BaseDotNetNamespaceAsElement.FILTER, DotNetNamespaceAsElement.ChildrenFilter.ONLY_ELEMENTS);

		PsiElement[] array = context.findByName(myName, deep, userDataHolderBase);

		if(!myName.endsWith(AttributeSuffix))
		{
			array = ArrayUtil.mergeArrays(array, context.findByName(myName + AttributeSuffix, deep, userDataHolderBase));
		}
		return array;
	}
}
