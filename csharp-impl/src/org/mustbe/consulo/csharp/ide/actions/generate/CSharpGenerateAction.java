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

package org.mustbe.consulo.csharp.ide.actions.generate;

import org.jetbrains.annotations.NotNull;
import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.actions.CodeInsightAction;

/**
 * @author VISTALL
 * @since 25.06.14
 */
public class CSharpGenerateAction extends CodeInsightAction
{
	private final CodeInsightActionHandler myHandler;

	public CSharpGenerateAction(CodeInsightActionHandler handler)
	{
		myHandler = handler;
	}

	@NotNull
	@Override
	protected CodeInsightActionHandler getHandler()
	{
		return myHandler;
	}
}
