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

package consulo.csharp.ide.lineMarkerProvider;

import java.awt.event.MouseEvent;
import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.intellij.codeInsight.daemon.impl.PsiElementListNavigator;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpIdentifier;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.psi.impl.source.resolve.overrideSystem.OverrideUtil;
import consulo.dotnet.psi.DotNetVirtualImplementOwner;

/**
 * @author VISTALL
 * @since 26.07.2015
 */
public class CSharpLineMarkerUtil
{
	public static final Function<PsiElement, PsiElement> BY_PARENT = new Function<PsiElement, PsiElement>()
	{
		@Override
		public PsiElement fun(PsiElement element)
		{
			return element.getParent();
		}
	};

	@RequiredReadAction
	public static void openTargets(@Nonnull Collection<? extends PsiElement> members, @Nonnull MouseEvent mouseEvent, @Nonnull String text, @Nonnull final Function<PsiElement, PsiElement> map)
	{
		NavigatablePsiElement[] navigatablePsiElements = members.toArray(new NavigatablePsiElement[members.size()]);
		ContainerUtil.sort(navigatablePsiElements, (o1, o2) ->
		{
			PsiElement map1 = map.fun(o1);
			PsiElement map2 = map.fun(o2);
			if(map1 instanceof PsiNamedElement && map2 instanceof PsiNamedElement)
			{
				return Comparing.compare(((PsiNamedElement) map1).getName(), ((PsiNamedElement) map2).getName());
			}
			return 0;
		});

		PsiElementListNavigator.openTargets(mouseEvent, navigatablePsiElements, text, text, new PsiMappedElementListCellRender(map));
	}

	@Nullable
	public static DotNetVirtualImplementOwner findElementForLineMarker(@Nonnull PsiElement element)
	{
		PsiElement superParent = null;
		IElementType elementType = PsiUtilCore.getElementType(element);
		if(elementType == CSharpTokens.THIS_KEYWORD)
		{
			superParent = element.getParent();
		}
		else if(elementType == CSharpTokens.IDENTIFIER)
		{
			superParent = getParentIfIsIdentifier(element);
		}
		if(superParent == null)
		{
			return null;
		}

		return OverrideUtil.isAllowForOverride(superParent) ? (DotNetVirtualImplementOwner) superParent : null;
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public static <T> T getNameIdentifierAs(@Nullable PsiElement element, @Nonnull Class<T> clazz)
	{
		if(element == null)
		{
			return null;
		}

		PsiElement parentIfIsIdentifier = getParentIfIsIdentifier(element);
		if(parentIfIsIdentifier != null)
		{
			return clazz.isAssignableFrom(parentIfIsIdentifier.getClass()) ? (T) parentIfIsIdentifier : null;
		}
		return null;
	}

	@Nullable
	public static PsiElement getParentIfIsIdentifier(@Nonnull PsiElement element)
	{
		IElementType elementType = PsiUtilCore.getElementType(element);
		if(elementType == CSharpTokens.IDENTIFIER && element.getParent() instanceof CSharpIdentifier)
		{
			return element.getParent().getParent();
		}
		return null;
	}
}
