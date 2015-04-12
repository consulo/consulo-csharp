package org.musbe.consulo.csharp.parsing;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;

/**
 * @author VISTALL
 * @since 12.04.2015
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface SetLanguageVersion
{
	@NotNull
	CSharpLanguageVersion version() default CSharpLanguageVersion._1_0;
}
