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
import com.intellij.codeInsight.completion.CompletionInitializationContext;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionUtilCore;
import consulo.annotations.RequiredReadAction;

/**
 * @author VISTALL
 * @since 28-Oct-17
 */
public class CSharpCompletionContributor extends CompletionContributor
{
	public CSharpCompletionContributor()
	{
		CSharpLinqCompletionContributor.extend(this);

		CSharpAccessorCompletionContributor.extend(this);

		CSharpStatementCompletionContributor.extend(this);

		CSharpKeywordCompletionContributor.extend(this);

		CSharpExpressionCompletionContributor.extend(this);

		CSharpMemberAddByCompletionContributor.extend(this);
	}

	@Override
	public void beforeCompletion(@NotNull CompletionInitializationContext context)
	{
		context.setDummyIdentifier(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED);
	}

	@RequiredReadAction
	@Override
	public void fillCompletionVariants(CompletionParameters parameters, CompletionResultSet result)
	{
		super.fillCompletionVariants(parameters, CSharpCompletionSorting.modifyResultSet(parameters, result));
	}
}
