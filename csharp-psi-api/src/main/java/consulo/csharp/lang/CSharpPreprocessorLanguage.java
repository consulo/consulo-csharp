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

package consulo.csharp.lang;

import consulo.csharp.api.localize.CSharpLocalize;
import consulo.language.Language;
import consulo.language.template.TemplateLanguage;
import consulo.localize.LocalizeValue;
import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 23.01.14
 */
public class CSharpPreprocessorLanguage extends Language implements TemplateLanguage
{
	public static final CSharpPreprocessorLanguage INSTANCE = new CSharpPreprocessorLanguage();

	private CSharpPreprocessorLanguage()
	{
		super("C#Preprocessor");
	}

	@Nonnull
    @Override
	public LocalizeValue getDisplayName()
	{
		return CSharpLocalize.csharpPreprocessorLanguageDisplayName();
	}

	@Override
	public boolean isCaseSensitive()
	{
		return true;
	}
}
