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
import org.jetbrains.annotations.Nullable;
import consulo.csharp.compiler.CSharpPlatform;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.vfs.VirtualFile;
import consulo.dotnet.compiler.DotNetCompilerOptionsBuilder;
import consulo.module.extension.ModuleExtension;
import consulo.module.extension.ModuleInheritableNamedPointer;

/**
 * @author VISTALL
 * @since 15.05.14
 */
public interface CSharpModuleExtension<T extends ModuleExtension<T>> extends CSharpSimpleModuleExtension<T>
{
	String INTERNAL_SDK_KEY = "<internal>";

	boolean isOptimizeCode();

	@NotNull
	CSharpPlatform getPlatform();

	@Nullable
	String getCompilerTarget();

	@NotNull
	ModuleInheritableNamedPointer<Sdk> getCustomCompilerSdkPointer();

	void setCompilerExecutable(@NotNull DotNetCompilerOptionsBuilder builder, @NotNull VirtualFile executable);
}
