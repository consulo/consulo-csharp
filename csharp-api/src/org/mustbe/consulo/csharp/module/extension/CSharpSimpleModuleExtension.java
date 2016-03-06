package org.mustbe.consulo.csharp.module.extension;

import org.consulo.module.extension.ModuleExtension;
import org.jetbrains.annotations.NotNull;

/**
 * @author VISTALL
 * @since 07.06.2015
 */
public interface CSharpSimpleModuleExtension<T extends ModuleExtension<T>> extends ModuleExtension<T>
{
	boolean isAllowUnsafeCode();

	@NotNull
	CSharpLanguageVersion getLanguageVersion();

	boolean isSupportedLanguageVersion(@NotNull CSharpLanguageVersion languageVersion);
}
