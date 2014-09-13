package org.mustbe.consulo.csharp.ide.codeStyle;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import com.intellij.application.options.CodeStyleAbstractConfigurable;
import com.intellij.application.options.CodeStyleAbstractPanel;
import com.intellij.lang.Language;
import com.intellij.openapi.options.Configurable;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsProvider;

/**
 * @author VISTALL
 * @since 13.09.14
 */
public class CSharpCodeStyleSettingsProvider extends CodeStyleSettingsProvider
{
	@NotNull
	@Override
	public Configurable createSettingsPage(CodeStyleSettings codeStyleSettings, CodeStyleSettings codeStyleSettings2)
	{
		return new CodeStyleAbstractConfigurable(codeStyleSettings, codeStyleSettings, "C#")
		{
			@Override
			protected CodeStyleAbstractPanel createPanel(CodeStyleSettings settings)
			{
				return new CSharpCodeStylePanel(settings);
			}

			@Nullable
			@Override
			public String getHelpTopic()
			{
				return null;
			}
		};
	}

	@Nullable
	@Override
	public Language getLanguage()
	{
		return CSharpLanguage.INSTANCE;
	}
}
