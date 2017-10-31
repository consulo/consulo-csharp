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

import org.jetbrains.annotations.NotNull;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Consumer;
import com.intellij.util.ProcessingContext;
import consulo.annotations.RequiredReadAction;
import consulo.codeInsight.completion.CompletionProvider;
import consulo.csharp.ide.completion.patterns.CSharpPatterns;
import consulo.csharp.lang.psi.CSharpFieldDeclaration;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.psi.UsefulPsiTreeUtil;
import consulo.dotnet.psi.DotNetModifierList;

/**
 * @author VISTALL
 * @since 24.12.14
 */
public interface CSharpMemberAddByCompletionContributor
{
	ExtensionPointName<CSharpMemberAddByCompletionContributor> EP_NAME = ExtensionPointName.create("consulo.csharp.memberAddByCompletionContributor");

	static void extend(CompletionContributor contributor)
	{
		contributor.extend(CompletionType.BASIC, CSharpPatterns.field(), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			public void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result)
			{
				CSharpFieldDeclaration currentElement = PsiTreeUtil.getParentOfType(parameters.getPosition(), CSharpFieldDeclaration.class);
				assert currentElement != null;

				DotNetModifierList modifierList = currentElement.getModifierList();
				if(modifierList != null)
				{
					int textLength = modifierList.getTextLength();
					if(textLength > 0)
					{
						return;
					}
				}

				PsiElement nextSibling = UsefulPsiTreeUtil.getNextSiblingSkippingWhiteSpacesAndComments(currentElement);
				TextRange textRange = nextSibling == null ? null : new TextRange(currentElement.getTextRange().getEndOffset(), nextSibling.getTextRange().getStartOffset());
				if(textRange != null && !StringUtil.containsLineBreak(textRange.subSequence(currentElement.getContainingFile().getText())))
				{
					return;
				}

				CSharpTypeDeclaration typeDeclaration = PsiTreeUtil.getParentOfType(parameters.getPosition(), CSharpTypeDeclaration.class);

				assert typeDeclaration != null;

				Consumer<LookupElement> delegate = lookupElement ->
				{
					if(lookupElement != null)
					{
						CSharpCompletionSorting.force(lookupElement, CSharpCompletionSorting.KindSorter.Type.overrideMember);
						result.consume(lookupElement);
					}
				};

				for(CSharpMemberAddByCompletionContributor completionContributor : EP_NAME.getExtensions())
				{
					completionContributor.processCompletion(parameters, context, delegate, typeDeclaration);
				}
			}
		});
	}

	@RequiredReadAction
	void processCompletion(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull Consumer<LookupElement> result, @NotNull CSharpTypeDeclaration typeDeclaration);
}
