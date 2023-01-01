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

import consulo.csharp.lang.psi.icon.CSharpPsiIconGroup;
import consulo.language.file.LanguageFileType;
import consulo.language.template.TemplateLanguageFileType;
import consulo.localize.LocalizeValue;
import consulo.ui.image.Image;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 22.11.13.
 */
public class CSharpFileType extends LanguageFileType implements TemplateLanguageFileType
{
	public static final String EXTENSION = "cs";
	public static final String DOT_EXTENSION = "." + EXTENSION;

	public static final CSharpFileType INSTANCE = new CSharpFileType();

	private CSharpFileType()
	{
		super(CSharpLanguage.INSTANCE);
	}

	@Nonnull
	@Override
	public String getId()
	{
		return "C#";
	}

	@Nonnull
	@Override
	public LocalizeValue getDescription()
	{
		return LocalizeValue.localizeTODO("C# files");
	}

	@Nonnull
	@Override
	public String getDefaultExtension()
	{
		return EXTENSION;
	}

	@Nonnull
	@Override
	public Image getIcon()
	{
		return CSharpPsiIconGroup.csharp();
	}
}
