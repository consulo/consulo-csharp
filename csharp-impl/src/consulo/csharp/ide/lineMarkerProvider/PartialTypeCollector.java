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

package consulo.csharp.ide.lineMarkerProvider;

import java.awt.event.MouseEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredDispatchThread;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.psi.impl.partial.CSharpCompositeTypeDeclaration;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.impl.PsiElementListNavigator;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.PsiElementListCellRenderer;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.presentation.java.SymbolPresentationUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.Consumer;
import com.intellij.util.Function;

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
	public void collect(PsiElement psiElement, @NotNull Consumer<LineMarkerInfo> consumer)
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

			LineMarkerInfo<PsiElement> lineMarkerInfo = new LineMarkerInfo<PsiElement>(psiElement, psiElement.getTextRange(), AllIcons.Nodes.TreeDownArrow, Pass.UPDATE_OVERRIDEN_MARKERS,
					new Function<PsiElement, String>()

			{
				@Override
				public String fun(PsiElement element)
				{
					return "Navigate to partial types";
				}
			}, new GutterIconNavigationHandler<PsiElement>()
			{
				@Override
				@RequiredDispatchThread
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
			consumer.consume(lineMarkerInfo);
		}
	}
}
