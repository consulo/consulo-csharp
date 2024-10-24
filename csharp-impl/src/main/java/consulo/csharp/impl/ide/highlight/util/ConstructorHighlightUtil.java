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

package consulo.csharp.impl.ide.highlight.util;

import jakarta.annotation.Nonnull;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.impl.ide.highlight.quickFix.RenameQuickFix;
import consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.impl.psi.source.CSharpPsiUtilImpl;
import consulo.language.editor.rawHighlight.HighlightInfo;
import consulo.language.editor.rawHighlight.HighlightInfoType;
import consulo.language.editor.intention.QuickFixAction;
import consulo.language.psi.PsiElement;
import consulo.util.lang.Comparing;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 12.11.14
 */
public class ConstructorHighlightUtil
{
	@Nullable
	@RequiredReadAction
	public static HighlightInfo checkConstructorDeclaration(@Nonnull CSharpConstructorDeclaration declaration)
	{
		PsiElement nameIdentifier = declaration.getNameIdentifier();

		if(nameIdentifier == null)
		{
			return null;
		}

		PsiElement parent = declaration.getParent();
		if(!(parent instanceof CSharpTypeDeclaration))
		{
			return null;
		}

		String expectedTypeName = ((CSharpTypeDeclaration) parent).getName();
		if(expectedTypeName == null)
		{
			return null;
		}
		if(!Comparing.equal(expectedTypeName, CSharpPsiUtilImpl.getNameWithoutAt(nameIdentifier.getText())))
		{
			HighlightInfo.Builder builder = HighlightInfo.newHighlightInfo(HighlightInfoType.ERROR);
			builder = builder.descriptionAndTooltip("Expected method name");
			builder = builder.range(nameIdentifier);
			HighlightInfo highlightInfo = builder.create();
			QuickFixAction.registerQuickFixAction(highlightInfo, new RenameQuickFix(expectedTypeName, declaration));
			return highlightInfo;
		}
		return null;
	}
}
