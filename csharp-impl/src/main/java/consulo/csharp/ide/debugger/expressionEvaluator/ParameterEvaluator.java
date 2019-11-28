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

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.ide.debugger.CSharpEvaluateContext;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.debugger.proxy.DotNetMethodParameterProxy;
import consulo.dotnet.debugger.proxy.DotNetMethodProxy;
import consulo.dotnet.debugger.proxy.DotNetStackFrameProxy;
import consulo.dotnet.debugger.proxy.value.DotNetValueProxy;

/**
 * @author VISTALL
 * @since 05.08.2015
 */
public class ParameterEvaluator extends LocalVariableOrParameterEvaluator<DotNetParameter>
{
	private int myIndex;

	@RequiredReadAction
	public ParameterEvaluator(@Nonnull DotNetParameter parameter)
	{
		super(parameter);
		myIndex = parameter.getIndex();
	}

	@Override
	protected boolean tryEvaluateFromStackFrame(@Nonnull CSharpEvaluateContext context, DotNetStackFrameProxy frame, DotNetMethodProxy method)
	{
		DotNetMethodParameterProxy methodParameterMirror = method.getParameters()[myIndex];

		DotNetValueProxy value = context.getFrame().getParameterValue(methodParameterMirror);
		if(value != null)
		{
			context.pull(value, methodParameterMirror);
			return true;
		}
		return false;
	}
}
