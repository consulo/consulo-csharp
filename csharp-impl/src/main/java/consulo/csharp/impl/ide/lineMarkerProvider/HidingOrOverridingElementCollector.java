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

package consulo.csharp.impl.ide.lineMarkerProvider;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.AllIcons;
import consulo.codeEditor.markup.GutterIconRenderer;
import consulo.csharp.lang.impl.psi.source.resolve.overrideSystem.OverrideUtil;
import consulo.csharp.lang.psi.icon.CSharpPsiIconGroup;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.DotNetVirtualImplementOwner;
import consulo.language.editor.Pass;
import consulo.language.editor.gutter.GutterIconNavigationHandler;
import consulo.language.editor.gutter.LineMarkerInfo;
import consulo.language.psi.PsiElement;
import consulo.navigation.Navigatable;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.image.Image;
import consulo.util.collection.ContainerUtil;

import javax.annotation.Nonnull;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 10.06.14
 */
public class HidingOrOverridingElementCollector implements LineMarkerCollector
{
	private static class OurHandler implements GutterIconNavigationHandler<PsiElement>
	{
		public static final OurHandler INSTANCE = new OurHandler();

		@Override
		@RequiredUIAccess
		public void navigate(MouseEvent mouseEvent, PsiElement element)
		{
			DotNetVirtualImplementOwner virtualImplementOwner = CSharpLineMarkerUtil.findElementForLineMarker(element);
			if(virtualImplementOwner == null)
			{
				return;
			}

			Collection<DotNetVirtualImplementOwner> members = OverrideUtil.collectOverridingMembers(virtualImplementOwner);

			if(members.isEmpty())
			{
				return;
			}

			if(members.size() == 1)
			{
				DotNetVirtualImplementOwner firstItem = ContainerUtil.getFirstItem(members);
				if(firstItem instanceof Navigatable)
				{
					((Navigatable) firstItem).navigate(true);
				}
			}
			else
			{
				CSharpLineMarkerUtil.openTargets(members, mouseEvent, "Searching for overriding", CSharpLineMarkerUtil.BY_PARENT);
			}
		}
	}

	@RequiredReadAction
	@Override
	public void collect(PsiElement psiElement, @Nonnull Consumer<LineMarkerInfo> consumer)
	{
		DotNetVirtualImplementOwner virtualImplementOwner = CSharpLineMarkerUtil.findElementForLineMarker(psiElement);
		if(virtualImplementOwner != null)
		{
			PsiElement parentParent = virtualImplementOwner.getParent();
			if(!(parentParent instanceof DotNetTypeDeclaration))
			{
				return;
			}

			Collection<DotNetVirtualImplementOwner> overrideElements = OverrideUtil.collectOverridingMembers(virtualImplementOwner);

			if(overrideElements.isEmpty())
			{
				return;
			}

			Image icon = null;
			if(virtualImplementOwner.getTypeForImplement() != null)
			{
				icon = CSharpPsiIconGroup.gutterHidingmethod();
			}
			else
			{
				icon = AllIcons.Gutter.ImplementingMethod;
				for(DotNetVirtualImplementOwner overrideElement : overrideElements)
				{
					if(!((DotNetModifierListOwner) overrideElement).hasModifier(DotNetModifier.ABSTRACT))
					{
						icon = AllIcons.Gutter.OverridingMethod;
						break;
					}
				}
			}

			LineMarkerInfo<PsiElement> lineMarkerInfo = new LineMarkerInfo<>(psiElement, psiElement.getTextRange(), icon, Pass.LINE_MARKERS, it -> "Searching for overriding",
					OurHandler.INSTANCE, GutterIconRenderer.Alignment.RIGHT);

			consumer.accept(lineMarkerInfo);
		}
	}
}
