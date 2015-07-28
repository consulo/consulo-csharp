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

import java.util.Collections;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.codeInsight.actions.AddUsingAction;
import org.mustbe.consulo.csharp.ide.completion.util.LtGtInsertHandler;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpUsingList;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpReferenceExpressionImplUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.CSharpResolveOptions;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.ExecuteTarget;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.MemberResolveScopeProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.StubScopeProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.overrideSystem.OverrideProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.csharp.lang.psi.resolve.MemberByNameSelector;
import org.mustbe.consulo.dotnet.libraryAnalyzer.NamespaceReference;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetShortNameSearcher;
import org.mustbe.consulo.dotnet.resolve.GlobalSearchScopeFilter;
import org.mustbe.dotnet.msil.decompiler.util.MsilHelper;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.completion.PrefixMatcher;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.ide.IconDescriptorUpdaters;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Couple;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.ResolveState;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.CommonProcessors;
import com.intellij.util.Consumer;
import com.intellij.util.Function;
import com.intellij.util.ProcessingContext;
import com.intellij.util.Processor;
import com.intellij.util.indexing.IdFilter;
import lombok.val;

/**
 * @author VISTALL
 * @since 27.07.2015
 */
public class CSharpTypeReferenceCompletionContributor extends CompletionContributor
{
	public CSharpTypeReferenceCompletionContributor()
	{
		extend(CompletionType.BASIC, psiElement(CSharpTokens.IDENTIFIER).withParent(CSharpReferenceExpression.class),
				new CompletionProvider<CompletionParameters>()
		{
			@Override
			@RequiredReadAction
			protected void addCompletions(@NotNull final CompletionParameters completionParameters,
					ProcessingContext processingContext,
					@NotNull final CompletionResultSet completionResultSet)
			{
				val parent = (CSharpReferenceExpression) completionParameters.getPosition().getParent();
				if(parent.getQualifier() != null)
				{
					return;
				}

				CSharpReferenceExpression.ResolveToKind kind = parent.kind();
				if(kind == CSharpReferenceExpression.ResolveToKind.TYPE_LIKE ||
						kind == CSharpReferenceExpression.ResolveToKind.CONSTRUCTOR ||
						kind == CSharpReferenceExpression.ResolveToKind.ANY_MEMBER)
				{
					final PrefixMatcher matcher = completionResultSet.getPrefixMatcher();

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
								shortNameSearcher.collectTypes(key, resolveScope, projectIdFilter, new Processor<DotNetTypeDeclaration>()
								{
									@Override
									@RequiredReadAction
									public boolean process(DotNetTypeDeclaration typeDeclaration)
									{
										ProgressManager.checkCanceled();

										consumeType(completionParameters, completionResultSet, parent, insideUsingList, typeDeclaration);

										return true;
									}
								});
							}
							return true;
						}
					}, resolveScope, projectIdFilter);

				}
			}
		});
	}

	@RequiredReadAction
	private static void consumeType(final CompletionParameters completionParameters,
			Consumer<LookupElement> consumer,
			CSharpReferenceExpression parent,
			boolean insideUsingList,
			DotNetTypeDeclaration maybeMsilType)
	{
		if(!insideUsingList && isAlreadyResolved(maybeMsilType, parent))
		{
			return;
		}

		String presentationText = MsilHelper.cutGenericMarker(maybeMsilType.getName());

		int genericCount = 0;
		DotNetGenericParameter[] genericParameters = maybeMsilType.getGenericParameters();
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

		String lookupString = insideUsingList ? maybeMsilType.getPresentableQName() : maybeMsilType.getName();
		if(lookupString == null)
		{
			return;
		}
		lookupString = MsilHelper.cutGenericMarker(lookupString);

		LookupElementBuilder builder = LookupElementBuilder.create(maybeMsilType, lookupString);
		builder = builder.withPresentableText(presentationText);
		builder = builder.withIcon(IconDescriptorUpdaters.getIcon(maybeMsilType, Iconable.ICON_FLAG_VISIBILITY));

		final String parentQName = maybeMsilType.getPresentableParentQName();
		builder = builder.withTypeText(parentQName, true);
		final InsertHandler<LookupElement> ltGtInsertHandler = genericCount == 0 ? null : LtGtInsertHandler.getInstance(genericCount > 0);
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
							Collections.<NamespaceReference>singleton(new NamespaceReference(parentQName, null))).execute();
				}
			});
		}

		consumer.consume(builder);
	}

	@RequiredReadAction
	public static boolean isAlreadyResolved(DotNetQualifiedElement element, PsiElement parent)
	{
		String parentQName = element.getPresentableParentQName();
		if(StringUtil.isEmpty(parentQName))
		{
			return true;
		}

		ResolveState resolveState = ResolveState.initial();
		resolveState = resolveState.put(CSharpResolveUtil.SELECTOR, new MemberByNameSelector(MsilHelper.cutGenericMarker(element.getName())));

		Couple<PsiElement> resolveLayers = CSharpReferenceExpressionImplUtil.getResolveLayers(parent, false);
		//PsiElement last = resolveLayers.getFirst();
		PsiElement targetToWalkChildren = resolveLayers.getSecond();

		CSharpResolveOptions options = CSharpResolveOptions.build();
		options.kind(CSharpReferenceExpression.ResolveToKind.TYPE_LIKE);
		options.element(element);

		StubScopeProcessor p = new MemberResolveScopeProcessor(element, CommonProcessors.<ResolveResult>alwaysFalse(), new ExecuteTarget[]{
				ExecuteTarget.GENERIC_PARAMETER,
				ExecuteTarget.TYPE,
				ExecuteTarget.DELEGATE_METHOD,
				ExecuteTarget.NAMESPACE,
				ExecuteTarget.TYPE_DEF
		}, OverrideProcessor.ALWAYS_TRUE);

		if(!CSharpResolveUtil.walkChildren(p, targetToWalkChildren, true, true, resolveState))
		{
			return true;
		}

		if(!CSharpResolveUtil.walkGenericParameterList(p, CommonProcessors.<ResolveResult>alwaysTrue(), element, null, resolveState))
		{
			return true;
		}

		return !CSharpResolveUtil.walkUsing(p, parent, null, resolveState);
	}
}
