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

package consulo.csharp.ide.codeStyle;

import javax.annotation.Nonnull;

import consulo.language.codeStyle.CodeStyleSettings;
import consulo.language.codeStyle.ui.setting.CodeStyleAbstractConfigurable;
import consulo.language.codeStyle.ui.setting.CodeStyleAbstractPanel;

/**
 * @author VISTALL
 * @since 11.11.14
 */
public class CSharpCodeStyleConfigurable extends CodeStyleAbstractConfigurable
{
	public CSharpCodeStyleConfigurable(@Nonnull CodeStyleSettings settings, CodeStyleSettings cloneSettings)
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
