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

package consulo.csharp.ide.projectView;

import org.jetbrains.annotations.NotNull;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetAttributeUtil;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.BasePsiNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtilCore;

/**
 * @author VISTALL
 * @since 10.03.14
 */
public abstract class CSharpAbstractElementTreeNode<T extends PsiElement> extends BasePsiNode<T>
{
	protected CSharpAbstractElementTreeNode(Project project, T value, ViewSettings viewSettings)
	{
		super(project, value, viewSettings);
	}

	@Override
	public boolean shouldDrillDownOnEmptyElement()
	{
		return true;
	}

	@Override
	public boolean canRepresent(final Object element)
	{
		return isValid() && (super.canRepresent(element) || canRepresent(getValue(), element));
	}

	@Override
	public boolean expandOnDoubleClick()
	{
		return false;
	}

	@Override
	protected boolean isDeprecated()
	{
		T value = getValue();
		return value != null && value.isValid() && DotNetAttributeUtil.hasAttribute(value, DotNetTypes.System.ObsoleteAttribute);
	}

	@Override
	public boolean contains(@NotNull VirtualFile file)
	{
		T value = getValue();
		return value != null && value.isValid() && PsiUtilCore.getVirtualFile(value) == file;
	}

	private boolean canRepresent(final PsiElement psiElement, final Object element)
	{
		if(psiElement == null || !psiElement.isValid())
		{
			return false;
		}

		final PsiFile parentFile = psiElement.getContainingFile();
		if(parentFile != null && (parentFile == element || parentFile.getVirtualFile() == element))
		{
			return true;
		}

		if(!getSettings().isShowMembers())
		{
			if(element instanceof PsiElement && ((PsiElement) element).isValid())
			{
				PsiFile elementFile = ((PsiElement) element).getContainingFile();
				if(elementFile != null && parentFile != null)
				{
					return elementFile.equals(parentFile);
				}
			}
		}

		return false;
	}
}
