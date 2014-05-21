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

package org.mustbe.consulo.csharp.ide.projectView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpFileImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpPsiUtilImpl;
import org.mustbe.consulo.dotnet.module.extension.DotNetModuleExtension;
import org.mustbe.consulo.dotnet.psi.DotNetMemberOwner;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import com.intellij.ide.projectView.SelectableTreeStructureProvider;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

/**
 * @author VISTALL
 * @since 09.12.13.
 */
public class CSharpProjectViewProvider implements SelectableTreeStructureProvider, DumbAware
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
	public Collection<AbstractTreeNode> modify(AbstractTreeNode abstractTreeNode, Collection<AbstractTreeNode> abstractTreeNodes, ViewSettings
			settings)
	{
		if(DumbService.isDumb(myProject))
		{
			return abstractTreeNodes;
		}
		List<AbstractTreeNode> nodes = new ArrayList<AbstractTreeNode>(abstractTreeNodes.size());
		for(AbstractTreeNode treeNode : abstractTreeNodes)
		{
			Object value = treeNode.getValue();

			if(value instanceof PsiFile)
			{
				CSharpFileImpl cSharpFile = CSharpPsiUtilImpl.findCSharpFile((PsiFile) value);
				if(cSharpFile != null)
				{
					DotNetNamedElement singleElement = CSharpPsiUtilImpl.findSingleElement(cSharpFile);
					if(singleElement != null)
					{
						DotNetModuleExtension extension = ModuleUtilCore.getExtension(singleElement,
								DotNetModuleExtension.class);
						if(extension != null && extension.isAllowSourceRoots())
						{
							nodes.add(new CSharpElementTreeNode(singleElement, settings));
						}
						else
						{
							nodes.add(new CSharpQElementTreeNode(singleElement, settings));
						}
					}
					else
					{
						nodes.add(new CSharpElementTreeNode(cSharpFile, settings));
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
					nodes.add(new CSharpElementTreeNode((DotNetMemberOwner) value, settings));
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
