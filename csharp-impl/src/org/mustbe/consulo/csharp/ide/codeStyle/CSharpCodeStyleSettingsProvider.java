package org.mustbe.consulo.csharp.ide.codeStyle;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import com.intellij.lang.Language;
import com.intellij.openapi.options.Configurable;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsProvider;

/**
 * @author VISTALL
 * @since 11.11.14
 */
public class CSharpCodeStyleSettingsProvider extends CodeStyleSettingsProvider
{
	@Nullable
	@Override
	public Language getLanguage()
	{
		return CSharpLanguage.INSTANCE;
	}

	@NotNull
	@Override
	public Configurable createSettingsPage(CodeStyleSettings settings, CodeStyleSettings originalSettings)
	{
		return new CSharpCodeStyleConfigurable(settings, originalSettings);
	}
}
