package org.mustbe.consulo.csharp.ide.codeStyle;

import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import com.intellij.application.options.TabbedLanguageCodeStylePanel;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsProvider;

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

	@Override
	protected void initTabs(CodeStyleSettings settings)
	{
		super.initTabs(settings);
		for(CodeStyleSettingsProvider provider : Extensions.getExtensions(CodeStyleSettingsProvider.EXTENSION_POINT_NAME))
		{
			if(provider.getLanguage() == CSharpLanguage.INSTANCE && !provider.hasSettingsPage())
			{
				createTab(provider);
			}

		}
	}
}
