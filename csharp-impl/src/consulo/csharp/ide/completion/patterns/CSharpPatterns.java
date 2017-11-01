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

package consulo.csharp.ide.completion.patterns;

import org.jetbrains.annotations.NotNull;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpCallArgument;
import consulo.csharp.lang.psi.CSharpFieldDeclaration;
import consulo.csharp.lang.psi.CSharpLocalVariable;
import consulo.csharp.lang.psi.CSharpLocalVariableDeclarationStatement;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpReferenceExpressionEx;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.psi.CSharpUserType;
import consulo.csharp.lang.psi.UsefulPsiTreeUtil;
import consulo.csharp.lang.psi.impl.source.CSharpConstructorSuperCallImpl;
import consulo.csharp.lang.psi.impl.source.CSharpPsiUtilImpl;
import consulo.dotnet.psi.DotNetType;

/**
 * @author VISTALL
 * @since 01.08.2015
 */
public class CSharpPatterns
{
	@NotNull
	public static PsiElementPattern.Capture<PsiElement> expressionStart()
	{
		return StandardPatterns.psiElement(CSharpTokens.IDENTIFIER).withParent(CSharpReferenceExpressionEx.class).with(new PatternCondition<PsiElement>("error validator")
		{
			@Override
			public boolean accepts(@NotNull PsiElement element, ProcessingContext processingContext)
			{
				CSharpReferenceExpression expression = PsiTreeUtil.getParentOfType(element, CSharpReferenceExpression.class);
				assert expression != null;

				PsiElement parent = expression.getParent();
				if(parent instanceof CSharpCallArgument)
				{
					PsiElement temp = UsefulPsiTreeUtil.getPrevSiblingSkipWhiteSpaces(parent, true);
					if(temp instanceof PsiErrorElement)
					{
						return false;
					}

					temp = UsefulPsiTreeUtil.getNextSiblingSkippingWhiteSpacesAndComments(parent);
					if(temp instanceof PsiErrorElement)
					{
						return false;
					}
				}
				else if(parent instanceof CSharpConstructorSuperCallImpl)
				{
					return false;
				}
				return true;
			}
		});
	}

	@NotNull
	public static PsiElementPattern.Capture<PsiElement> statementStart()
	{
		return StandardPatterns.psiElement().withElementType(CSharpTokens.IDENTIFIER).withSuperParent(3, CSharpLocalVariable.class).with(new PatternCondition<PsiElement>("null-identifier-local-var")

		{
			@Override
			@RequiredReadAction
			public boolean accepts(@NotNull PsiElement element, ProcessingContext context)
			{
				CSharpLocalVariable localVariable = PsiTreeUtil.getParentOfType(element, CSharpLocalVariable.class);
				// we cant use it when 'const <exp>'
				if(localVariable == null || localVariable.isConstant())
				{
					return false;
				}
				// disable it inside non local decl statement, like catch
				if(!(localVariable.getParent() instanceof CSharpLocalVariableDeclarationStatement))
				{
					return false;
				}
				DotNetType type = localVariable.getType();
				if(!(type instanceof CSharpUserType))
				{
					return false;
				}
				CSharpReferenceExpression referenceExpression = ((CSharpUserType) type).getReferenceExpression();
				if(referenceExpression.getQualifier() != null)
				{
					return false;
				}
				return CSharpPsiUtilImpl.isNullOrEmpty(localVariable);
			}
		});
	}

	@NotNull
	public static PsiElementPattern.Capture<PsiElement> fieldStart()
	{
		return StandardPatterns.psiElement().withElementType(CSharpTokens.IDENTIFIER).withSuperParent(3, CSharpFieldDeclaration.class);
	}
}
