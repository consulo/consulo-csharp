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

import java.util.Collections;

import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.codeInsight.actions.AddUsingAction;
import org.mustbe.consulo.csharp.ide.completion.item.ReplaceableTypeLikeLookupElement;
import org.mustbe.consulo.csharp.ide.completion.util.LtGtInsertHandler;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpUsingList;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.dotnet.libraryAnalyzer.NamespaceReference;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetShortNameSearcher;
import org.mustbe.consulo.dotnet.resolve.GlobalSearchScopeFilter;
import org.mustbe.dotnet.msil.decompiler.util.MsilHelper;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.completion.PrefixMatcher;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.ide.IconDescriptorUpdaters;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Consumer;
import com.intellij.util.Function;
import com.intellij.util.Processor;
import com.intellij.util.indexing.IdFilter;

/**
 * @author VISTALL
 * @since 27.07.2015
 */
public class CSharpTypeReferenceCompletionContributor
{
	public static void addTypesForUsing(final CompletionParameters parameters,
			final CompletionResultSet result,
			final InheritorsHolder inheritorsHolder)
	{
		final PrefixMatcher matcher = result.getPrefixMatcher();
		CSharpReferenceExpression parent = (CSharpReferenceExpression) parameters.getPosition().getParent();

		final Project project = parent.getProject();
		final GlobalSearchScope resolveScope = parent.getResolveScope();

		final DotNetShortNameSearcher shortNameSearcher = DotNetShortNameSearcher.getInstance(project);
		final IdFilter projectIdFilter = new GlobalSearchScopeFilter(resolveScope);

		final boolean insideUsingList = PsiTreeUtil.getParentOfType(parent, CSharpUsingList.class) != null;

		shortNameSearcher.collectTypeNames(new Processor<String>()
		{
			@Override
			public boolean process(String key)
			{
				ProgressManager.checkCanceled();

				if(matcher.prefixMatches(key))
				{
					shortNameSearcher.collectTypes(key, resolveScope, projectIdFilter,
							new Processor<DotNetTypeDeclaration>()
					{
						@Override
						@RequiredReadAction
						public boolean process(DotNetTypeDeclaration typeDeclaration)
						{
							ProgressManager.checkCanceled();

							if(inheritorsHolder.alreadyProcessed(typeDeclaration))
							{
								return true;
							}

							consumeType(parameters, result, insideUsingList, typeDeclaration);

							return true;
						}
					});
				}
				return true;
			}
		}, resolveScope, projectIdFilter);
	}

	@RequiredReadAction
	private static void consumeType(final CompletionParameters completionParameters,
			Consumer<LookupElement> consumer,
			boolean insideUsingList,
			DotNetTypeDeclaration someType)
	{
		final String parentQName = someType.getPresentableParentQName();
		if(StringUtil.isEmpty(parentQName))
		{
			return;
		}

		String presentationText = MsilHelper.cutGenericMarker(someType.getName());

		int genericCount = 0;
		DotNetGenericParameter[] genericParameters = someType.getGenericParameters();
		if((genericCount = genericParameters.length) > 0)
		{
			presentationText += "<" + StringUtil.join(genericParameters, new Function<DotNetGenericParameter, String>()
			{
				@Override
				public String fun(DotNetGenericParameter parameter)
				{
					return parameter.getName();
				}
			}, ", ");
			presentationText += ">";
		}

		String lookupString = insideUsingList ? someType.getPresentableQName() : someType.getName();
		if(lookupString == null)
		{
			return;
		}
		lookupString = MsilHelper.cutGenericMarker(lookupString);

		DotNetQualifiedElement targetElementForLookup = someType;
		CSharpMethodDeclaration methodDeclaration = someType.getUserData(CSharpResolveUtil.DELEGATE_METHOD_TYPE);
		if(methodDeclaration != null)
		{
			targetElementForLookup = methodDeclaration;
		}
		LookupElementBuilder builder = LookupElementBuilder.create(targetElementForLookup, lookupString);
		builder = builder.withPresentableText(presentationText);
		builder = builder.withIcon(IconDescriptorUpdaters.getIcon(targetElementForLookup,
				Iconable.ICON_FLAG_VISIBILITY));

		builder = builder.withTypeText(parentQName, true);
		final InsertHandler<LookupElement> ltGtInsertHandler = genericCount == 0 ? null : LtGtInsertHandler
				.getInstance(genericCount > 0);
		if(insideUsingList)
		{
			builder = builder.withInsertHandler(ltGtInsertHandler);
		}
		else
		{
			builder = builder.withInsertHandler(new InsertHandler<LookupElement>()
			{
				@Override
				public void handleInsert(InsertionContext context, LookupElement item)
				{
					if(ltGtInsertHandler != null)
					{
						ltGtInsertHandler.handleInsert(context, item);
					}

					new AddUsingAction(completionParameters.getEditor(), context.getFile(),
							Collections.<NamespaceReference>singleton(new NamespaceReference(parentQName,
									null))).execute();
				}
			});
		}

		consumer.consume(new ReplaceableTypeLikeLookupElement(builder));
	}
}
