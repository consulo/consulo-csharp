/*
 * Copyright 2013-2016 must-be.org
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
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import com.intellij.psi.tree.IElementType;
import mono.debugger.BooleanValueMirror;
import mono.debugger.Value;
import mono.debugger.VirtualMachine;

/**
 * @author VISTALL
 * @since 09.03.2016
 */
public class PrefixOperatorEvaluator extends Evaluator
{
	private IElementType myOperatorElementType;

	public PrefixOperatorEvaluator(IElementType operatorElementType)
	{
		myOperatorElementType = operatorElementType;
	}

	@Override
	public void evaluate(@NotNull CSharpEvaluateContext context)
	{
		Value<?> popValue = context.popValue();
		if(popValue == null)
		{
			throw new IllegalArgumentException("no pop value");
		}

		VirtualMachine delegate = context.getDebuggerContext().getVirtualMachine().getDelegate();
		if(myOperatorElementType == CSharpTokens.EXCL)
		{
			if(popValue instanceof BooleanValueMirror)
			{
				Boolean value = ((BooleanValueMirror) popValue).value();
				context.pull(new BooleanValueMirror(delegate, !value), null);
			}
			else
			{
				throw new IllegalArgumentException("dont supported '!' with not bool value");
			}
		}
		else
		{
			throw new IllegalArgumentException("unsupported prefix operator: " + myOperatorElementType);
		}
	}
}
