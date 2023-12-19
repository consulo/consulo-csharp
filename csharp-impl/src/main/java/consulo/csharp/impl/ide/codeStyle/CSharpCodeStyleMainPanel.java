/*
 * Copyright 2013-2017 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package consulo.csharp.impl.ide.codeStyle;

import consulo.csharp.lang.CSharpLanguage;
import consulo.language.codeStyle.CodeStyleSettings;
import consulo.language.codeStyle.setting.CodeStyleSettingsProvider;
import consulo.language.codeStyle.ui.setting.TabbedLanguageCodeStylePanel;

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
		for(CodeStyleSettingsProvider provider : CodeStyleSettingsProvider.EXTENSION_POINT_NAME.getExtensionList())
		{
			if(provider.getLanguage() == CSharpLanguage.INSTANCE && !provider.hasSettingsPage())
			{
				createTab(provider);
			}
		}
	}
}
