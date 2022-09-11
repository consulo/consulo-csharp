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
import consulo.csharp.impl.ide.lineMarkerProvider.CSharpLineMarkerUtil;
import consulo.dotnet.psi.DotNetVariable;
import consulo.language.editor.template.Expression;
import consulo.language.editor.template.ExpressionContext;
import consulo.language.editor.template.Result;
import consulo.language.editor.template.TextResult;
import consulo.language.editor.template.macro.Macro;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @author VISTALL
 * @since 29.12.14
 */
@ExtensionImpl
public class SuggestIndexVariableNameMacro extends Macro
{
	@Override
	@Nonnull
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
	@RequiredUIAccess
	public Result calculateResult(@Nonnull Expression[] params, ExpressionContext context)
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
