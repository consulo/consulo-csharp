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

package consulo.csharp.ide.refactoring.rename;

import consulo.csharp.lang.psi.impl.source.CSharpFileImpl;
import consulo.msil.representation.MsilFileRepresentationVirtualFile;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 15.05.14
 */
public class CSharpVetoRenameCondition implements Condition<PsiElement>
{
	@Override
	public boolean value(PsiElement psiElement)
	{
		if(psiElement instanceof CSharpFileImpl)
		{
			VirtualFile virtualFile = ((CSharpFileImpl) psiElement).getVirtualFile();
			return virtualFile instanceof MsilFileRepresentationVirtualFile;
		}
		return false;
	}
}
