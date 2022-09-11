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

package consulo.csharp.impl.ide.liveTemplates.macro;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.ApplicationManager;
import consulo.csharp.impl.ide.lineMarkerProvider.CSharpLineMarkerUtil;
import consulo.csharp.impl.ide.refactoring.util.CSharpNameSuggesterUtil;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.dotnet.psi.DotNetVariable;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.language.editor.template.Expression;
import consulo.language.editor.template.ExpressionContext;
import consulo.language.editor.template.Result;
import consulo.language.editor.template.TextResult;
import consulo.language.editor.template.macro.Macro;
import consulo.language.psi.*;
import consulo.language.util.IncorrectOperationException;
import consulo.logging.Logger;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.util.collection.ContainerUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author VISTALL
 * @since 11.06.14
 */
@ExtensionImpl
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
					try
					{
						ReparseRangeUtil.reparseRange(fileCopy, offset, offset, "xxx");
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
