package org.mustbe.consulo.csharp.lang.psi.resolve;

import org.jetbrains.annotations.NotNull;
import consulo.annotations.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.lang.psi.impl.BaseDotNetNamespaceAsElement;
import consulo.dotnet.psi.DotNetInheritUtil;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.resolve.DotNetNamespaceAsElement;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.psi.PsiElement;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 07.10.14
 */
public class AttributeByNameSelector implements CSharpResolveSelector
{
	public static final String AttributeSuffix = "Attribute";

	private String myNameWithAt;

	public AttributeByNameSelector(String nameWithAt)
	{
		myNameWithAt = nameWithAt;
	}

	@NotNull
	@Override
	@RequiredReadAction
	public PsiElement[] doSelectElement(@NotNull CSharpResolveContext context, boolean deep)
	{
		if(myNameWithAt.isEmpty())
		{
			return PsiElement.EMPTY_ARRAY;
		}

		UserDataHolderBase options = new UserDataHolderBase();
		options.putUserData(BaseDotNetNamespaceAsElement.FILTER, DotNetNamespaceAsElement.ChildrenFilter.ONLY_ELEMENTS);

		if(myNameWithAt.charAt(0) == '@')
		{
			return context.findByName(myNameWithAt.substring(1, myNameWithAt.length()), deep, options);
		}
		else
		{
			PsiElement[] array = context.findByName(myNameWithAt, deep, options);

			array = ArrayUtil.mergeArrays(array, context.findByName(myNameWithAt + AttributeSuffix, deep, options));

			return ContainerUtil.findAllAsArray(array, new Condition<PsiElement>()
			{
				@Override
				@RequiredReadAction
				public boolean value(PsiElement element)
				{
					return element instanceof CSharpTypeDeclaration && DotNetInheritUtil.isAttribute((DotNetTypeDeclaration) element);
				}
			});
		}
	}
}
