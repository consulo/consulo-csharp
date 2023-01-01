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

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.lang.impl.ide.codeStyle.CSharpCodeGenerationSettings;
import consulo.csharp.lang.impl.psi.CSharpTypeRefPresentationUtil;
import consulo.csharp.lang.impl.psi.fragment.CSharpFragmentFactory;
import consulo.csharp.lang.impl.psi.fragment.CSharpFragmentFileImpl;
import consulo.csharp.lang.impl.psi.source.resolve.util.CSharpResolveUtil;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.csharp.module.extension.CSharpModuleUtil;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.language.editor.template.Expression;
import consulo.language.editor.template.ExpressionContext;
import consulo.language.editor.template.Result;
import consulo.language.editor.template.TextResult;
import consulo.language.editor.template.macro.Macro;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.util.collection.SmartList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @author VISTALL
 * @since 11.06.14
 */
@ExtensionImpl
public class ForeachComponentTypeMacro extends Macro
{
	@Override
	public String getName()
	{
		return "csharpForeachComponentType";
	}

	@Override
	public String getPresentableName()
	{
		return "csharpForeachComponentType(VARIABLE)";
	}

	@Nonnull
	@Override
	public String getDefaultValue()
	{
		return "var";
	}

	@Nullable
	@Override
	@RequiredReadAction
	public Result calculateQuickResult(@Nonnull Expression[] params, ExpressionContext context)
	{
		return calculateResult(params, context);
	}

	@Nullable
	@Override
	@RequiredReadAction
	public LookupElement[] calculateLookupItems(@Nonnull Expression[] params, ExpressionContext context)
	{
		Result result = calculateResult(params, context);
		if(result == null)
		{
			return LookupElement.EMPTY_ARRAY;
		}
		List<LookupElement> list = new SmartList<>();

		boolean useVarForExtractLocalVariable = CSharpCodeGenerationSettings.getInstance(context.getProject()).USE_VAR_FOR_EXTRACT_LOCAL_VARIABLE;
		boolean varSupported = CSharpModuleUtil.findLanguageVersion(context.getPsiElementAtStartOffset()).isAtLeast(CSharpLanguageVersion._2_0);

		boolean canUseVar = varSupported && useVarForExtractLocalVariable;

		if(canUseVar)
		{
			list.add(LookupElementBuilder.create("var").bold());
		}
		else
		{
			list.add(LookupElementBuilder.create(result.toString()));

			if(varSupported)
			{
				list.add(LookupElementBuilder.create("var").bold());
			}
		}
		return list.toArray(new LookupElement[list.size()]);
	}

	@Nullable
	@Override
	@RequiredReadAction
	public Result calculateResult(@Nonnull Expression[] params, ExpressionContext context)
	{
		if(params.length != 1)
		{
			return null;
		}
		Result result = params[0].calculateResult(context);
		if(result == null)
		{
			return null;
		}
		String text = result.toString();

		PsiElement place = context.getPsiElementAtStartOffset();
		CSharpFragmentFileImpl expressionFragment = CSharpFragmentFactory.createExpressionFragment(context.getProject(), text, place);

		DotNetExpression expression = PsiTreeUtil.getChildOfType(expressionFragment, DotNetExpression.class);

		if(expression == null)
		{
			return null;
		}

		DotNetTypeRef typeRef = CSharpResolveUtil.resolveIterableType(expression.toTypeRef(false));
		if(typeRef == DotNetTypeRef.ERROR_TYPE)
		{
			return null;
		}
		return new TextResult(CSharpTypeRefPresentationUtil.buildShortText(typeRef));
	}
}
