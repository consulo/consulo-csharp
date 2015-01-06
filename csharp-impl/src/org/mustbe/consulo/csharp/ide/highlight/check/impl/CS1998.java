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

package org.mustbe.consulo.csharp.ide.highlight.check.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpSimpleLikeMethodAsElement;
import org.mustbe.consulo.csharp.lang.psi.CSharpStoppableRecursiveElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpAwaitExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpLambdaExpressionImpl;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.psi.DotNetModifierList;
import org.mustbe.consulo.dotnet.psi.DotNetModifierListOwner;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 27.11.14
 */
public class CS1998 extends CompilerCheck<CSharpSimpleLikeMethodAsElement>
{
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpSimpleLikeMethodAsElement element)
	{
		PsiElement modifierElement = getAsyncModifier(element);
		if(modifierElement == null)
		{
			return null;
		}
		PsiElement codeBlock = element.getCodeBlock();
		if(codeBlock == null)
		{
			return null;
		}
		CSharpStoppableRecursiveElementVisitor<Boolean> visitor = new CSharpStoppableRecursiveElementVisitor<Boolean>()
		{
			@Override
			public void visitAwaitExpression(CSharpAwaitExpressionImpl expression)
			{
				stopWalk(Boolean.TRUE);
			}
		};
		codeBlock.accept(visitor);

		if(visitor.getValue() == null)
		{
			return newBuilder(modifierElement);
		}

		return null;
	}

	@Nullable
	public static PsiElement getAsyncModifier(PsiElement element)
	{
		if(!(element instanceof CSharpSimpleLikeMethodAsElement))
		{
			return null;
		}

		if(element instanceof DotNetModifierListOwner)
		{
			DotNetModifierList modifierList = ((DotNetModifierListOwner) element).getModifierList();
			if(modifierList == null)
			{
				return null;
			}
			return modifierList.getModifierElement(CSharpModifier.ASYNC);
		}
		else if(element instanceof CSharpLambdaExpressionImpl)
		{
			return ((CSharpLambdaExpressionImpl) element).getModifierElement(CSharpModifier.ASYNC);
		}
		return null;
	}
}
