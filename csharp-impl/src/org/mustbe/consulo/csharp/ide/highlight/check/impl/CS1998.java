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
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.codeInsight.actions.RemoveModifierFix;
import org.mustbe.consulo.csharp.ide.highlight.CSharpHighlightContext;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpSimpleLikeMethodAsElement;
import org.mustbe.consulo.csharp.lang.psi.CSharpStoppableRecursiveElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpAwaitExpressionImpl;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.psi.DotNetModifierList;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 27.11.14
 */
public class CS1998 extends CompilerCheck<CSharpSimpleLikeMethodAsElement>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpHighlightContext highlightContext, @NotNull CSharpSimpleLikeMethodAsElement element)
	{
		DotNetModifierList modifierList = element.getModifierList();
		if(modifierList == null)
		{
			return null;
		}
		PsiElement modifierElement = modifierList.getModifierElement(CSharpModifier.ASYNC);
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
			return newBuilder(modifierElement).addQuickFix(new RemoveModifierFix(CSharpModifier.ASYNC, element));
		}

		return null;
	}
}
