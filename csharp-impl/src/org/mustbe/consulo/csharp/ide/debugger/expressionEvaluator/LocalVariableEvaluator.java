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
import consulo.annotations.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.debugger.CSharpEvaluateContext;
import org.mustbe.consulo.csharp.lang.psi.CSharpLocalVariable;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.text.StringUtil;
import consulo.dotnet.debugger.proxy.DotNetLocalVariableProxy;
import consulo.dotnet.debugger.proxy.DotNetMethodProxy;
import consulo.dotnet.debugger.proxy.DotNetStackFrameProxy;
import consulo.dotnet.debugger.proxy.value.DotNetValueProxy;

/**
 * @author VISTALL
 * @since 05.08.2015
 */
public class LocalVariableEvaluator extends LocalVariableOrParameterEvaluator<CSharpLocalVariable>
{
	@RequiredReadAction
	public LocalVariableEvaluator(CSharpLocalVariable localVariable)
	{
		super(localVariable);
	}

	@Override
	protected boolean tryEvaluateFromStackFrame(@NotNull CSharpEvaluateContext context, DotNetStackFrameProxy frame, DotNetMethodProxy method)
	{
		DotNetLocalVariableProxy[] locals = method.getLocalVariables(frame);

		DotNetLocalVariableProxy mirror = null;
		for(DotNetLocalVariableProxy local : locals)
		{
			String name = local.getName();
			if(StringUtil.isEmpty(name))
			{
				continue;
			}

			if(Comparing.equal(myName, name))
			{
				mirror = local;
				break;
			}
		}

		if(mirror != null)
		{
			DotNetValueProxy value = frame.getLocalValue(mirror);
			if(value != null)
			{
				context.pull(value, mirror);
				return true;
			}
		}
		return false;
	}
}
