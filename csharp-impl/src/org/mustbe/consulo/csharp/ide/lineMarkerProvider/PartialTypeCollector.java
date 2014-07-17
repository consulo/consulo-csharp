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
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.resolve.CSharpPsiSearcher;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.psi.PsiElement;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Function;
import lombok.val;

/**
 * @author VISTALL
 * @since 25.03.14
 */
public class PartialTypeCollector implements LineMarkerCollector
{
	@Override
	public void collect(PsiElement psiElement, @NotNull Collection<LineMarkerInfo> lineMarkerInfos)
	{
		if(psiElement.getParent() instanceof CSharpTypeDeclaration && psiElement.getNode().getElementType() == CSharpTokens.IDENTIFIER)
		{
			CSharpTypeDeclaration parent = (CSharpTypeDeclaration) psiElement.getParent();

			if(!parent.hasModifier(CSharpModifier.PARTIAL))
			{
				return;
			}

			val vmQName = parent.getVmQName();
			assert vmQName != null;
			DotNetTypeDeclaration[] types = CSharpPsiSearcher.getInstance(parent.getProject()).findTypes(vmQName, parent.getResolveScope());

			if(types.length > 1 && ArrayUtil.contains(parent, types))
			{
				val lineMarkerInfo = new LineMarkerInfo<PsiElement>(psiElement, psiElement.getTextRange(), AllIcons.Nodes.TreeDownArrow,
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
					public void navigate(MouseEvent mouseEvent, PsiElement element)
					{
						val typeDeclaration = (DotNetTypeDeclaration) element.getParent();

						DotNetTypeDeclaration[] types = CSharpPsiSearcher.getInstance(typeDeclaration.getProject()).findTypes(vmQName,
								typeDeclaration.getResolveScope());

						DotNetTypeDeclaration[] newArray = ArrayUtil.remove(types, typeDeclaration);

						JBPopup popup = NavigationUtil.getPsiElementPopup(newArray, "Open types (" + newArray.length + " items)");
						popup.show(new RelativePoint(mouseEvent));
					}
				}, GutterIconRenderer.Alignment.LEFT
				);
				lineMarkerInfos.add(lineMarkerInfo);
			}
		}
	}
}
