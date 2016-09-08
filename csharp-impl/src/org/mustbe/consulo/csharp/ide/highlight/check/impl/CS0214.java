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
import consulo.annotations.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.codeInsight.actions.AddModifierFix;
import org.mustbe.consulo.csharp.ide.highlight.CSharpHighlightContext;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpLocalVariable;
import org.mustbe.consulo.csharp.lang.psi.CSharpLocalVariableDeclarationStatement;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpFixedStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpOperatorReferenceImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpPrefixExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpUnsafeStatementImpl;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetElement;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.psi.DotNetPointerType;
import consulo.dotnet.psi.DotNetQualifiedElement;
import consulo.dotnet.psi.DotNetType;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * @author VISTALL
 * @since 15.05.14
 */
public class CS0214 extends CompilerCheck<DotNetElement>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpHighlightContext highlightContext, @NotNull DotNetElement element)
	{
		if(element instanceof CSharpFixedStatementImpl)
		{
			Pair<Boolean, DotNetModifierListOwner> pair = isAllowedUnsafeCode(element);
			if(pair.getFirst() == Boolean.FALSE)
			{
				PsiElement target = ((CSharpFixedStatementImpl) element).getFixedElement();

				CompilerCheckBuilder builder = newBuilder(target);
				if(pair.getSecond() != null)
				{
					builder.addQuickFix(new AddModifierFix(CSharpModifier.UNSAFE, pair.getSecond())).setHighlightInfoType(HighlightInfoType
							.WRONG_REF);
				}
				return builder;
			}
		}
		else if(element instanceof CSharpLocalVariableDeclarationStatement)
		{
			Pair<Boolean, DotNetModifierListOwner> pair = isAllowedUnsafeCode(element);
			if(pair.getFirst() == Boolean.TRUE)
			{
				return null;
			}

			for(CSharpLocalVariable localVariable : ((CSharpLocalVariableDeclarationStatement) element).getVariables())
			{
				DotNetType selfType = localVariable.getSelfType();
				if(selfType instanceof DotNetPointerType)
				{
					CompilerCheckBuilder builder = newBuilder(selfType);
					if(pair.getSecond() != null)
					{
						builder.addQuickFix(new AddModifierFix(CSharpModifier.UNSAFE, pair.getSecond()));
					}
					return builder;
				}
			}
		}
		else if(element instanceof CSharpPrefixExpressionImpl)
		{
			CSharpOperatorReferenceImpl operatorElement = ((CSharpPrefixExpressionImpl) element).getOperatorElement();

			if(operatorElement.getOperatorElementType() == CSharpTokens.MUL || operatorElement.getOperatorElementType() == CSharpTokens.AND)
			{
				Pair<Boolean, DotNetModifierListOwner> pair = isAllowedUnsafeCode(element);
				if(pair.getFirst() == Boolean.TRUE)
				{
					return null;
				}

				CompilerCheckBuilder builder = newBuilder(element);
				if(pair.getSecond() != null)
				{
					builder.addQuickFix(new AddModifierFix(CSharpModifier.UNSAFE, pair.getSecond()));
				}
				return builder;
			}
		}
		return null;
	}

	@NotNull
	private static Pair<Boolean, DotNetModifierListOwner> isAllowedUnsafeCode(PsiElement element)
	{
		CSharpUnsafeStatementImpl unsafeStatement = PsiTreeUtil.getParentOfType(element, CSharpUnsafeStatementImpl.class);
		if(unsafeStatement != null)
		{
			return Pair.create(Boolean.TRUE, null);
		}

		DotNetQualifiedElement qualifiedElement = PsiTreeUtil.getParentOfType(element, DotNetQualifiedElement.class);
		if(!(qualifiedElement instanceof DotNetModifierListOwner))
		{
			// dont interest if we dont have parent - how it can be?
			return Pair.create(Boolean.TRUE, null);
		}

		DotNetModifierListOwner modifierListOwner = (DotNetModifierListOwner) qualifiedElement;
		return Pair.create(modifierListOwner.hasModifier(CSharpModifier.UNSAFE), modifierListOwner);
	}
}
