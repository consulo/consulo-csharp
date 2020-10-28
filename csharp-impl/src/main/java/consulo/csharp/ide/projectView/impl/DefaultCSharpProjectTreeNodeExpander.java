/*
 * Copyright 2013-2020 consulo.io
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

package consulo.csharp.ide.projectView.impl;

import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.ide.codeInsight.problems.CSharpLocationUtil;
import consulo.csharp.ide.projectView.CSharpElementTreeNode;
import consulo.csharp.ide.projectView.CSharpProjectTreeNodeExpander;
import consulo.csharp.lang.psi.CSharpFile;
import consulo.csharp.lang.psi.impl.source.CSharpDummyDeclarationImpl;
import consulo.csharp.lang.psi.impl.source.CSharpPsiUtilImpl;
import consulo.dotnet.psi.DotNetNamedElement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2020-10-28
 */
public class DefaultCSharpProjectTreeNodeExpander implements CSharpProjectTreeNodeExpander
{
	@RequiredReadAction
	@Nullable
	@Override
	public AbstractTreeNode<?> expandFile(@Nonnull Project project, @Nonnull ViewSettings settings, @Nonnull AbstractTreeNode<?> treeNode)
	{
		Object value = treeNode.getValue();

		if(value instanceof PsiFile)
		{
			CSharpFile file = CSharpPsiUtilImpl.findCSharpFile((PsiFile) value);
			if(file != null)
			{
				if(CSharpLocationUtil.isValidLocation(project, ((PsiFile) value).getVirtualFile()))
				{
					DotNetNamedElement singleElement = CSharpPsiUtilImpl.findSingleElementNoNameCheck(file);
					if(singleElement instanceof CSharpDummyDeclarationImpl)
					{
						return new CSharpElementTreeNode(file, settings, 0);
					}
					else if(singleElement != null)
					{
						return new CSharpElementTreeNode(singleElement, settings, CSharpElementTreeNode.ALLOW_GRAY_FILE_NAME);
					}
					else
					{
						return new CSharpElementTreeNode(file, settings, CSharpElementTreeNode.FORCE_EXPAND);
					}
				}
			}
		}

		return treeNode;
	}
}
