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

package consulo.csharp.ide.codeInsight.problems;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.wolfAnalyzer.WolfFileProblemFilter;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.inject.Inject;

/**
 * @author VISTALL
 * @since 17.01.14
 */
@ExtensionImpl
public class CSharpFileProblemHighlightFilter implements WolfFileProblemFilter
{
	private final Project myProject;

	@Inject
	public CSharpFileProblemHighlightFilter(Project project)
	{
		myProject = project;
	}

	@Override
	public boolean isToBeHighlighted(VirtualFile virtualFile)
	{
		return CSharpLocationUtil.isValidLocation(myProject, virtualFile);
	}
}
