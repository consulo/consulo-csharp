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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.intellij.codeInsight.template.Expression;
import com.intellij.codeInsight.template.ExpressionContext;
import com.intellij.codeInsight.template.Macro;
import com.intellij.codeInsight.template.Result;
import com.intellij.codeInsight.template.TextResult;

/**
 * @author VISTALL
 * @since 07.03.2016
 */
public class TypeMacro extends Macro
{
	@Override
	public String getName()
	{
		return "csharpType";
	}

	@Override
	public String getPresentableName()
	{
		return "type";
	}

	@Nullable
	@Override
	public Result calculateResult(@Nonnull Expression[] params, ExpressionContext context)
	{
		return new TextResult("TYPE");
	}
}
