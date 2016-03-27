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
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.debugger.CSharpEvaluateContext;
import org.mustbe.consulo.dotnet.debugger.proxy.DotNetStackFrameMirrorProxy;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import mono.debugger.MethodMirror;
import mono.debugger.MethodParameterMirror;
import mono.debugger.Value;

/**
 * @author VISTALL
 * @since 05.08.2015
 */
public class ParameterEvaluator extends LocalVariableOrParameterEvaluator<DotNetParameter>
{
	private int myIndex;

	@RequiredReadAction
	public ParameterEvaluator(@NotNull DotNetParameter parameter)
	{
		super(parameter);
		myIndex = parameter.getIndex();
	}

	@Override
	protected boolean tryEvaluateFromStackFrame(@NotNull CSharpEvaluateContext context, DotNetStackFrameMirrorProxy frame, MethodMirror method)
	{
		MethodParameterMirror methodParameterMirror = method.parameters()[myIndex];

		Value value = context.getFrame().localOrParameterValue(methodParameterMirror);
		if(value != null)
		{
			context.pull(value, methodParameterMirror);
			return true;
		}
		return false;
	}
}
