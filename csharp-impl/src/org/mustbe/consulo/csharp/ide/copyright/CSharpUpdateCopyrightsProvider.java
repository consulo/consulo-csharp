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

package org.mustbe.consulo.csharp.ide.copyright;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.maddyhome.idea.copyright.CopyrightProfile;
import com.maddyhome.idea.copyright.psi.UpdateCopyright;
import com.maddyhome.idea.copyright.psi.UpdateCopyrightsProvider;
import com.maddyhome.idea.copyright.psi.UpdatePsiFileCopyright;

/**
 * @author VISTALL
 * @since 15.03.14
 */
public class CSharpUpdateCopyrightsProvider extends UpdateCopyrightsProvider
{
	@Override
	public UpdateCopyright createInstance(Project project, Module module, VirtualFile virtualFile, FileType fileType,
			CopyrightProfile copyrightProfile)
	{
		return new UpdatePsiFileCopyright(project, module, virtualFile, copyrightProfile)
		{
			@Override
			protected void scanFile()
			{
				PsiFile file = getFile();

				checkComments(file.getFirstChild(), null, true);
			}
		};
	}
}
