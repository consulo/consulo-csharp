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
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpMethodCallExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpReferenceExpressionImpl;
import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ConstantFunction;
import lombok.val;

/**
 * @author VISTALL
 * @since 26.03.14
 */
public class RecursiveCallCollector implements LineMarkerCollector
{
	@Override
	public void collect(PsiElement psiElement, @NotNull Collection<LineMarkerInfo> lineMarkerInfos)
	{
		if(psiElement.getNode().getElementType() == CSharpTokens.IDENTIFIER && psiElement.getParent() instanceof CSharpReferenceExpressionImpl &&
				psiElement.getParent().getParent() instanceof CSharpMethodCallExpressionImpl)
		{
			PsiElement resolve = ((CSharpReferenceExpressionImpl) psiElement.getParent()).resolve();
			if(resolve instanceof CSharpMethodDeclaration)
			{
				CSharpMethodDeclaration parentOfType = PsiTreeUtil.getParentOfType(psiElement, CSharpMethodDeclaration.class);
				if(parentOfType == resolve)
				{
					val lineMarkerInfo = new LineMarkerInfo<PsiElement>(psiElement, psiElement.getTextRange(), AllIcons.Gutter.RecursiveMethod,
							Pass.UPDATE_OVERRIDEN_MARKERS, new ConstantFunction<PsiElement, String>("Recursive call"), new GutterIconNavigationHandler<PsiElement>()

					{
						@Override
						public void navigate(MouseEvent e, PsiElement elt)
						{

						}
					}, GutterIconRenderer.Alignment.LEFT);
					lineMarkerInfos.add(lineMarkerInfo);
				}
			}
		}
	}
}
