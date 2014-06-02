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

package org.mustbe.consulo.csharp.ide.msil.representation.builder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.CSharpFileType;
import com.intellij.codeInsight.daemon.impl.analysis.DefaultHighlightingSettingProvider;
import com.intellij.codeInsight.daemon.impl.analysis.FileHighlightingSetting;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author VISTALL
 * @since 02.06.14
 */
public class MsilRepresentationHighlightingSettingProvider extends DefaultHighlightingSettingProvider
{
	@Nullable
	@Override
	public FileHighlightingSetting getDefaultSetting(@NotNull Project project, @NotNull VirtualFile file)
	{
		if(file.getFileType() == CSharpFileType.INSTANCE && ProjectFileIndex.SERVICE.getInstance(project).isInLibraryClasses(file))
		{
			return FileHighlightingSetting.SKIP_INSPECTION;
		}
		return FileHighlightingSetting.NONE;
	}
}
