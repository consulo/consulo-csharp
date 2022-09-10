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

package consulo.csharp.ide.completion;

import javax.annotation.Nonnull;

import consulo.language.editor.completion.*;
import consulo.language.psi.PsiElement;
import consulo.language.util.ProcessingContext;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.ide.completion.patterns.CSharpPatterns;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpReferenceExpressionEx;
import consulo.csharp.lang.psi.CSharpSoftTokens;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.csharp.module.extension.CSharpModuleUtil;

/**
 * @author VISTALL
 * @since 03.01.15
 */
class CSharpLinqCompletionContributor
{
	static void extend(CompletionContributor contributor)
	{
		contributor.extend(CompletionType.BASIC, CSharpPatterns.expressionStart(), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			public void addCompletions(@Nonnull CompletionParameters parameters, ProcessingContext context, @Nonnull CompletionResultSet result)
			{
				PsiElement position = parameters.getPosition();
				if(!CSharpModuleUtil.findLanguageVersion(position).isAtLeast(CSharpLanguageVersion._3_0))
				{
					return;
				}
				CSharpReferenceExpressionEx parent = (CSharpReferenceExpressionEx) position.getParent();
				if(parent.getQualifier() != null || parent.kind() != CSharpReferenceExpression.ResolveToKind.ANY_MEMBER)
				{
					return;
				}

				CSharpCompletionUtil.elementToLookup(result, CSharpSoftTokens.FROM_KEYWORD, null, null);
			}
		});
	}
}
