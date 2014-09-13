package org.mustbe.consulo.csharp.ide.codeStyle;

import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import com.intellij.application.options.TabbedLanguageCodeStylePanel;
import com.intellij.psi.codeStyle.CodeStyleSettings;

/**
 * @author VISTALL
 * @since 13.09.14
 */
public class CSharpTabbedLanguageCodeStylePanel extends TabbedLanguageCodeStylePanel
{
	public CSharpTabbedLanguageCodeStylePanel(CodeStyleSettings currentSettings, CodeStyleSettings settings)
	{
		super(CSharpLanguage.INSTANCE, currentSettings, settings);
	}
}
