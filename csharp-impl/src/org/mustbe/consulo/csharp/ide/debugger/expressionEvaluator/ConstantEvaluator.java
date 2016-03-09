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
import org.mustbe.consulo.dotnet.DotNetTypes;
import edu.arizona.cs.mbel.signature.SignatureConstants;
import mono.debugger.BooleanValueMirror;
import mono.debugger.NumberValueMirror;
import mono.debugger.StringValueMirror;
import mono.debugger.VirtualMachine;

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
	public void evaluate(@NotNull CSharpEvaluateContext context)
	{
		VirtualMachine delegate = context.getDebuggerContext().getVirtualMachine().getDelegate();
		if(DotNetTypes.System.Int32.equals(myVmQName))
		{
			context.pull(new NumberValueMirror(delegate, SignatureConstants.ELEMENT_TYPE_I4, (Number) myValue), null);
		}
		else if(DotNetTypes.System.String.equals(myVmQName))
		{
			StringValueMirror valueMirror = delegate.rootAppDomain().createString((String) myValue);
			context.pull(valueMirror, null);
		}
		else if(DotNetTypes.System.Boolean.equals(myVmQName))
		{
			context.pull(new BooleanValueMirror(delegate, (Boolean) myValue), null);
		}
		else
		{
			throw new IllegalArgumentException("constant is not supported");
		}
	}
}
