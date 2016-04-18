/*
 * Copyright 2013-2016 must-be.org
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

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredDispatchThread;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.completion.util.SpaceInsertHandler;
import org.mustbe.consulo.csharp.lang.psi.CSharpSoftTokens;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterListOwner;
import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionInitializationContext;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.NotNullPairFunction;

/**
 * @author VISTALL
 * @since 18.04.2016
 */
public class CSharpWhereCompletionContributor extends CompletionContributor
{
	@Override
	public void beforeCompletion(@NotNull CompletionInitializationContext context)
	{
		context.setDummyIdentifier("where ");
	}

	@RequiredReadAction
	@Override
	public void fillCompletionVariants(CompletionParameters parameters, CompletionResultSet result)
	{
		result = CSharpCompletionSorting.modifyResultSet(parameters, result);

		PsiElement position = parameters.getPosition();
		if(PsiUtilCore.getElementType(position) != CSharpSoftTokens.WHERE_KEYWORD)
		{
			return;
		}

		DotNetGenericParameterListOwner listOwner = PsiTreeUtil.getParentOfType(position, DotNetGenericParameterListOwner.class);
		if(listOwner == null || listOwner.getGenericParametersCount() == 0)
		{
			return;
		}

		CSharpCompletionUtil.elementToLookup(result, CSharpSoftTokens.WHERE_KEYWORD, new NotNullPairFunction<LookupElementBuilder, IElementType, LookupElement>()
		{
			@NotNull
			@Override
			public LookupElement fun(LookupElementBuilder lookupElementBuilder, IElementType iElementType)
			{
				lookupElementBuilder = lookupElementBuilder.withInsertHandler(new InsertHandler<LookupElement>()
				{
					@Override
					@RequiredDispatchThread
					public void handleInsert(InsertionContext context, LookupElement item)
					{
						SpaceInsertHandler.INSTANCE.handleInsert(context, item);

						AutoPopupController.getInstance(context.getProject()).scheduleAutoPopup(context.getEditor());
					}
				});
				return lookupElementBuilder;
			}
		}, null);
	}
}
