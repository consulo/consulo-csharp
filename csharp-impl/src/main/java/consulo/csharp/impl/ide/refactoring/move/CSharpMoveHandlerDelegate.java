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

package consulo.csharp.impl.ide.refactoring.move;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.psi.CSharpFile;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dataContext.DataContext;
import consulo.language.editor.refactoring.move.MoveCallback;
import consulo.language.editor.refactoring.move.MoveHandlerDelegate;
import consulo.language.psi.PsiDirectory;
import consulo.language.psi.PsiElement;
import consulo.project.Project;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 26.07.2015
 */
@ExtensionImpl
public class CSharpMoveHandlerDelegate extends MoveHandlerDelegate
{
	@Override
	public boolean isValidTarget(PsiElement psiElement, PsiElement[] sources)
	{
		if(!(psiElement instanceof PsiDirectory))
		{
			return false;
		}
		for(PsiElement source : sources)
		{
			if(source instanceof CSharpTypeDeclaration)
			{
				return true;
			}

			if(source instanceof CSharpFile)
			{
				return true;
			}
		}
		return false;
	}

	@Override
	@RequiredReadAction
	public boolean canMove(PsiElement[] elements, @Nullable PsiElement targetContainer)
	{
		for(PsiElement psiElement : elements)
		{
			if(psiElement.getLanguage() != CSharpLanguage.INSTANCE)
			{
				return false;
			}
		}
		return super.canMove(elements, targetContainer);
	}

	@Override
	@RequiredReadAction
	public boolean canMove(DataContext dataContext)
	{
		PsiElement[] elements = dataContext.getData(PsiElement.KEY_OF_ARRAY);
		return elements != null && canMove(elements, null);
	}

	@Override
	public void doMove(Project project, PsiElement[] elements, @Nullable PsiElement targetContainer, @Nullable MoveCallback callback)
	{
		CSharpMoveClassesUtil.doMove(project, adjustForMove(project, elements, targetContainer), new PsiElement[]{targetContainer}, callback);
	}
}
