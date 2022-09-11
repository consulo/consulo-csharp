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

package consulo.csharp.impl.ide.projectView;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.Application;
import consulo.application.dumb.DumbAware;
import consulo.application.progress.ProgressManager;
import consulo.dotnet.psi.DotNetMemberOwner;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.project.Project;
import consulo.project.ui.view.tree.AbstractTreeNode;
import consulo.project.ui.view.tree.SelectableTreeStructureProvider;
import consulo.project.ui.view.tree.ViewSettings;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.tree.TreeHelper;
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
@ExtensionImpl
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
		return TreeHelper.calculateYieldingToWriteAction(() -> doModify(oldNodes, settings));
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
				for(CSharpProjectTreeNodeExpander expander : Application.get().getExtensionPoint(CSharpProjectTreeNodeExpander.class))
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
