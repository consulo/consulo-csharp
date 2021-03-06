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

import javax.annotation.Nonnull;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.psi.impl.source.CSharpMethodCallExpressionImpl;
import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Consumer;
import com.intellij.util.FunctionUtil;

/**
 * @author VISTALL
 * @since 26.03.14
 */
public class RecursiveCallCollector implements LineMarkerCollector
{
	@RequiredReadAction
	@Override
	public void collect(PsiElement psiElement, @Nonnull Consumer<LineMarkerInfo> consumer)
	{
		if(psiElement.getNode().getElementType() == CSharpTokens.IDENTIFIER && psiElement.getParent() instanceof CSharpReferenceExpression &&
				psiElement.getParent().getParent() instanceof CSharpMethodCallExpressionImpl)
		{
			PsiElement resolvedElement = ((CSharpReferenceExpression) psiElement.getParent()).resolve();
			if(resolvedElement instanceof CSharpMethodDeclaration)
			{
				CSharpMethodDeclaration methodDeclaration = PsiTreeUtil.getParentOfType(psiElement, CSharpMethodDeclaration.class);
				if(resolvedElement.isEquivalentTo(methodDeclaration))
				{
					LineMarkerInfo<PsiElement> lineMarkerInfo = new LineMarkerInfo<PsiElement>(psiElement, psiElement.getTextRange(), AllIcons.Gutter.RecursiveMethod, Pass.LINE_MARKERS,
							FunctionUtil.constant("Recursive call"), null, GutterIconRenderer.Alignment.CENTER);
					consumer.consume(lineMarkerInfo);
				}
			}
		}
	}
}
