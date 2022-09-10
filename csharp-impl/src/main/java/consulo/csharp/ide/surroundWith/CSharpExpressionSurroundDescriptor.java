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

package consulo.csharp.ide.surroundWith;

import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.lang.CSharpLanguage;
import consulo.dotnet.psi.DotNetExpression;
import consulo.language.Language;
import consulo.language.editor.surroundWith.SurroundDescriptor;
import consulo.language.editor.surroundWith.Surrounder;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiWhiteSpace;
import consulo.language.psi.util.PsiTreeUtil;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 17.05.14
 */
@ExtensionImpl
public class CSharpExpressionSurroundDescriptor implements SurroundDescriptor
{
	private Surrounder[] mySurrounders = new Surrounder[] {
		new CSharpWithParenthesesSurrounder()
	};

	@Nonnull
	@Override
	public Surrounder[] getSurrounders()
	{
		return mySurrounders;
	}

	@Nonnull
	@Override
	public PsiElement[] getElementsToSurround(PsiFile file, int startOffset, int endOffset)
	{
		final DotNetExpression expr = findExpressionInRange(file, startOffset, endOffset);
		if(expr == null)
		{
			return PsiElement.EMPTY_ARRAY;
		}
		return new PsiElement[]{expr};
	}

	private static DotNetExpression findExpressionInRange(PsiFile file, int startOffset, int endOffset)
	{
		PsiElement element1 = file.findElementAt(startOffset);
		PsiElement element2 = file.findElementAt(endOffset - 1);
		if(element1 instanceof PsiWhiteSpace)
		{
			startOffset = element1.getTextRange().getEndOffset();
		}
		if(element2 instanceof PsiWhiteSpace)
		{
			endOffset = element2.getTextRange().getStartOffset();
		}
		DotNetExpression expression = PsiTreeUtil.findElementOfClassAtRange(file, startOffset, endOffset, DotNetExpression.class);
		if(expression == null || expression.getTextRange().getEndOffset() != endOffset)
		{
			return null;
		}
		return expression;
	}

	@Override
	public boolean isExclusive()
	{
		return false;
	}

	@Nonnull
	@Override
	public Language getLanguage()
	{
		return CSharpLanguage.INSTANCE;
	}
}
