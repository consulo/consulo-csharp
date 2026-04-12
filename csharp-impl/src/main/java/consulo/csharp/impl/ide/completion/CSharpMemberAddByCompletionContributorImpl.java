/*
 * Copyright 2013-2026 consulo.io
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

package consulo.csharp.impl.ide.completion;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.editor.completion.CSharpMemberAddByCompletionContributor;
import consulo.csharp.impl.ide.completion.patterns.CSharpPatterns;
import consulo.csharp.lang.impl.psi.UsefulPsiTreeUtil;
import consulo.csharp.lang.psi.CSharpFieldDeclaration;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.document.util.TextRange;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.language.editor.completion.*;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.ProcessingContext;
import consulo.util.lang.StringUtil;

import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 2026-04-12
 */
public class CSharpMemberAddByCompletionContributorImpl {
    static void extend(CompletionContributor contributor) {
        contributor.extend(CompletionType.BASIC, CSharpPatterns.fieldStart(), new CompletionProvider() {
            @RequiredReadAction
            @Override
            public void addCompletions(CompletionParameters parameters, ProcessingContext context, CompletionResultSet result) {
                CSharpFieldDeclaration currentElement = PsiTreeUtil.getParentOfType(parameters.getPosition(), CSharpFieldDeclaration.class);
                assert currentElement != null;

                DotNetModifierList modifierList = currentElement.getModifierList();
                if (modifierList != null) {
                    int textLength = modifierList.getTextLength();
                    if (textLength > 0) {
                        return;
                    }
                }

                PsiElement nextSibling = UsefulPsiTreeUtil.getNextSiblingSkippingWhiteSpacesAndComments(currentElement);
                TextRange textRange = nextSibling == null ? null : new TextRange(currentElement.getTextRange().getStartOffset(), nextSibling.getTextRange().getStartOffset());
                if (textRange != null && !StringUtil.containsLineBreak(textRange.subSequence(currentElement.getContainingFile().getText()))) {
                    return;
                }

                CSharpTypeDeclaration typeDeclaration = PsiTreeUtil.getParentOfType(parameters.getPosition(), CSharpTypeDeclaration.class);

                if (typeDeclaration == null) {
                    return;
                }

                Consumer<LookupElement> delegate = lookupElement ->
                {
                    if (lookupElement != null) {
                        CSharpCompletionSorting.force(lookupElement, CSharpCompletionSorting.KindSorter.Type.overrideMember);
                        result.accept(lookupElement);
                    }
                };

                for (CSharpMemberAddByCompletionContributor completionContributor : CSharpMemberAddByCompletionContributor.EP_NAME.getExtensionList()) {
                    completionContributor.processCompletion(parameters, context, delegate, typeDeclaration);
                }
            }
        });
    }
}
