package org.mustbe.consulo.csharp.ide.codeStyle;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import com.intellij.lang.Language;
import com.intellij.openapi.application.ApplicationBundle;
import com.intellij.openapi.options.Configurable;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsProvider;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;

/**
 * @author VISTALL
 * @since 28.07.2015
 */
public class CSharpCodeGenerationSettingsProvider extends CodeStyleSettingsProvider
{
	@Override
	public boolean hasSettingsPage()
	{
		return false;
	}

	@Override
	public String getConfigurableDisplayName()
	{
		return ApplicationBundle.message("title.code.generation");
	}

	@Nullable
	@Override
	public Language getLanguage()
	{
		return CSharpLanguage.INSTANCE;
	}

	@Nullable
	@Override
	public CustomCodeStyleSettings createCustomSettings(CodeStyleSettings settings)
	{
		return new CSharpCodeGenerationSettings(settings);
	}

	@NotNull
	@Override
	public Configurable createSettingsPage(CodeStyleSettings settings, CodeStyleSettings originalSettings)
	{
		return new CSharpCodeGenerationSettingsConfigurable(settings);
	}
}
