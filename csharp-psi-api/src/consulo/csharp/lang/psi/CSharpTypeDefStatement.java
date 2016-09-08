package consulo.csharp.lang.psi;

import consulo.lombok.annotations.ArrayFactoryFields;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredReadAction;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.resolve.DotNetTypeRef;
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
