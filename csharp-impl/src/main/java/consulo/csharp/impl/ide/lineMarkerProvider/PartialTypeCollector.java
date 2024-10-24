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
import consulo.codeEditor.markup.GutterIconRenderer;
import consulo.component.util.Iconable;
import consulo.csharp.lang.impl.psi.partial.CSharpCompositeTypeDeclaration;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.language.editor.Pass;
import consulo.language.editor.gutter.GutterIconNavigationHandler;
import consulo.language.editor.gutter.LineMarkerInfo;
import consulo.language.editor.ui.PsiElementListCellRenderer;
import consulo.language.editor.ui.PsiElementListNavigator;
import consulo.language.psi.NavigatablePsiElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiUtilCore;
import consulo.language.psi.util.SymbolPresentationUtil;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.virtualFileSystem.VirtualFile;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 25.03.14
 */
public class PartialTypeCollector implements LineMarkerCollector
{
	public static class OurRender extends PsiElementListCellRenderer<PsiElement>
	{
		@Override
		public String getElementText(PsiElement element)
		{
			VirtualFile virtualFile = PsiUtilCore.getVirtualFile(element);
			return virtualFile == null ? SymbolPresentationUtil.getSymbolPresentableText(element) : virtualFile.getName();
		}

		@Nullable
		@Override
		protected String getContainerText(PsiElement element, String name)
		{
			VirtualFile virtualFile = PsiUtilCore.getVirtualFile(element);
			if(virtualFile == null)
			{
				return SymbolPresentationUtil.getSymbolContainerText(element);
			}
			else
			{
				return "(" + virtualFile.getPath() + ")";
			}
		}

		@Override
		protected int getIconFlags()
		{
			return Iconable.ICON_FLAG_VISIBILITY;
		}
	}

	@RequiredReadAction
	@Override
	public void collect(PsiElement psiElement, @Nonnull Consumer<LineMarkerInfo> consumer)
	{
		CSharpTypeDeclaration parent = CSharpLineMarkerUtil.getNameIdentifierAs(psiElement, CSharpTypeDeclaration.class);
		if(parent != null)
		{
			if(!parent.hasModifier(CSharpModifier.PARTIAL))
			{
				return;
			}

			CSharpCompositeTypeDeclaration compositeType = CSharpCompositeTypeDeclaration.findCompositeType(parent);
			if(compositeType == null)
			{
				return;
			}

			LineMarkerInfo<PsiElement> lineMarkerInfo = new LineMarkerInfo<PsiElement>(psiElement, psiElement.getTextRange(), PlatformIconGroup.gutterFold(), Pass.LINE_MARKERS,
					element -> "Navigate to partial types", new GutterIconNavigationHandler<PsiElement>()
			{
				@Override
				@RequiredUIAccess
				public void navigate(MouseEvent mouseEvent, PsiElement element)
				{
					final CSharpTypeDeclaration typeDeclaration = CSharpLineMarkerUtil.getNameIdentifierAs(element, CSharpTypeDeclaration.class);

					assert typeDeclaration != null;

					CSharpCompositeTypeDeclaration compositeType = CSharpCompositeTypeDeclaration.findCompositeType(typeDeclaration);
					if(compositeType == null)
					{
						return;
					}

					DotNetTypeDeclaration[] newArray = compositeType.getTypeDeclarations();
					NavigatablePsiElement[] navigatablePsiElements = new NavigatablePsiElement[newArray.length];
					for(int i = 0; i < newArray.length; i++)
					{
						navigatablePsiElements[i] = (NavigatablePsiElement) newArray[i];
					}
					PsiElementListNavigator.openTargets(mouseEvent, navigatablePsiElements, "Navigate to partial types", "Navigate to partial types", new OurRender());
				}
			}, GutterIconRenderer.Alignment.CENTER
			);
			consumer.accept(lineMarkerInfo);
		}
	}
}
