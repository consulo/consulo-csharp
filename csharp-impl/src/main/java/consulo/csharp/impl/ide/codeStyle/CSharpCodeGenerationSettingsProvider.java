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

import consulo.annotation.component.ExtensionImpl;
import consulo.application.localize.ApplicationLocalize;
import consulo.configurable.Configurable;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.impl.ide.codeStyle.CSharpCodeGenerationSettings;
import consulo.language.Language;
import consulo.language.codeStyle.CodeStyleSettings;
import consulo.language.codeStyle.CustomCodeStyleSettings;
import consulo.language.codeStyle.setting.CodeStyleSettingsProvider;
import consulo.localize.LocalizeValue;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 28.07.2015
 */
@ExtensionImpl
public class CSharpCodeGenerationSettingsProvider extends CodeStyleSettingsProvider
{
	@Override
	public boolean hasSettingsPage()
	{
		return false;
	}

	@Nonnull
    @Override
	public LocalizeValue getConfigurableDisplayName()
	{
		return ApplicationLocalize.titleCodeGeneration();
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

	@Nonnull
	@Override
	public Configurable createSettingsPage(CodeStyleSettings settings, CodeStyleSettings originalSettings)
	{
		return new CSharpCodeGenerationSettingsConfigurable(settings);
	}
}
