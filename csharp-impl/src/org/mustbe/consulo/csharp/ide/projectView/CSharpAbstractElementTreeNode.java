package org.mustbe.consulo.csharp.ide.projectView;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetAttributeUtil;
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
		return DotNetAttributeUtil.hasAttribute(value, DotNetTypes.System.ObsoleteAttribute);
	}

	@Override
	public boolean contains(@NotNull VirtualFile file)
	{
		return PsiUtilCore.getVirtualFile(getValue()) == file;
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
