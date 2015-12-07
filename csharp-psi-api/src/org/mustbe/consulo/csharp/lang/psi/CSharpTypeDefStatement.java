package org.mustbe.consulo.csharp.lang.psi;

import org.consulo.lombok.annotations.ArrayFactoryFields;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.psi.PsiNameIdentifierOwner;

/**
 * @author VISTALL
 * @since 18.10.14
 */
@ArrayFactoryFields
public interface CSharpTypeDefStatement extends DotNetNamedElement, PsiNameIdentifierOwner, CSharpUsingListChild
{
	@Nullable
	@RequiredReadAction
	DotNetType getType();

	@NotNull
	@RequiredReadAction
	DotNetTypeRef toTypeRef();
}
