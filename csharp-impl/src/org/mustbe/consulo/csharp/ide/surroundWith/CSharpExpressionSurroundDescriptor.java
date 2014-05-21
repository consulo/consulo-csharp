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

package org.mustbe.consulo.csharp.ide.surroundWith;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import com.intellij.lang.surroundWith.SurroundDescriptor;
import com.intellij.lang.surroundWith.Surrounder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * @author VISTALL
 * @since 17.05.14
 */
public class CSharpExpressionSurroundDescriptor implements SurroundDescriptor
{
	private Surrounder[] mySurrounders = new Surrounder[] {
		new CSharpWithParenthesesSurrounder()
	};

	@NotNull
	@Override
	public Surrounder[] getSurrounders()
	{
		return mySurrounders;
	}

	@NotNull
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
}
