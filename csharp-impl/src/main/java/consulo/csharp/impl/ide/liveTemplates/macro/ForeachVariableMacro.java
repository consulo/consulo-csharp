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
import consulo.csharp.lang.impl.psi.DotNetTypes2;
import consulo.csharp.lang.impl.psi.source.CSharpTypeDeclarationImplUtil;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.editor.template.Expression;
import consulo.language.editor.template.ExpressionContext;
import consulo.language.psi.PsiElement;
import consulo.util.collection.SmartList;
import jakarta.annotation.Nullable;

import java.util.List;

/**
 * @author VISTALL
 * @since 11.06.14
 */
@ExtensionImpl
public class ForeachVariableMacro extends VariableTypeMacroBase
{
	private static final String[] ourAcceptableTypes = {
			DotNetTypes2.System.Collections.IEnumerable,
			DotNetTypes2.System.Collections.Generic.IEnumerable$1
	};

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

		List<DotNetVariable> list = new SmartList<DotNetVariable>();
		for(DotNetVariable variable : variables)
		{
			DotNetTypeRef typeRefOfVariable = variable.toTypeRef(true);

			if(CSharpTypeDeclarationImplUtil.isInheritOrSelf(typeRefOfVariable, psiElementAtStartOffset, ourAcceptableTypes))
			{
				list.add(variable);
			}
		}
		return list.toArray(new PsiElement[list.size()]);
	}

	@Override
	public String getName()
	{
		return "csharpForeachVariable";
	}

	@Override
	public String getPresentableName()
	{
		return "foreach variable";
	}
}
