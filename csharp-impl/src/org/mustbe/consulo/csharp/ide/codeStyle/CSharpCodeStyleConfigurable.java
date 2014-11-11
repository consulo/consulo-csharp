package org.mustbe.consulo.csharp.ide.codeStyle;

import org.jetbrains.annotations.NotNull;
import com.intellij.application.options.CodeStyleAbstractConfigurable;
import com.intellij.application.options.CodeStyleAbstractPanel;
import com.intellij.psi.codeStyle.CodeStyleSettings;

/**
 * @author VISTALL
 * @since 11.11.14
 */
public class CSharpCodeStyleConfigurable extends CodeStyleAbstractConfigurable
{
	public CSharpCodeStyleConfigurable(@NotNull CodeStyleSettings settings, CodeStyleSettings cloneSettings)
	{
		super(settings, cloneSettings, "C#");
	}

	@Override
	protected CodeStyleAbstractPanel createPanel(CodeStyleSettings settings)
	{
		return new CSharpCodeStyleMainPanel(getCurrentSettings(), settings);
	}

	@Override
	public String getHelpTopic()
	{
		return "reference.settingsdialog.codestyle.csharp";
	}
}
