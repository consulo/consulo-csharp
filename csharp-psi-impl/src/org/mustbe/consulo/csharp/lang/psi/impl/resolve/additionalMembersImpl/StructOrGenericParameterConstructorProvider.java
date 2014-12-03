package org.mustbe.consulo.csharp.lang.psi.impl.resolve.additionalMembersImpl;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.light.builder.CSharpLightConstructorDeclarationBuilder;
import org.mustbe.consulo.csharp.lang.psi.impl.resolve.CSharpAdditionalMemberProvider;
import org.mustbe.consulo.dotnet.psi.DotNetConstructorDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetElement;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;

/**
 * @author VISTALL
 * @since 26.10.14
 */
public class StructOrGenericParameterConstructorProvider implements CSharpAdditionalMemberProvider
{
	@NotNull
	@Override
	public DotNetElement[] getAdditionalMembers(@NotNull DotNetElement element)
	{
		if(element instanceof CSharpTypeDeclaration && ((CSharpTypeDeclaration) element).isStruct())
		{
			return buildDefaultConstructor((DotNetNamedElement) element, CSharpModifier.PUBLIC);
		}
		else if(element instanceof CSharpTypeDeclaration && !((CSharpTypeDeclaration) element).isInterface() && !((CSharpTypeDeclaration) element)
				.isStruct())
		{
			CSharpTypeDeclaration typeDeclaration = (CSharpTypeDeclaration) element;
			DotNetNamedElement[] members = typeDeclaration.getMembers();
			boolean foundConstructors = false;
			for(DotNetNamedElement member : members)
			{
				if(member instanceof DotNetConstructorDeclaration && !((DotNetConstructorDeclaration) member).isDeConstructor())
				{
					foundConstructors = true;
					break;
				}
			}

			if(!foundConstructors)
			{
				CSharpModifier modifier = typeDeclaration.hasModifier(CSharpModifier.ABSTRACT) ? CSharpModifier.PROTECTED : CSharpModifier.PUBLIC;

				return buildDefaultConstructor((DotNetNamedElement) element, modifier);
			}
		}
		else if(element instanceof DotNetGenericParameter)
		{
			// we need always create contructor, it ill check in CS0304
			return buildDefaultConstructor((DotNetNamedElement) element, CSharpModifier.PUBLIC);
		}
		return DotNetElement.EMPTY_ARRAY;
	}

	@NotNull
	private static DotNetElement[] buildDefaultConstructor(DotNetNamedElement element, @NotNull CSharpModifier modifier)
	{
		String name = element.getName();
		if(name == null)
		{
			return DotNetElement.EMPTY_ARRAY;
		}
		CSharpLightConstructorDeclarationBuilder builder = new CSharpLightConstructorDeclarationBuilder(element.getProject());
		builder.addModifier(modifier);
		builder.setNavigationElement(element);
		builder.withParent(element);
		builder.withName(name);
		return new DotNetElement[]{builder};
	}
}
