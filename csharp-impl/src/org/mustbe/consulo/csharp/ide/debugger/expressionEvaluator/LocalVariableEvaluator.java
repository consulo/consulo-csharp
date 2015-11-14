/*
 * Copyright 2013-2015 must-be.org
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

package org.mustbe.consulo.csharp.ide.debugger.expressionEvaluator;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.ide.debugger.CSharpEvaluateContext;
import org.mustbe.consulo.csharp.lang.psi.CSharpLocalVariable;
import mono.debugger.LocalVariableMirror;
import mono.debugger.Value;

/**
 * @author VISTALL
 * @since 05.08.2015
 */
public class LocalVariableEvaluator extends Evaluator
{
	private CSharpLocalVariable myLocalVariable;

	public LocalVariableEvaluator(CSharpLocalVariable localVariable)
	{
		myLocalVariable = localVariable;
	}

	@Override
	public void evaluate(@NotNull CSharpEvaluateContext context)
	{
		LocalVariableMirror[] locals = context.getFrame().location().method().locals();

		for(LocalVariableMirror local : locals)
		{
			if(myLocalVariable.getName().equals(local.name()))
			{
				Value value = context.getFrame().localOrParameterValue(local);
				if(value != null)
				{
					context.pull(value, local);
				}
				break;
			}
		}
	}
}
