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

package org.mustbe.consulo.csharp.ide.lineMarkerProvider;

import java.awt.event.MouseEvent;
import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredDispatchThread;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.partial.CSharpCompositeTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.resolve.CSharpPsiSearcher;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.PsiElementListCellRenderer;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.presentation.java.SymbolPresentationUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.ArrayUtil;
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
	public void collect(PsiElement psiElement, @NotNull Collection<LineMarkerInfo> lineMarkerInfos)
	{
		CSharpTypeDeclaration parent = CSharpLineMarkerUtil.getNameIdentifierAs(psiElement, CSharpTypeDeclaration.class);
		if(parent != null)
		{
			if(!parent.hasModifier(CSharpModifier.PARTIAL))
			{
				return;
			}

			CSharpCompositeTypeDeclaration compositeType = findCompositeType(parent);
			if(compositeType == null)
			{
				return;
			}

			LineMarkerInfo<PsiElement> lineMarkerInfo = new LineMarkerInfo<PsiElement>(psiElement, psiElement.getTextRange(), AllIcons.Nodes.TreeDownArrow,
					Pass.UPDATE_OVERRIDEN_MARKERS, new Function<PsiElement, String>()

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

					CSharpCompositeTypeDeclaration compositeType = findCompositeType(typeDeclaration);
					if(compositeType == null)
					{
						return;
					}

					DotNetTypeDeclaration[] newArray = compositeType.getTypeDeclarations();

					JBPopup popup = NavigationUtil.getPsiElementPopup(newArray, new OurRender(), "Open types (" + newArray.length + " items)");
					popup.show(new RelativePoint(mouseEvent));
				}
			}, GutterIconRenderer.Alignment.LEFT
			);
			lineMarkerInfos.add(lineMarkerInfo);
		}
	}

	@RequiredReadAction
	private static CSharpCompositeTypeDeclaration findCompositeType(CSharpTypeDeclaration parent)
	{
		String vmQName = parent.getVmQName();
		assert vmQName != null;
		DotNetTypeDeclaration[] types = CSharpPsiSearcher.getInstance(parent.getProject()).findTypes(vmQName, parent.getResolveScope());

		for(DotNetTypeDeclaration type : types)
		{
			if(type instanceof CSharpCompositeTypeDeclaration)
			{
				CSharpTypeDeclaration[] typeDeclarations = ((CSharpCompositeTypeDeclaration) type).getTypeDeclarations();
				if(ArrayUtil.contains(parent, typeDeclarations))
				{
					return (CSharpCompositeTypeDeclaration) type;
				}
			}
		}

		return null;
	}
}
