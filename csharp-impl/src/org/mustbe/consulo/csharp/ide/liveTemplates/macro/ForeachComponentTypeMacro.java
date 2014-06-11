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

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpExpressionFragmentFactory;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpExpressionFragmentFileImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.csharp.module.extension.CSharpModuleUtil;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.template.Expression;
import com.intellij.codeInsight.template.ExpressionContext;
import com.intellij.codeInsight.template.Macro;
import com.intellij.codeInsight.template.Result;
import com.intellij.codeInsight.template.TextResult;
import com.intellij.util.SmartList;

/**
 * @author VISTALL
 * @since 11.06.14
 */
public class ForeachComponentTypeMacro extends Macro
{
	@Override
	public String getName()
	{
		return "foreachComponentType";
	}

	@Override
	public String getPresentableName()
	{
		return "foreachComponentType(VARIABLE)";
	}

	@NotNull
	@Override
	public String getDefaultValue()
	{
		return "var";
	}

	@Nullable
	@Override
	public Result calculateQuickResult(@NotNull Expression[] params, ExpressionContext context)
	{
		return calculateResult(params, context);
	}

	@Nullable
	@Override
	public LookupElement[] calculateLookupItems(@NotNull Expression[] params, ExpressionContext context)
	{
		Result result = calculateResult(params, context);
		if(result == null)
		{
			return LookupElement.EMPTY_ARRAY;
		}
		List<LookupElement> list = new SmartList<LookupElement>();
		if(CSharpModuleUtil.findLanguageVersion(context.getPsiElementAtStartOffset()).isAtLeast(CSharpLanguageVersion._2_0))
		{
			list.add(LookupElementBuilder.create("var").bold());
		}
		list.add(LookupElementBuilder.create(result.toString()));
		return list.toArray(new LookupElement[list.size()]);
	}

	@Nullable
	@Override
	public Result calculateResult(
			@NotNull Expression[] params, ExpressionContext context)
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

		CSharpExpressionFragmentFileImpl expressionFragment = CSharpExpressionFragmentFactory.createExpressionFragment(context.getProject(), text,
				context.getPsiElementAtStartOffset());

		DotNetExpression expression = expressionFragment.getExpression();

		if(expression == null)
		{
			return null;
		}

		DotNetTypeRef typeRef = CSharpResolveUtil.resolveIterableType(context.getPsiElementAtStartOffset(), expression.toTypeRef(false));
		if(typeRef == DotNetTypeRef.ERROR_TYPE)
		{
			return null;
		}
		return new TextResult(typeRef.getPresentableText());
	}
}
