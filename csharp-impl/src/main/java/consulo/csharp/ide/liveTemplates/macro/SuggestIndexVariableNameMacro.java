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

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredDispatchThread;
import consulo.csharp.ide.lineMarkerProvider.CSharpLineMarkerUtil;
import consulo.dotnet.psi.DotNetVariable;
import com.intellij.codeInsight.template.Expression;
import com.intellij.codeInsight.template.ExpressionContext;
import com.intellij.codeInsight.template.Macro;
import com.intellij.codeInsight.template.Result;
import com.intellij.codeInsight.template.TextResult;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

/**
 * @author VISTALL
 * @since 29.12.14
 */
public class SuggestIndexVariableNameMacro extends Macro
{
	@Override
	@NotNull
	public String getDefaultValue()
	{
		return "i";
	}

	@Override
	public String getName()
	{
		return "csharpSuggestIndexName";
	}

	@Override
	public String getPresentableName()
	{
		return "csharpSuggestIndexName()";
	}

	@Nullable
	@Override
	@RequiredDispatchThread
	public Result calculateResult(@NotNull Expression[] params, ExpressionContext context)
	{
		final Project project = context.getProject();
		final int offset = context.getStartOffset();

		PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(context.getEditor().getDocument());
		PsiElement place = file.findElementAt(offset);

		List<DotNetVariable> dotNetVariables = CSharpLiveTemplateMacroUtil.resolveAllVariables(place);

		DotNetVariable variable = CSharpLineMarkerUtil.getNameIdentifierAs(place, DotNetVariable.class);

		ChooseLetterLoop:
		for(char letter = 'i'; letter <= 'z'; letter++)
		{
			for(DotNetVariable dotNetVariable : dotNetVariables)
			{
				// skip self
				if(dotNetVariable == variable)
				{
					continue;
				}

				String name = dotNetVariable.getName();
				if(name != null && name.length() == 1 && name.charAt(0) == letter)
				{
					continue ChooseLetterLoop;
				}
			}
			return new TextResult(String.valueOf(letter));
		}
		return null;
	}
}
