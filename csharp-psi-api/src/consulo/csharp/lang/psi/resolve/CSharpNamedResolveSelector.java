package consulo.csharp.lang.psi.resolve;

import org.jetbrains.annotations.NotNull;

/**
 * @author VISTALL
 * @since 09.10.14
 */
public interface CSharpNamedResolveSelector extends CSharpResolveSelector
{
	boolean isNameEqual(@NotNull String name);
}
