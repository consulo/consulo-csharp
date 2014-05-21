/*
 * Copyright 2013-2014 must-be.org
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

package org.mustbe.consulo.csharp.lang;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.CSharpIcons;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.fileTypes.TemplateLanguageFileType;

/**
 * @author VISTALL
 * @since 22.11.13.
 */
public class CSharpFileType extends LanguageFileType implements TemplateLanguageFileType
{
	public static final CSharpFileType INSTANCE = new CSharpFileType();

	private CSharpFileType()
	{
		super(CSharpLanguage.INSTANCE);
	}

	@NotNull
	@Override
	public String getName()
	{
		return "C#";
	}

	@NotNull
	@Override
	public String getDescription()
	{
		return "C# files";
	}

	@NotNull
	@Override
	public String getDefaultExtension()
	{
		return "cs";
	}

	@Nullable
	@Override
	public Icon getIcon()
	{
		return CSharpIcons.FileType;
	}
}
