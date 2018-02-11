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

package consulo.csharp.ide.debugger.expressionEvaluator;

import javax.annotation.Nonnull;

import consulo.csharp.ide.debugger.CSharpEvaluateContext;
import consulo.csharp.lang.psi.CSharpTokens;
import com.intellij.psi.tree.IElementType;
import consulo.dotnet.debugger.proxy.DotNetVirtualMachineProxy;
import consulo.dotnet.debugger.proxy.value.DotNetBooleanValueProxy;
import consulo.dotnet.debugger.proxy.value.DotNetValueProxy;

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
	public void evaluate(@Nonnull CSharpEvaluateContext context)
	{
		DotNetValueProxy popValue = context.popValue();
		if(popValue == null)
		{
			throw new IllegalArgumentException("no pop value");
		}

		DotNetVirtualMachineProxy virtualMachine = context.getDebuggerContext().getVirtualMachine();
		if(myOperatorElementType == CSharpTokens.EXCL)
		{
			if(popValue instanceof DotNetBooleanValueProxy)
			{
				Boolean value = ((DotNetBooleanValueProxy) popValue).getValue();
				context.pull(virtualMachine.createBooleanValue(!value), null);
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
