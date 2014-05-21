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

import org.consulo.lombok.annotations.ApplicationService;
import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import com.intellij.lang.LanguageVersion;

/**
 * @author VISTALL
 * @since 14.12.13.
 */
@ApplicationService
public abstract class CSharpLanguageVersionHelper
{
	@NotNull
	public abstract LanguageVersion<CSharpLanguage> getHighestVersion();

	@NotNull
	public abstract LanguageVersion<CSharpLanguage>[] getVersions();

	@NotNull
	public abstract LanguageVersion<CSharpLanguage> getWrapper(@NotNull CSharpLanguageVersion version);
}
