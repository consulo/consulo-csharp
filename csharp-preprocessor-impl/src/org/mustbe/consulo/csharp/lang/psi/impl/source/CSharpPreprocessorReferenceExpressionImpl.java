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

package org.mustbe.consulo.csharp.lang.psi.impl.source;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpPreprocessorDefineDirective;
import org.mustbe.consulo.csharp.lang.psi.CSharpPreprocessorElementVisitor;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 24.01.14
 */
public class CSharpPreprocessorReferenceExpressionImpl extends CSharpPreprocessorElementImpl implements CSharpPreprocessorExpression, PsiReference
{
	public CSharpPreprocessorReferenceExpressionImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@Override
	public PsiReference getReference()
	{
		return this;
	}

	@Override
	public void accept(@NotNull CSharpPreprocessorElementVisitor visitor)
	{
		visitor.visitReferenceExpression(this);
	}

	@Override
	public PsiElement getElement()
	{
		return this;
	}

	@Override
	public TextRange getRangeInElement()
	{
		PsiElement element = getElement();
		return new TextRange(0, element.getTextLength());
	}

	@Nullable
	@Override
	@RequiredReadAction
	public PsiElement resolve()
	{
		PsiFile containingFile = getContainingFile();
		if(!(containingFile instanceof CSharpPreprocessorFileImpl))
		{
			return null;
		}

		String text = getText();

		for(CSharpPreprocessorDefineDirective directive : ((CSharpPreprocessorFileImpl) containingFile).getVariables(true))
		{
			if(Comparing.equal(text, directive.getName()))
			{
				return directive;
			}
		}

		return null;
	}

	@NotNull
	@Override
	public String getCanonicalText()
	{
		return getText();
	}

	@Override
	public PsiElement handleElementRename(String s) throws IncorrectOperationException
	{
		return null;
	}

	@Override
	public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException
	{
		return null;
	}

	@Override
	public boolean isReferenceTo(PsiElement element)
	{
		return resolve() == element;
	}

	@NotNull
	@Override
	public Object[] getVariants()
	{
		return ArrayUtil.EMPTY_OBJECT_ARRAY;
	}

	@Override
	public boolean isSoft()
	{
		return true;
	}
}
