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

import org.jetbrains.annotations.NotNull;
import consulo.module.extension.ModuleExtension;

/**
 * @author VISTALL
 * @since 07.06.2015
 */
public interface CSharpSimpleModuleExtension<T extends ModuleExtension<T>> extends ModuleExtension<T>
{
	boolean isAllowUnsafeCode();

	@NotNull
	CSharpLanguageVersion getLanguageVersion();

	boolean isSupportedLanguageVersion(@NotNull CSharpLanguageVersion languageVersion);
}