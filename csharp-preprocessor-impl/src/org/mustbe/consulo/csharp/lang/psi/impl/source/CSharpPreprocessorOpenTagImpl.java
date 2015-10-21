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
import org.mustbe.consulo.csharp.lang.psi.CSharpPreprocessorElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpPreprocessorTokens;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 18.12.13.
 */
public class CSharpPreprocessorOpenTagImpl extends CSharpPreprocessorElementImpl
{
	public CSharpPreprocessorOpenTagImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@Nullable
	public CSharpPreprocessorExpression getValue()
	{
		return findChildByClass(CSharpPreprocessorExpression.class);
	}

	@NotNull
	@RequiredReadAction
	public PsiElement getSharpElement()
	{
		return findNotNullChildByType(CSharpPreprocessorTokens.SHARP);
	}

	@Nullable
	@RequiredReadAction
	public PsiElement getKeywordElement()
	{
		return findChildByType(CSharpPreprocessorTokens.KEYWORDS);
	}

	@RequiredReadAction
	public boolean isElse()
	{
		return findChildByType(CSharpPreprocessorTokens.ELSE_KEYWORD) != null;
	}

	@Nullable
	@Deprecated
	public PsiElement getStopElement()
	{
		return null;
	}

	@Override
	public void accept(@NotNull CSharpPreprocessorElementVisitor visitor)
	{
		visitor.visitOpenTag(this);
	}
}
