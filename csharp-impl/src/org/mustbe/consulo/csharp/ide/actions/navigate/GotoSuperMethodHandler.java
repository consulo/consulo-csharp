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

package org.mustbe.consulo.csharp.ide.actions.navigate;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.CSharpFileType;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.overrideSystem.OverrideUtil;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.psi.DotNetVirtualImplementOwner;
import com.intellij.lang.LanguageCodeInsightActionHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 16.12.14
 */
public class GotoSuperMethodHandler implements LanguageCodeInsightActionHandler
{
	@Nullable
	public static DotNetVirtualImplementOwner findVirtualImplementOwner(@NotNull Editor editor, @NotNull PsiFile file)
	{
		if(file.getFileType() != CSharpFileType.INSTANCE)
		{
			return null;
		}
		final int offset = editor.getCaretModel().getOffset();
		final PsiElement elementAt = file.findElementAt(offset);
		if(elementAt == null)
		{
			return null;
		}
		return PsiTreeUtil.getParentOfType(elementAt, DotNetVirtualImplementOwner.class);
	}

	@Override
	public boolean isValidFor(Editor editor, PsiFile file)
	{
		return findVirtualImplementOwner(editor, file) != null;
	}

	@Override
	public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file)
	{
		DotNetVirtualImplementOwner virtualImplementOwner = findVirtualImplementOwner(editor, file);
		if(virtualImplementOwner == null)
		{
			return;
		}

		Collection<DotNetVirtualImplementOwner> collection = OverrideUtil.collectOverridingMembers(virtualImplementOwner);

		for(DotNetVirtualImplementOwner owner : collection)
		{
			if(!(owner instanceof DotNetModifierListOwner && ((DotNetModifierListOwner) owner).hasModifier(DotNetModifier.ABSTRACT)))
			{
				((Navigatable)owner).navigate(true);
				return;
			}
		}
		DotNetVirtualImplementOwner firstItem = ContainerUtil.getFirstItem(collection);
		if(firstItem instanceof Navigatable)
		{
			((Navigatable) firstItem).navigate(true);
		}
	}

	@Override
	public boolean startInWriteAction()
	{
		return false;
	}
}
