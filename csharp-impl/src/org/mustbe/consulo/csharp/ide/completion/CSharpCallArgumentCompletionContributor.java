/*
 * Copyright 2013-2015 must-be.org
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

import gnu.trove.THashSet;

import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgument;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import org.mustbe.consulo.csharp.lang.psi.CSharpNamedCallArgument;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpSimpleLikeMethodAsElement;
import org.mustbe.consulo.csharp.lang.psi.CSharpSimpleParameterInfo;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 22.08.2015
 */
public class CSharpCallArgumentCompletionContributor extends CompletionContributor
{
	public CSharpCallArgumentCompletionContributor()
	{
		extend(CompletionType.BASIC, psiElement(CSharpTokens.IDENTIFIER).withParent(CSharpReferenceExpression.class)
				.withSuperParent(2, CSharpCallArgument.class), new CompletionProvider<CompletionParameters>()
		{
			@RequiredReadAction
			@Override
			protected void addCompletions(@NotNull CompletionParameters parameters,
					ProcessingContext context,
					@NotNull CompletionResultSet result)
			{
				CSharpReferenceExpression referenceExpression = (CSharpReferenceExpression) parameters.getPosition()
						.getParent();

				CSharpCallArgument callArgument = (CSharpCallArgument) referenceExpression.getParent();
				if(callArgument instanceof CSharpNamedCallArgument)
				{
					return;
				}

				CSharpCallArgumentListOwner argumentListOwner = PsiTreeUtil.getParentOfType(referenceExpression,
						CSharpCallArgumentListOwner.class);

				assert argumentListOwner != null;
				ResolveResult[] resolveResults = argumentListOwner.multiResolve(false);

				boolean visitedNotNamed = false;
				Set<String> alreadyDeclared = new THashSet<String>(5);
				CSharpCallArgument[] callArguments = argumentListOwner.getCallArguments();
				for(CSharpCallArgument c : callArguments)
				{
					if(c == callArgument)
					{
						continue;
					}
					if(c instanceof CSharpNamedCallArgument)
					{
						alreadyDeclared.add(((CSharpNamedCallArgument) c).getName());
					}
					else
					{
						visitedNotNamed = true;
					}
				}
				int thisCallArgumentPosition = visitedNotNamed ? ArrayUtil.indexOf(callArguments, callArgument) : -1;

				Set<String> wantToCompleteParameters = new THashSet<String>();
				for(ResolveResult resolveResult : resolveResults)
				{
					PsiElement element = resolveResult.getElement();
					if(element instanceof CSharpSimpleLikeMethodAsElement)
					{
						CSharpSimpleParameterInfo[] parameterInfos = ((CSharpSimpleLikeMethodAsElement) element)
								.getParameterInfos();

						if(thisCallArgumentPosition != -1)
						{
							if(parameterInfos.length > thisCallArgumentPosition)
							{
								for(int i = thisCallArgumentPosition; i < parameterInfos.length; i++)
								{
									CSharpSimpleParameterInfo parameterInfo = parameterInfos[i];
									ContainerUtil.addIfNotNull(wantToCompleteParameters, parameterInfo.getName());
								}
							}
						}
						else
						{
							for(CSharpSimpleParameterInfo parameterInfo : parameterInfos)
							{
								ContainerUtil.addIfNotNull(wantToCompleteParameters, parameterInfo.getName());
							}
						}
					}
				}

				wantToCompleteParameters.removeAll(alreadyDeclared);

				for(String wantToCompleteParameter : wantToCompleteParameters)
				{
					LookupElementBuilder builder = LookupElementBuilder.create(wantToCompleteParameter + ": ");
					builder = builder.withIcon(AllIcons.Nodes.Parameter);

					result.consume(builder);
				}
			}
		});
	}
}
