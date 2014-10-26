package org.mustbe.consulo.csharp.lang.psi.impl.resolve.additionalMembersImpl;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.light.builder.CSharpLightConstructorDeclarationBuilder;
import org.mustbe.consulo.csharp.lang.psi.impl.resolve.CSharpAdditionalTypeMemberProvider;
import org.mustbe.consulo.dotnet.psi.DotNetElement;

/**
 * @author VISTALL
 * @since 26.10.14
 */
public class StructConstructorProvider implements CSharpAdditionalTypeMemberProvider
{
	@NotNull
	@Override
	public DotNetElement[] getAdditionalMembers(@NotNull CSharpTypeDeclaration typeDeclaration)
	{
		if(typeDeclaration.isStruct())
		{
			CSharpLightConstructorDeclarationBuilder builder = new CSharpLightConstructorDeclarationBuilder(typeDeclaration.getProject());
			builder.addModifier(CSharpModifier.PUBLIC);
			builder.setNavigationElement(typeDeclaration);
			builder.withParent(typeDeclaration);
			builder.withName(typeDeclaration.getName());
			return new DotNetElement[] {builder};
		}
		return DotNetElement.EMPTY_ARRAY;
	}
}
