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

package consulo.csharp.impl.ide.debugger.expressionEvaluator;

import javax.annotation.Nonnull;

import consulo.csharp.impl.ide.debugger.CSharpEvaluateContext;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.debugger.proxy.DotNetVirtualMachineProxy;
import consulo.internal.dotnet.asm.signature.SignatureConstants;

/**
 * @author VISTALL
 * @since 07.03.2016
 */
public class ConstantEvaluator extends Evaluator
{
	private Object myValue;
	private String myVmQName;

	public ConstantEvaluator(Object value, String vmQName)
	{
		myValue = value;
		myVmQName = vmQName;
	}

	@Override
	public void evaluate(@Nonnull CSharpEvaluateContext context)
	{
		DotNetVirtualMachineProxy virtualMachine = context.getDebuggerContext().getVirtualMachine();
		if(DotNetTypes.System.Int32.equals(myVmQName))
		{
			context.pull(virtualMachine.createNumberValue(SignatureConstants.ELEMENT_TYPE_I4, (Number) myValue), null);
		}
		else if(DotNetTypes.System.String.equals(myVmQName))
		{
			context.pull(virtualMachine.createStringValue((String) myValue), null);
		}
		else if(DotNetTypes.System.Boolean.equals(myVmQName))
		{
			context.pull(virtualMachine.createBooleanValue((Boolean) myValue), null);
		}
		else
		{
			throw new IllegalArgumentException("constant is not supported");
		}
	}
}
