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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.codeInsight.template.Expression;
import com.intellij.codeInsight.template.ExpressionContext;
import com.intellij.codeInsight.template.Macro;
import com.intellij.codeInsight.template.Result;
import com.intellij.codeInsight.template.TextResult;

/**
 * @author VISTALL
 * @since 11.06.14
 */
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
	public Result calculateQuickResult(@NotNull Expression[] params, ExpressionContext context)
	{
		return calculateResult(params, context);
	}

	@Nullable
	@Override
	public Result calculateResult(
			@NotNull Expression[] params, ExpressionContext context)
	{
		return new TextResult("it");
	}
}
