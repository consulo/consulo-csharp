package org.mustbe.consulo.csharp.lang.psi.impl.resolve;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.dotnet.psi.DotNetElement;
import com.intellij.openapi.extensions.ExtensionPointName;

/**
 * @author VISTALL
 * @since 26.10.14
 */
public interface CSharpAdditionalMemberProvider
{
	ExtensionPointName<CSharpAdditionalMemberProvider> EP_NAME =
			ExtensionPointName.create("org.mustbe.consulo.csharp.additionalMemberProvider");

	@NotNull
	DotNetElement[] getAdditionalMembers(@NotNull DotNetElement element);
}
