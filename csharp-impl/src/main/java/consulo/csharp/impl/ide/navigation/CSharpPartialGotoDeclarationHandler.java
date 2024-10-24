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

package consulo.csharp.impl.ide.navigation;

import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.csharp.impl.ide.lineMarkerProvider.PartialTypeCollector;
import consulo.csharp.lang.impl.psi.light.builder.CSharpLightConstructorDeclarationBuilder;
import consulo.csharp.lang.impl.psi.partial.CSharpCompositeTypeDeclaration;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.dataContext.DataContext;
import consulo.language.ast.ASTNode;
import consulo.language.editor.ui.PsiElementListCellRenderer;
import consulo.language.editor.ui.navigation.GotoDeclarationHandlerEx;
import consulo.language.psi.PsiElement;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 02.05.2015
 */
@ExtensionImpl
public class CSharpPartialGotoDeclarationHandler implements GotoDeclarationHandlerEx
{
	@Nullable
	@Override
	public PsiElement[] getGotoDeclarationTargets(PsiElement sourceElement, int offset, Editor editor)
	{
		ASTNode node = sourceElement == null ? null : sourceElement.getNode();
		if(node != null && node.getElementType() == CSharpTokens.IDENTIFIER)
		{
			PsiElement parent = sourceElement.getParent();
			if(parent instanceof CSharpReferenceExpression)
			{
				PsiElement resolvedElement = ((CSharpReferenceExpression) parent).resolve();
				if(resolvedElement instanceof CSharpCompositeTypeDeclaration)
				{
					return ((CSharpCompositeTypeDeclaration) resolvedElement).getTypeDeclarations();
				}
				else if(resolvedElement instanceof CSharpLightConstructorDeclarationBuilder)
				{
					PsiElement nextParent = resolvedElement.getParent();
					if(nextParent instanceof CSharpCompositeTypeDeclaration)
					{
						return ((CSharpCompositeTypeDeclaration) nextParent).getTypeDeclarations();
					}
				}
			}
		}
		return null;
	}

	@Nullable
	@Override
	public String getActionText(DataContext context)
	{
		return null;
	}

	@Nonnull
	@Override
	public PsiElementListCellRenderer<PsiElement> createRender(@Nonnull PsiElement[] elements)
	{
		return new PartialTypeCollector.OurRender();
	}
}
