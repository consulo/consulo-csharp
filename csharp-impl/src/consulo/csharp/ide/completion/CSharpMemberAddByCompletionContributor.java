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

import static com.intellij.patterns.StandardPatterns.psiElement;

import org.jetbrains.annotations.NotNull;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Consumer;
import com.intellij.util.ProcessingContext;
import consulo.annotations.RequiredReadAction;
import consulo.codeInsight.completion.CompletionProvider;
import consulo.csharp.lang.psi.CSharpFieldDeclaration;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetQualifiedElement;

/**
 * @author VISTALL
 * @since 24.12.14
 */
public abstract class CSharpMemberAddByCompletionContributor extends CompletionContributor
{
	public CSharpMemberAddByCompletionContributor()
	{
		extend(CompletionType.BASIC, psiElement().withSuperParent(4, CSharpTypeDeclaration.class), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			public void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result)
			{
				DotNetQualifiedElement currentElement = PsiTreeUtil.getParentOfType(parameters.getPosition(), DotNetQualifiedElement.class);
				assert currentElement != null;

				// if we not field - return
				if(!(currentElement instanceof CSharpFieldDeclaration))
				{
					return;
				}

				DotNetModifierList modifierList = ((CSharpFieldDeclaration) currentElement).getModifierList();
				if(modifierList != null)
				{
					int textLength = modifierList.getTextLength();
					if(textLength > 0)
					{
						return;
					}
				}

				CSharpTypeDeclaration typeDeclaration = PsiTreeUtil.getParentOfType(parameters.getPosition(), CSharpTypeDeclaration.class);
				assert typeDeclaration != null;

				final CompletionResultSet delegateResultSet = CSharpCompletionSorting.modifyResultSet(parameters, result);
				Consumer<LookupElement> delegate = lookupElement ->
				{
					if(lookupElement != null)
					{
						CSharpCompletionSorting.force(lookupElement, CSharpCompletionSorting.KindSorter.Type.overrideMember);
						delegateResultSet.consume(lookupElement);
					}
				};

				processCompletion(parameters, context, delegate, typeDeclaration);
			}
		});
	}

	@RequiredReadAction
	public abstract void processCompletion(@NotNull CompletionParameters parameters,
			@NotNull ProcessingContext context,
			@NotNull Consumer<LookupElement> result,
			@NotNull CSharpTypeDeclaration typeDeclaration);
}
