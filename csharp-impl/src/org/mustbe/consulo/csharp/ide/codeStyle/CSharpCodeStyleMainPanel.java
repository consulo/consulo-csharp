package org.mustbe.consulo.csharp.ide.codeStyle;

import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import com.intellij.application.options.TabbedLanguageCodeStylePanel;
import com.intellij.psi.codeStyle.CodeStyleSettings;

/**
 * @author VISTALL
 * @since 11.11.14
 */
public class CSharpCodeStyleMainPanel extends TabbedLanguageCodeStylePanel
{
	public CSharpCodeStyleMainPanel(CodeStyleSettings currentSettings, CodeStyleSettings settings)
	{
		super(CSharpLanguage.INSTANCE, currentSettings, settings);
	}
}
