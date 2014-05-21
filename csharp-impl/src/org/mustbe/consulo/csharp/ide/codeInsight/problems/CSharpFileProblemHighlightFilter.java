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

package org.mustbe.consulo.csharp.ide.codeInsight.problems;

import org.mustbe.consulo.dotnet.module.extension.DotNetModuleExtension;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleFileIndex;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author VISTALL
 * @since 17.01.14
 */
public class CSharpFileProblemHighlightFilter implements Condition<VirtualFile>
{
	private final Project myProject;

	public CSharpFileProblemHighlightFilter(Project project)
	{
		myProject = project;
	}

	@Override
	public boolean value(VirtualFile virtualFile)
	{
		Module moduleForFile = ModuleUtilCore.findModuleForFile(virtualFile, myProject);
		if(moduleForFile == null)
		{
			return false;
		}
		DotNetModuleExtension extension = ModuleUtilCore.getExtension(moduleForFile, DotNetModuleExtension.class);
		if(extension == null)
		{
			return false;
		}

		if(!extension.isAllowSourceRoots())
		{
			return true;
		}
		else
		{
			ModuleFileIndex fileIndex = ModuleRootManager.getInstance(moduleForFile).getFileIndex();
			return fileIndex.isInSourceContent(virtualFile) || fileIndex.isInTestSourceContent(virtualFile);
		}
	}
}
