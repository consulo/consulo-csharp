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

package consulo.csharp.module.extension;

import consulo.csharp.api.localize.CSharpLocalize;
import consulo.localize.LocalizeValue;
import consulo.util.pointers.Named;
import consulo.util.pointers.NamedPointer;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 15.05.14
 */
public enum CSharpLanguageVersion implements Named, NamedPointer<CSharpLanguageVersion>
{
	_1_0(CSharpLocalize.csharpVersion_1_0()),
	_2_0(CSharpLocalize.csharpVersion_2_0()),
	_3_0(CSharpLocalize.csharpVersion_3_0()),
	_4_0(CSharpLocalize.csharpVersion_4_0()),
	_5_0(CSharpLocalize.csharpVersion_5_0()),
	_6_0(CSharpLocalize.csharpVersion_6_0()),
	_7_0(CSharpLocalize.csharpVersion_7_0()),
	_7_1(CSharpLocalize.csharpVersion_7_1()),
	_7_2(CSharpLocalize.csharpVersion_7_2()),
	_7_3(CSharpLocalize.csharpVersion_7_3()),
	_8_0(CSharpLocalize.csharpVersion_8_0()),
	_9_0(CSharpLocalize.csharpVersion_9_0()),
	_10_0(CSharpLocalize.csharpVersion_10_0());

	public static final CSharpLanguageVersion HIGHEST = _10_0;

	private final LocalizeValue myDescriptionValue;

	CSharpLanguageVersion(@Nonnull LocalizeValue descriptionValue)
	{
		myDescriptionValue = descriptionValue;
	}

	public boolean isAtLeast(@Nonnull CSharpLanguageVersion languageVersion)
	{
		return ordinal() >= languageVersion.ordinal();
	}

	@Nonnull
	public String getPresentableName()
	{
		String name = name();
		name = name.substring(1, name.length());
		name = name.replace("_", ".");
		return name;
	}

	@Nonnull
	public LocalizeValue getDescriptionValue()
	{
		return myDescriptionValue;
	}

	@Nonnull
	@Override
	public CSharpLanguageVersion get()
	{
		return this;
	}

	@Nonnull
	@Override
	public String getName()
	{
		return name();
	}
}
