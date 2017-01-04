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

package consulo.csharp.ide.refactoring.move;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Nullable;
import consulo.csharp.lang.psi.CSharpFile;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.psi.impl.source.CSharpPsiUtilImpl;
import consulo.dotnet.psi.DotNetNamedElement;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.move.MoveCallback;
import com.intellij.refactoring.move.MoveHandlerDelegate;
import com.intellij.refactoring.move.moveFilesOrDirectories.MoveFilesOrDirectoriesUtil;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 26.07.2015
 */
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
				CSharpFile containingFile = (CSharpFile) source.getContainingFile();
				if(containingFile == null)
				{
					continue;
				}
				DotNetNamedElement singleElement = CSharpPsiUtilImpl.findSingleElement(containingFile);
				if(singleElement == source)
				{
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean canMove(DataContext dataContext)
	{
		PsiElement psiElement = CommonDataKeys.PSI_ELEMENT.getData(dataContext);
		return canMove(new PsiElement[] {psiElement}, null);
	}

	@Override
	public void collectFilesOrDirsFromContext(DataContext dataContext, Set<PsiElement> filesOrDirs)
	{
		PsiElement psiElement = CommonDataKeys.PSI_ELEMENT.getData(dataContext);
		if(psiElement instanceof CSharpTypeDeclaration)
		{
			CSharpFile containingFile = (CSharpFile) psiElement.getContainingFile();
			DotNetNamedElement singleElement = CSharpPsiUtilImpl.findSingleElement(containingFile);
			if(singleElement == psiElement)
			{
				filesOrDirs.add(containingFile);
			}
		}
	}

	@Override
	public void doMove(Project project, PsiElement[] elements, @Nullable PsiElement targetContainer, @Nullable MoveCallback callback)
	{
		MoveFilesOrDirectoriesUtil.doMove(project, adjustForMove(project, elements, targetContainer), new PsiElement[] {targetContainer}, callback);
	}

	@Nullable
	@Override
	public PsiElement[] adjustForMove(Project project, PsiElement[] sourceElements, PsiElement targetElement)
	{
		List<PsiElement> elements = new ArrayList<PsiElement>(sourceElements.length);
		for(PsiElement sourceElement : sourceElements)
		{
			if(sourceElement instanceof CSharpTypeDeclaration)
			{
				elements.add(sourceElement.getContainingFile());
			}
			else
			{
				elements.add(sourceElement);
			}
		}
		return ContainerUtil.toArray(elements, PsiElement.ARRAY_FACTORY);
	}
}
