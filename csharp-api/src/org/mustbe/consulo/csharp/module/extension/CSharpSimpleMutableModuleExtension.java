package org.mustbe.consulo.csharp.module.extension;

import org.consulo.module.extension.MutableModuleExtension;
import org.consulo.module.extension.MutableModuleInheritableNamedPointer;
import org.jetbrains.annotations.NotNull;

/**
 * @author VISTALL
 * @since 07.06.2015
 */
public interface CSharpSimpleMutableModuleExtension<T extends CSharpSimpleModuleExtension<T>> extends CSharpSimpleModuleExtension<T>,
		MutableModuleExtension<T>
{
	MutableModuleInheritableNamedPointer<CSharpLanguageVersion> getLanguageVersionPointer();

	void setLanguageVersion(@NotNull CSharpLanguageVersion version);

	void setAllowUnsafeCode(boolean value);
}
