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

package consulo.csharp.ide.refactoring.rename;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpFileImpl;
import consulo.language.editor.refactoring.rename.VetoRenameCondition;
import consulo.language.psi.PsiElement;
import consulo.msil.impl.representation.fileSystem.MsilFileRepresentationVirtualFile;
import consulo.virtualFileSystem.VirtualFile;

/**
 * @author VISTALL
 * @since 15.05.14
 */
@ExtensionImpl
public class CSharpVetoRenameCondition implements VetoRenameCondition
{
	@RequiredReadAction
	@Override
	public boolean isVetoed(PsiElement psiElement)
	{
		if(psiElement instanceof CSharpFileImpl)
		{
			VirtualFile virtualFile = ((CSharpFileImpl) psiElement).getVirtualFile();
			return virtualFile instanceof MsilFileRepresentationVirtualFile;
		}
		return false;
	}
}
