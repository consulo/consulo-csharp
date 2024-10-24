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

package consulo.csharp.impl.ide.completion.patterns;

import consulo.language.pattern.PatternCondition;
import consulo.language.pattern.PsiElementPattern;
import consulo.language.pattern.StandardPatterns;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiErrorElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.UsefulPsiTreeUtil;
import consulo.csharp.lang.psi.*;
import consulo.csharp.lang.impl.psi.source.CSharpConstructorSuperCallImpl;
import consulo.csharp.lang.impl.psi.source.CSharpExpressionStatementImpl;
import consulo.csharp.lang.impl.psi.source.CSharpPsiUtilImpl;
import consulo.dotnet.psi.DotNetType;
import consulo.language.util.ProcessingContext;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 01.08.2015
 */
public class CSharpPatterns
{
	@Nonnull
	public static PsiElementPattern.Capture<PsiElement> expressionStart()
	{
		return StandardPatterns.psiElement(CSharpTokens.IDENTIFIER).withParent(CSharpReferenceExpressionEx.class).with(new PatternCondition<PsiElement>("csharp-expression")
		{
			@Override
			public boolean accepts(@Nonnull PsiElement element, ProcessingContext processingContext)
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

				if(parent instanceof CSharpExpressionStatementImpl)
				{
					PsiElement previous = PsiTreeUtil.skipWhitespacesBackward(parent);
					return previous == null || !(previous.getLastChild() instanceof PsiErrorElement);
				}
				
				return true;
			}
		});
	}

	@Nonnull
	public static PsiElementPattern.Capture<PsiElement> statementStart()
	{
		return StandardPatterns.psiElement().withElementType(CSharpTokens.IDENTIFIER).with(new PatternCondition<PsiElement>("csharp-statement")
		{
			@Override
			@RequiredReadAction
			public boolean accepts(@Nonnull PsiElement element, ProcessingContext processingContext)
			{
				PsiElement parent = element.getParent();
				PsiElement parent2 = parent == null ? null : parent.getParent();
				PsiElement parent3 = parent2 == null ? null : parent2.getParent();
				if(parent3 instanceof CSharpLocalVariable)
				{
					return validateLocalVariable((CSharpLocalVariable) parent3);
				}

				if(parent instanceof CSharpReferenceExpression && parent2 instanceof CSharpExpressionStatementImpl)
				{
					CSharpReferenceExpression expression = (CSharpReferenceExpression) parent;
					if(expression.getQualifier() != null)
					{
						return false;
					}

					CSharpReferenceExpression.ResolveToKind kind = expression.kind();
					if(kind != CSharpReferenceExpression.ResolveToKind.ANY_MEMBER)
					{
						return false;
					}
				}

				if(parent2 instanceof CSharpExpressionStatementImpl)
				{
					PsiElement previous = PsiTreeUtil.skipWhitespacesBackward(parent2);
					return previous == null || !(previous.getLastChild() instanceof PsiErrorElement);
				}

				return false;
			}

			@RequiredReadAction
			private boolean validateLocalVariable(CSharpLocalVariable localVariable)
			{
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
		/*return StandardPatterns.psiElement().withElementType(CSharpTokens.IDENTIFIER).withSuperParent(3, CSharpLocalVariable.class).with(new PatternCondition<PsiElement>
		("null-identifier-local-var")

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
		}); */
	}

	@Nonnull
	public static PsiElementPattern.Capture<PsiElement> fieldStart()
	{
		return StandardPatterns.psiElement().withElementType(CSharpTokens.IDENTIFIER).withSuperParent(3, CSharpFieldDeclaration.class).with(new PatternCondition<PsiElement>("field-type-no-qualifier")
		{
			@Override
			@RequiredReadAction
			public boolean accepts(@Nonnull PsiElement element, ProcessingContext context)
			{
				CSharpFieldDeclaration declaration = PsiTreeUtil.getParentOfType(element, CSharpFieldDeclaration.class);
				if(declaration == null)
				{
					return false;
				}
				DotNetType type = declaration.getType();
				if(!(type instanceof CSharpUserType))
				{
					return false;
				}
				CSharpReferenceExpression referenceExpression = ((CSharpUserType) type).getReferenceExpression();
				if(referenceExpression.getQualifier() != null)
				{
					return false;
				}
				return true;
			}
		});
	}
}
