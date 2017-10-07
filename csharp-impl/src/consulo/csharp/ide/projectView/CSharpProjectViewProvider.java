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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import com.intellij.ide.projectView.SelectableTreeStructureProvider;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import consulo.annotations.RequiredDispatchThread;
import consulo.csharp.ide.codeInsight.problems.CSharpLocationUtil;
import consulo.csharp.lang.psi.CSharpFile;
import consulo.csharp.lang.psi.impl.source.CSharpDummyDeclarationImpl;
import consulo.csharp.lang.psi.impl.source.CSharpPsiUtilImpl;
import consulo.dotnet.psi.DotNetMemberOwner;
import consulo.dotnet.psi.DotNetNamedElement;

/**
 * @author VISTALL
 * @since 09.12.13.
 */
public class CSharpProjectViewProvider implements SelectableTreeStructureProvider
{
	private final Project myProject;

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
	@RequiredDispatchThread
	public Collection<AbstractTreeNode> modify(AbstractTreeNode abstractTreeNode, Collection<AbstractTreeNode> oldNodes, ViewSettings settings)
	{
		List<AbstractTreeNode> nodes = new ArrayList<>(oldNodes.size());
		for(AbstractTreeNode treeNode : oldNodes)
		{
			Object value = treeNode.getValue();

			if(value instanceof PsiFile)
			{
				CSharpFile file = CSharpPsiUtilImpl.findCSharpFile((PsiFile) value);
				if(file != null)
				{
					if(CSharpLocationUtil.isValidLocation(myProject, ((PsiFile) value).getVirtualFile()))
					{
						DotNetNamedElement singleElement = CSharpPsiUtilImpl.findSingleElementNoNameCheck(file);
						if(singleElement instanceof CSharpDummyDeclarationImpl)
						{
							nodes.add(new CSharpElementTreeNode(file, settings, 0));
						}
						else if(singleElement != null)
						{
							nodes.add(new CSharpElementTreeNode(singleElement, settings, CSharpElementTreeNode.ALLOW_GRAY_FILE_NAME));
						}
						else
						{
							nodes.add(new CSharpElementTreeNode(file, settings, CSharpElementTreeNode.FORCE_EXPAND));
						}
					}
					else
					{
						nodes.add(treeNode);
					}
				}
				else
				{
					nodes.add(treeNode);
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

	@Nullable
	@Override
	public Object getData(Collection<AbstractTreeNode> abstractTreeNodes, String s)
	{
		return null;
	}
}
