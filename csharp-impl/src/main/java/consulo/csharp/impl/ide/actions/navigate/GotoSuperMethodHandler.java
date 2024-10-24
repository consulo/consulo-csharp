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

package consulo.csharp.impl.ide.actions.navigate;

import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.csharp.lang.CSharpFileType;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.impl.psi.source.resolve.overrideSystem.OverrideUtil;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.psi.DotNetVirtualImplementOwner;
import consulo.language.Language;
import consulo.language.editor.action.GotoSuperActionHander;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.navigation.Navigatable;
import consulo.project.Project;
import consulo.util.collection.ContainerUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Collection;

/**
 * @author VISTALL
 * @since 16.12.14
 */
@ExtensionImpl
public class GotoSuperMethodHandler implements GotoSuperActionHander
{
	@Nullable
	public static DotNetVirtualImplementOwner findVirtualImplementOwner(@Nonnull Editor editor, @Nonnull PsiFile file)
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
	public void invoke(@Nonnull Project project, @Nonnull Editor editor, @Nonnull PsiFile file)
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

	@Nonnull
	@Override
	public Language getLanguage()
	{
		return CSharpLanguage.INSTANCE;
	}
}
