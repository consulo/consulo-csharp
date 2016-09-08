package consulo.csharp.module.extension;

import org.jetbrains.annotations.NotNull;
import consulo.module.extension.MutableModuleExtension;
import consulo.module.extension.MutableModuleInheritableNamedPointer;

/**
 * @author VISTALL
 * @since 07.06.2015
 */
public interface CSharpSimpleMutableModuleExtension<T extends CSharpSimpleModuleExtension<T>> extends CSharpSimpleModuleExtension<T>, MutableModuleExtension<T>
{
	MutableModuleInheritableNamedPointer<CSharpLanguageVersion> getLanguageVersionPointer();

	void setLanguageVersion(@NotNull CSharpLanguageVersion version);

	void setAllowUnsafeCode(boolean value);
}
