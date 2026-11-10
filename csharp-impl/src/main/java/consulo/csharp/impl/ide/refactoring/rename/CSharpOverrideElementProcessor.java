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

package consulo.csharp.impl.ide.refactoring.rename;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.content.scope.SearchScope;
import consulo.csharp.lang.impl.psi.source.resolve.overrideSystem.OverrideUtil;
import consulo.dotnet.psi.DotNetVirtualImplementOwner;
import consulo.language.editor.refactoring.rename.RenamePsiElementProcessor;
import consulo.language.psi.PsiElement;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.ui.MessageBoxBuilder;
import consulo.ui.MessageButtonRole;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.util.lang.StringUtil;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * @author VISTALL
 * @since 17.12.14
 */
@ExtensionImpl
public class CSharpOverrideElementProcessor extends RenamePsiElementProcessor
{
	@Nullable
	private Boolean myRenameAll;

	@Override
	@RequiredReadAction
	public void prepareRenaming(PsiElement element, String newName, Map<PsiElement, String> allRenames, SearchScope scope)
	{
		// if name is empty that mean start rename
		if(StringUtil.isEmpty(newName))
		{
			myRenameAll = null;
		}

		if(myRenameAll == null)
		{
			Set<DotNetVirtualImplementOwner> allElements = getAllElements(element);
			if(!allElements.isEmpty())
			{
				myRenameAll = askRenameAll(element).getNow(Boolean.FALSE);
				if(myRenameAll)
				{
					putAll(allElements, newName, allRenames);
				}
			}
		}
		else if(myRenameAll)
		{
			putAll(getAllElements(element), newName, allRenames);
		}
	}

	@Override
	@RequiredUIAccess
	public CompletableFuture<?> prepareRenamingAsync(PsiElement element, String newName, Map<PsiElement, String> allRenames)
	{
		if(StringUtil.isEmpty(newName))
		{
			myRenameAll = null;
		}

		Set<DotNetVirtualImplementOwner> allElements = getAllElements(element);
		if(myRenameAll != null || allElements.isEmpty())
		{
			if(Boolean.TRUE.equals(myRenameAll))
			{
				putAll(allElements, newName, allRenames);
			}
			return CompletableFuture.completedFuture(null);
		}

		return askRenameAll(element).thenAccept(renameAll ->
		{
			myRenameAll = renameAll;
			if(renameAll)
			{
				putAll(allElements, newName, allRenames);
			}
		});
	}

	@RequiredUIAccess
	private static CompletableFuture<Boolean> askRenameAll(PsiElement element)
	{
		Project project = element.getProject();
		if(project.getApplication().isUnitTestMode())
		{
			return CompletableFuture.completedFuture(Boolean.TRUE);
		}

		return MessageBoxBuilder.<Boolean>create()
				.asQuestion()
				.title(LocalizeValue.localizeTODO("Rename"))
				.text(LocalizeValue.localizeTODO("Rename all override/implement targets or this target?"))
				.button(MessageButtonRole.YES, LocalizeValue.localizeTODO("All Targets"), Boolean.TRUE)
				.asDefaultButton()
				.button(MessageButtonRole.NO, LocalizeValue.localizeTODO("This Target"), Boolean.FALSE)
				.exitValue(Boolean.FALSE)
				.showAsync(project)
				.thenApply(Boolean.TRUE::equals);
	}

	private static void putAll(Set<DotNetVirtualImplementOwner> elements, String newName, Map<PsiElement, String> allRenames)
	{
		for(DotNetVirtualImplementOwner tempElement : elements)
		{
			allRenames.put(tempElement, newName);
		}
	}

	@RequiredReadAction
	public static Set<DotNetVirtualImplementOwner> getAllElements(PsiElement element)
	{
		Collection<DotNetVirtualImplementOwner> temp1 = OverrideUtil.collectOverridingMembers((DotNetVirtualImplementOwner) element);
		Collection<DotNetVirtualImplementOwner> temp2 = OverrideUtil.collectOverriddenMembers((DotNetVirtualImplementOwner) element);
		Set<DotNetVirtualImplementOwner> set = new HashSet<DotNetVirtualImplementOwner>(temp1.size() + temp1.size());
		set.addAll(temp1);
		set.addAll(temp2);
		return set;
	}

	@Override
	@RequiredReadAction
	public boolean canProcessElement(PsiElement element)
	{
		return OverrideUtil.isAllowForOverride(element);
	}
}
