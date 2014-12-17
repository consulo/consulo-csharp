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

package org.mustbe.consulo.csharp.ide.refactoring.rename;

import gnu.trove.THashSet;

import java.util.Collection;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.overrideSystem.OverrideUtil;
import org.mustbe.consulo.dotnet.psi.DotNetVirtualImplementOwner;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.refactoring.rename.RenamePsiElementProcessor;
import com.intellij.refactoring.rename.RenameUtil;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 17.12.14
 */
public class CSharpOverrideElementProcessor extends RenamePsiElementProcessor
{
	private int myResult;

	@Nullable
	@Override
	public PsiElement substituteElementToRename(PsiElement element, @Nullable Editor editor)
	{
		boolean show = !OverrideUtil.collectOverridingMembers((DotNetVirtualImplementOwner) element).isEmpty();
		if(!show)
		{
			show = !OverrideUtil.collectOverridenMembers((DotNetVirtualImplementOwner) element).isEmpty();
		}

		if(show)
		{
			MessageDialogBuilder.YesNo builder = MessageDialogBuilder.yesNo("Rename", "Rename all override/implement methods or this method?");
			builder = builder.yesText("All Methods");
			builder = builder.noText("This Method");

			myResult = builder.show();
		}
		else
		{
			myResult = Messages.NO;
		}
		return element;
	}

	@Override
	public void renameElement(PsiElement element,
			String newName,
			UsageInfo[] usages,
			@Nullable RefactoringElementListener listener) throws IncorrectOperationException
	{
		switch(myResult)
		{
			case Messages.YES:
				Collection<DotNetVirtualImplementOwner> temp1 = OverrideUtil.collectOverridingMembers((DotNetVirtualImplementOwner) element);
				Collection<DotNetVirtualImplementOwner> temp2 = OverrideUtil.collectOverridenMembers((DotNetVirtualImplementOwner) element);
				Set<DotNetVirtualImplementOwner> set = new THashSet<DotNetVirtualImplementOwner>(temp1.size() + temp1.size() + 1);
				set.addAll(temp1);
				set.addAll(temp2);
				set.add((DotNetVirtualImplementOwner) element);

				for(DotNetVirtualImplementOwner owner : set)
				{
					RenameUtil.doRenameGenericNamedElement(owner, newName, usages, listener);
				}
				break;
			case Messages.NO:
				super.renameElement(element, newName, usages, listener);
				break;
		}
	}

	@Override
	public boolean canProcessElement(@NotNull PsiElement element)
	{
		return element instanceof DotNetVirtualImplementOwner && !(element instanceof CSharpMethodDeclaration && ((CSharpMethodDeclaration) element)
				.isDelegate());
	}
}
