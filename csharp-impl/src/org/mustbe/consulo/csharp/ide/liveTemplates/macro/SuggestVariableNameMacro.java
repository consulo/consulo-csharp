/*
 * Copyright 2013-2014 must-be.org
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

package org.mustbe.consulo.csharp.ide.liveTemplates.macro;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import consulo.lombok.annotations.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredDispatchThread;
import org.mustbe.consulo.csharp.ide.lineMarkerProvider.CSharpLineMarkerUtil;
import org.mustbe.consulo.csharp.ide.refactoring.util.CSharpNameSuggesterUtil;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.template.Expression;
import com.intellij.codeInsight.template.ExpressionContext;
import com.intellij.codeInsight.template.Macro;
import com.intellij.codeInsight.template.Result;
import com.intellij.codeInsight.template.TextResult;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.text.BlockSupport;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 11.06.14
 */
@Logger
public class SuggestVariableNameMacro extends Macro
{
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

	@NotNull
	@Override
	public String getDefaultValue()
	{
		return "it";
	}

	@Nullable
	@Override
	@RequiredDispatchThread
	public Result calculateQuickResult(@NotNull Expression[] params, ExpressionContext context)
	{
		return calculateResult(params, context);
	}

	@Nullable
	@Override
	@RequiredDispatchThread
	public LookupElement[] calculateLookupItems(@NotNull Expression[] params, ExpressionContext context)
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
	@RequiredDispatchThread
	public Result calculateResult(@NotNull Expression[] params, ExpressionContext context)
	{
		Collection<String> suggestedVariableNames = getSuggestedVariableNames(context);
		return new TextResult(ContainerUtil.getFirstItem(suggestedVariableNames, "it"));
	}

	@NotNull
	@RequiredDispatchThread
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
						LOGGER.error(e);
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
