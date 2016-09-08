package org.mustbe.consulo.csharp.module.extension;

import org.jetbrains.annotations.NotNull;
import consulo.module.extension.ModuleExtension;

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
