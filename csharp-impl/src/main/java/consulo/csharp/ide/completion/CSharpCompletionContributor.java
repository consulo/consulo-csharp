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

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.lang.CSharpLanguage;
import consulo.language.Language;
import consulo.language.editor.completion.*;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 28-Oct-17
 */
@ExtensionImpl
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
	public void beforeCompletion(@Nonnull CompletionInitializationContext context)
	{
		context.setDummyIdentifier(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED);
	}

	@RequiredReadAction
	@Override
	public void fillCompletionVariants(CompletionParameters parameters, CompletionResultSet result)
	{
		CompletionResultSet resultSet = CSharpCompletionSorting.modifyResultSet(parameters, result);

		super.fillCompletionVariants(parameters, resultSet);

		CSharpNoVariantsDelegator.fillCompletionVariants(parameters, resultSet);
	}

	@Nonnull
	@Override
	public Language getLanguage()
	{
		return CSharpLanguage.INSTANCE;
	}
}
