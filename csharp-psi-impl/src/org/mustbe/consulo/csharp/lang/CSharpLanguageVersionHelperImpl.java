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

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import com.intellij.lang.LanguageVersion;

/**
 * @author VISTALL
 * @since 14.12.13.
 */
public class CSharpLanguageVersionHelperImpl extends CSharpLanguageVersionHelper
{
	private final CSharpLanguageVersionWrapper[] myWrappers;
	private CSharpLanguageVersionWrapper myHighest;

	public CSharpLanguageVersionHelperImpl()
	{
		CSharpLanguageVersion[] values = CSharpLanguageVersion.values();

		myWrappers = new CSharpLanguageVersionWrapper[values.length];
		for(int i = 0; i < myWrappers.length; i++)
		{
			myWrappers[i] = new CSharpLanguageVersionWrapper(values[i]);
			if(values[i] == CSharpLanguageVersion.HIGHEST)
			{
				myHighest = myWrappers[i];
			}
		}
		assert myHighest != null;
	}

	@NotNull
	@Override
	public LanguageVersion<CSharpLanguage> getHighestVersion()
	{
		return myHighest;
	}

	@NotNull
	@Override
	public LanguageVersion<CSharpLanguage>[] getVersions()
	{
		return myWrappers;
	}

	@NotNull
	@Override
	public LanguageVersion<CSharpLanguage> getWrapper(@NotNull CSharpLanguageVersion version)
	{
		return myWrappers[version.ordinal()];
	}
}
