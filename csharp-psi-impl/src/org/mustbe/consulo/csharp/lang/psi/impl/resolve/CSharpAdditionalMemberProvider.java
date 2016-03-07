package org.mustbe.consulo.csharp.lang.psi.impl.resolve;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.dotnet.psi.DotNetElement;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.psi.PsiElement;
import com.intellij.util.Consumer;

/**
 * @author VISTALL
 * @since 26.10.14
 */
public interface CSharpAdditionalMemberProvider
{
	ExtensionPointName<CSharpAdditionalMemberProvider> EP_NAME = ExtensionPointName.create("org.mustbe.consulo.csharp.additionalMemberProvider");

	enum Target
	{
		CONSTRUCTOR,
		DE_CONSTRUCTOR,
		INDEX_METHOD,
		CONVERSION_METHOD,
		OPERATOR_METHOD,
		OTHER
	}

	@RequiredReadAction
	void processAdditionalMembers(@NotNull DotNetElement element, @NotNull DotNetGenericExtractor extractor, @NotNull Consumer<PsiElement> consumer);

	@NotNull
	Target getTarget();
}
