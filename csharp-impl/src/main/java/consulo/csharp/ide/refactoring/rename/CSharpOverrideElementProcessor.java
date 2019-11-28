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

package consulo.csharp.ide.refactoring.rename;

import gnu.trove.THashSet;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import com.intellij.openapi.ui.MessageDialogBuilder;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.SearchScope;
import com.intellij.refactoring.rename.RenamePsiElementProcessor;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.impl.source.resolve.overrideSystem.OverrideUtil;
import consulo.dotnet.psi.DotNetVirtualImplementOwner;

/**
 * @author VISTALL
 * @since 17.12.14
 */
public class CSharpOverrideElementProcessor extends RenamePsiElementProcessor
{
	private int myLastResult;

	@Override
	@RequiredReadAction
	public void prepareRenaming(PsiElement element, String newName, Map<PsiElement, String> allRenames, SearchScope scope)
	{
		// if name is empty that mean start rename
		if(StringUtil.isEmpty(newName))
		{
			myLastResult = -1;
		}

		if(myLastResult == -1)
		{
			Set<DotNetVirtualImplementOwner> allElements = getAllElements(element);
			if(!allElements.isEmpty())
			{
				MessageDialogBuilder.YesNo builder = MessageDialogBuilder.yesNo("Rename", "Rename all override/implement targets or this target?");
				builder = builder.yesText("All Targets");
				builder = builder.noText("This Target");

				if((myLastResult = builder.show()) == Messages.YES)
				{
					for(DotNetVirtualImplementOwner tempElement : allElements)
					{
						allRenames.put(tempElement, newName);
					}
				}
			}
		}
		else if(myLastResult == Messages.YES)
		{
			Set<DotNetVirtualImplementOwner> allElements = getAllElements(element);
			for(DotNetVirtualImplementOwner tempElement : allElements)
			{
				allRenames.put(tempElement, newName);
			}
		}
	}

	@Nonnull
	@RequiredReadAction
	public static Set<DotNetVirtualImplementOwner> getAllElements(PsiElement element)
	{
		Collection<DotNetVirtualImplementOwner> temp1 = OverrideUtil.collectOverridingMembers((DotNetVirtualImplementOwner) element);
		Collection<DotNetVirtualImplementOwner> temp2 = OverrideUtil.collectOverridenMembers((DotNetVirtualImplementOwner) element);
		Set<DotNetVirtualImplementOwner> set = new THashSet<DotNetVirtualImplementOwner>(temp1.size() + temp1.size());
		set.addAll(temp1);
		set.addAll(temp2);
		return set;
	}

	@Override
	@RequiredReadAction
	public boolean canProcessElement(@Nonnull PsiElement element)
	{
		return OverrideUtil.isAllowForOverride(element);
	}
}
