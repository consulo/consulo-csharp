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

import java.util.Collection;

import javax.annotation.Nonnull;

import com.intellij.codeInsight.TailType;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import consulo.annotation.access.RequiredReadAction;
import consulo.codeInsight.completion.CompletionProvider;
import consulo.csharp.ide.completion.insertHandler.CSharpTailInsertHandlerWithChar;
import consulo.csharp.ide.refactoring.util.CSharpNameSuggesterUtil;
import consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import consulo.csharp.lang.psi.CSharpIdentifier;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.resolve.DotNetPsiSearcher;

/**
 * @author VISTALL
 * @since 06.01.15
 */
public class CSharpMemberNameCompletionContributor extends CompletionContributor
{
	public CSharpMemberNameCompletionContributor()
	{
		extend(CompletionType.BASIC, StandardPatterns.psiElement(CSharpTokens.IDENTIFIER).withParent(CSharpIdentifier.class), new CompletionProvider()
		{
			@Override
			@RequiredReadAction
			public void addCompletions(@Nonnull CompletionParameters parameters, ProcessingContext context, @Nonnull CompletionResultSet result)
			{
				PsiElement position = parameters.getPosition();
				CSharpIdentifier identifier = PsiTreeUtil.getParentOfType(position, CSharpIdentifier.class);
				assert identifier != null;

				PsiElement parent = identifier.getParent();
				if(parent instanceof DotNetVariable)
				{
					DotNetVariable variable = (DotNetVariable) parent;
					Collection<String> suggestedNames = CSharpNameSuggesterUtil.getSuggestedVariableNames(variable);

					for(String suggestedName : suggestedNames)
					{
						LookupElementBuilder element = LookupElementBuilder.create(suggestedName);
						element = element.withInsertHandler(new CSharpTailInsertHandlerWithChar(TailType.EQ, '='));
						result.addElement(element);
					}
				}
				else if(parent instanceof CSharpTypeDeclaration)
				{
					PsiFile originalFile = parameters.getOriginalFile();
					VirtualFile virtualFile = originalFile.getVirtualFile();
					assert virtualFile != null;
					String nameWithoutExtension = virtualFile.getNameWithoutExtension();
					if(!nameWithoutExtension.contains("."))
					{
						String parentQName = ((CSharpTypeDeclaration) parent).getPresentableParentQName();
						String qName = nameWithoutExtension;
						if(!StringUtil.isEmpty(parentQName))
						{
							qName = parentQName + "." + nameWithoutExtension;
						}

						DotNetTypeDeclaration type = DotNetPsiSearcher.getInstance(originalFile.getProject()).findType(qName, originalFile.getResolveScope());
						if(type != null)
						{
							return;
						}

						LookupElementBuilder element = LookupElementBuilder.create(nameWithoutExtension);
						result.addElement(element);
					}
				}
				else if(parent instanceof CSharpConstructorDeclaration)
				{
					PsiElement maybeType = parent.getParent();
					if(maybeType instanceof CSharpTypeDeclaration)
					{
						String name = ((CSharpTypeDeclaration) maybeType).getName();
						if(name == null)
						{
							return;
						}

						LookupElementBuilder element = LookupElementBuilder.create(name);
						result.addElement(element);
					}
				}
			}
		});
	}
}
