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

import com.intellij.ide.projectView.SelectableTreeStructureProvider;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.ide.util.treeView.AbstractTreeUi;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import consulo.annotation.access.RequiredReadAction;
import consulo.dotnet.psi.DotNetMemberOwner;
import consulo.ui.annotation.RequiredUIAccess;
import jakarta.inject.Inject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author VISTALL
 * @since 09.12.13.
 */
public class CSharpProjectViewProvider implements SelectableTreeStructureProvider, DumbAware
{
	private final Project myProject;

	@Inject
	public CSharpProjectViewProvider(Project project)
	{
		myProject = project;
	}

	@Nullable
	@Override
	public PsiElement getTopLevelElement(PsiElement element)
	{
		return element.getContainingFile();
	}

	@Override
	@RequiredUIAccess
	public Collection<AbstractTreeNode> modify(AbstractTreeNode parent, Collection<AbstractTreeNode> oldNodes, ViewSettings settings)
	{
		return AbstractTreeUi.calculateYieldingToWriteAction(() -> doModify(oldNodes, settings));
	}

	@Nonnull
	@RequiredReadAction
	private List<AbstractTreeNode> doModify(Collection<AbstractTreeNode> oldNodes, ViewSettings settings)
	{
		List<AbstractTreeNode> nodes = new ArrayList<>(oldNodes.size());
		for(AbstractTreeNode treeNode : oldNodes)
		{
			ProgressManager.checkCanceled();

			Object value = treeNode.getValue();

			if(value instanceof PsiFile)
			{
				for(CSharpProjectTreeNodeExpander expander : CSharpProjectTreeNodeExpander.EP_NAME.getExtensionList(Application.get()))
				{
					AbstractTreeNode<?> node = expander.expandFile(myProject, settings, treeNode);
					if(node != null)
					{
						nodes.add(node);
						break;
					}
				}
			}
			else
			{
				if(value instanceof DotNetMemberOwner)
				{
					nodes.add(new CSharpElementTreeNode((DotNetMemberOwner) value, settings, 0));
				}
				else
				{
					nodes.add(treeNode);
				}
			}
		}
		return nodes;
	}
}
