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

package org.mustbe.consulo.csharp.ide.completion;

import static com.intellij.patterns.StandardPatterns.psiElement;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpFieldDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Consumer;
import com.intellij.util.ProcessingContext;

/**
 * @author VISTALL
 * @since 24.12.14
 */
public abstract class CSharpMemberAddByCompletionContributor extends CompletionContributor
{
	public CSharpMemberAddByCompletionContributor()
	{
		extend(CompletionType.BASIC, psiElement().withSuperParent(4, CSharpTypeDeclaration.class), new DumbCompletionProvider()
		{
			@RequiredReadAction
			@Override
			protected void addLookupElements(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result)
			{
				DotNetQualifiedElement currentElement = PsiTreeUtil.getParentOfType(parameters.getPosition(), DotNetQualifiedElement.class);
				assert currentElement != null;

				// if we not field - return
				if(!(currentElement instanceof CSharpFieldDeclaration))
				{
					return;
				}

				CSharpTypeDeclaration typeDeclaration = PsiTreeUtil.getParentOfType(parameters.getPosition(), CSharpTypeDeclaration.class);
				assert typeDeclaration != null;

				processCompletion(parameters, context, CSharpCompletionSorting.modifyResultSet(parameters, result), typeDeclaration);
			}
		});
	}

	@RequiredReadAction
	public abstract void processCompletion(@NotNull CompletionParameters parameters,
			@NotNull ProcessingContext context,
			@NotNull Consumer<LookupElement> result,
			@NotNull CSharpTypeDeclaration typeDeclaration);
}
