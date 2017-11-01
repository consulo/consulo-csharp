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

package consulo.csharp.ide.highlight.check.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.psi.PsiElement;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.ide.highlight.CSharpHighlightContext;
import consulo.csharp.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpFieldDeclaration;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.psi.impl.source.CSharpAssignmentExpressionImpl;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetExpression;

/**
 * @author VISTALL
 * @since 01-Nov-17
 */
public class CS1612 extends CompilerCheck<CSharpAssignmentExpressionImpl>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpHighlightContext highlightContext, @NotNull CSharpAssignmentExpressionImpl element)
	{
		DotNetExpression leftExpression = element.getLeftExpression();

		if(leftExpression instanceof CSharpReferenceExpression)
		{
			PsiElement targetField = ((CSharpReferenceExpression) leftExpression).resolve();

			if(targetField instanceof CSharpFieldDeclaration)
			{
				PsiElement parent = targetField.getParent();
				if(!(parent instanceof CSharpTypeDeclaration))
				{
					return null;
				}

				if(!(((CSharpTypeDeclaration) parent).isStruct()))
				{
					return null;
				}

				DotNetExpression qualifier = ((CSharpReferenceExpression) leftExpression).getQualifier();
				if(qualifier instanceof CSharpReferenceExpression)
				{
					DotNetExpression nextQualifier = ((CSharpReferenceExpression) qualifier).getQualifier();
					if(nextQualifier != null)
					{
						if(nextQualifier instanceof CSharpReferenceExpression && (((CSharpReferenceExpression) nextQualifier).kind() == CSharpReferenceExpression.ResolveToKind.THIS || (
								(CSharpReferenceExpression) nextQualifier).kind() == CSharpReferenceExpression.ResolveToKind.BASE))
						{
							return null;
						}

						PsiElement qualifierNext = ((CSharpReferenceExpression) qualifier).resolve();
						if(qualifierNext != null)
						{
							return newBuilder(qualifier, formatElement(qualifierNext));
						}
					}
				}
				else
				{
					return null;
				}
			}

		}
		return super.checkImpl(languageVersion, highlightContext, element);
	}
}
