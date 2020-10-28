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

package consulo.csharp.ide.projectView;

import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.project.Project;
import consulo.annotation.access.RequiredReadAction;
import consulo.extensions.StrictExtensionPointName;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2020-10-28
 */
public interface CSharpProjectTreeNodeExpander
{
	StrictExtensionPointName<Application, CSharpProjectTreeNodeExpander> EP_NAME = StrictExtensionPointName.forApplication("consulo.csharp.projectTreeNodeExpander");

	@Nullable
	@RequiredReadAction
	AbstractTreeNode<?> expandFile(@Nonnull Project project, @Nonnull ViewSettings viewSettings, @Nonnull AbstractTreeNode<?> originalNode);
}
