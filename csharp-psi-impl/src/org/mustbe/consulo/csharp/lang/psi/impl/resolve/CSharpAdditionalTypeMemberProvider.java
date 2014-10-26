package org.mustbe.consulo.csharp.lang.psi.impl.resolve;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetElement;
import com.intellij.openapi.extensions.ExtensionPointName;

/**
 * @author VISTALL
 * @since 26.10.14
 */
public interface CSharpAdditionalTypeMemberProvider
{
	ExtensionPointName<CSharpAdditionalTypeMemberProvider> EP_NAME =
			ExtensionPointName.create("org.mustbe.consulo.csharp.additionalTypeMemberProvider");

	@NotNull
	DotNetElement[] getAdditionalMembers(@NotNull CSharpTypeDeclaration typeDeclaration);
}
