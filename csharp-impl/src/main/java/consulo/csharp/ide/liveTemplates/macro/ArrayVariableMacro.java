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

import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpArrayTypeRef;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.editor.template.Expression;
import consulo.language.editor.template.ExpressionContext;
import consulo.language.psi.PsiElement;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 30.12.14
 */
public class ArrayVariableMacro extends VariableTypeMacroBase
{
	@Nullable
	@Override
	protected PsiElement[] getVariables(Expression[] params, ExpressionContext context)
	{
		final PsiElement psiElementAtStartOffset = context.getPsiElementAtStartOffset();
		if(psiElementAtStartOffset == null)
		{
			return PsiElement.EMPTY_ARRAY;
		}

		List<DotNetVariable> variables = CSharpLiveTemplateMacroUtil.resolveAllVariables(context.getPsiElementAtStartOffset());

		List<DotNetVariable> list = new ArrayList<>();
		for(DotNetVariable variable : variables)
		{
			DotNetTypeRef typeRefOfVariable = variable.toTypeRef(true);

			if(typeRefOfVariable instanceof CSharpArrayTypeRef && ((CSharpArrayTypeRef) typeRefOfVariable).getDimensions() == 0)
			{
				list.add(variable);
			}
		}
		return list.toArray(new PsiElement[list.size()]);
	}

	@Override
	public String getName()
	{
		return "csharpArrayVariable";
	}

	@Override
	public String getPresentableName()
	{
		return "array variable";
	}
}
