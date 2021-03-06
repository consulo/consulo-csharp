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

import javax.annotation.Nonnull;

import com.intellij.openapi.components.ServiceManager;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.lang.LanguageVersion;

/**
 * @author VISTALL
 * @since 14.12.13.
 */
public abstract class CSharpLanguageVersionHelper
{
	@Nonnull
	public static CSharpLanguageVersionHelper getInstance()
	{
		return ServiceManager.getService(CSharpLanguageVersionHelper.class);
	}

	@Nonnull
	public abstract LanguageVersion getHighestVersion();

	@Nonnull
	public abstract LanguageVersion[] getVersions();

	@Nonnull
	public abstract LanguageVersion getWrapper(@Nonnull CSharpLanguageVersion version);
}
