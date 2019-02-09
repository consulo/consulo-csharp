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

package consulo.csharp.ide.liveTemplates.macro;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.template.Expression;
import com.intellij.codeInsight.template.ExpressionContext;
import com.intellij.codeInsight.template.Macro;
import com.intellij.codeInsight.template.Result;
import com.intellij.codeInsight.template.TextResult;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.text.BlockSupport;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.ContainerUtil;
import consulo.ui.RequiredUIAccess;
import consulo.csharp.ide.lineMarkerProvider.CSharpLineMarkerUtil;
import consulo.csharp.ide.refactoring.util.CSharpNameSuggesterUtil;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.dotnet.psi.DotNetVariable;

/**
 * @author VISTALL
 * @since 11.06.14
 */
public class SuggestVariableNameMacro extends Macro
{
	private static final Logger LOGGER = Logger.getInstance(SuggestVariableNameMacro.class);

	@Override
	public String getName()
	{
		return "csharpSuggestVariableName";
	}

	@Override
	public String getPresentableName()
	{
		return "suggestVariableName variable";
	}

	@Nonnull
	@Override
	public String getDefaultValue()
	{
		return "it";
	}

	@Nullable
	@Override
	@RequiredUIAccess
	public Result calculateQuickResult(@Nonnull Expression[] params, ExpressionContext context)
	{
		return calculateResult(params, context);
	}

	@Nullable
	@Override
	@RequiredUIAccess
	public LookupElement[] calculateLookupItems(@Nonnull Expression[] params, ExpressionContext context)
	{
		Collection<String> suggestedVariableNames = getSuggestedVariableNames(context);

		List<LookupElement> list = new ArrayList<LookupElement>(suggestedVariableNames.size());
		for(String temp : suggestedVariableNames)
		{
			list.add(LookupElementBuilder.create(temp));
		}
		return list.toArray(new LookupElement[list.size()]);
	}

	@Nullable
	@Override
	@RequiredUIAccess
	public Result calculateResult(@Nonnull Expression[] params, ExpressionContext context)
	{
		Collection<String> suggestedVariableNames = getSuggestedVariableNames(context);
		return new TextResult(ContainerUtil.getFirstItem(suggestedVariableNames, "it"));
	}

	@Nonnull
	@RequiredUIAccess
	private Collection<String> getSuggestedVariableNames(ExpressionContext context)
	{
		final Project project = context.getProject();
		final int offset = context.getStartOffset();

		PsiDocumentManager.getInstance(project).commitAllDocuments();

		PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(context.getEditor().getDocument());
		assert file != null;
		PsiElement element = file.findElementAt(offset);

		assert element != null;
		if(PsiUtilCore.getElementType(element) == CSharpTokens.IDENTIFIER)
		{
			DotNetVariable variable = CSharpLineMarkerUtil.getNameIdentifierAs(element, DotNetVariable.class);
			if(variable != null)
			{
				return CSharpNameSuggesterUtil.getSuggestedVariableNames(variable);
			}
		}
		else
		{
			final PsiFile fileCopy = (PsiFile) file.copy();
			ApplicationManager.getApplication().runWriteAction(new Runnable()
			{
				@Override
				public void run()
				{
					BlockSupport blockSupport = BlockSupport.getInstance(project);
					try
					{
						blockSupport.reparseRange(fileCopy, offset, offset, "xxx");
					}
					catch(IncorrectOperationException e)
					{
						SuggestVariableNameMacro.LOGGER.error(e);
					}
				}
			});
			PsiElement identifierCopy = fileCopy.findElementAt(offset);
			DotNetVariable variable = CSharpLineMarkerUtil.getNameIdentifierAs(identifierCopy, DotNetVariable.class);
			if(variable != null)
			{
				return CSharpNameSuggesterUtil.getSuggestedVariableNames(variable);
			}
		}
		return Collections.emptyList();
	}
}
