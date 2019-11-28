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

import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.text.StringUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.ide.debugger.CSharpEvaluateContext;
import consulo.csharp.lang.psi.CSharpLocalVariable;
import consulo.dotnet.debugger.nodes.DotNetDebuggerCompilerGenerateUtil;
import consulo.dotnet.debugger.proxy.DotNetFieldProxy;
import consulo.dotnet.debugger.proxy.DotNetLocalVariableProxy;
import consulo.dotnet.debugger.proxy.DotNetMethodProxy;
import consulo.dotnet.debugger.proxy.DotNetStackFrameProxy;
import consulo.dotnet.debugger.proxy.DotNetTypeProxy;
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
	protected boolean tryEvaluateFromStackFrame(@Nonnull CSharpEvaluateContext context, DotNetStackFrameProxy frame, DotNetMethodProxy method)
	{
		DotNetLocalVariableProxy[] locals = method.getLocalVariables(frame);

		DotNetLocalVariableProxy mirror = null;
		DotNetFieldProxy wrappedFieldLocal = null;
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

			if(DotNetDebuggerCompilerGenerateUtil.isLocalVarWrapper(name))
			{
				DotNetTypeProxy type = local.getType();
				if(type == null)
				{
					continue;
				}

				DotNetFieldProxy[] fields = type.getFields();
				if(fields.length != 1)
				{
					continue;
				}

				DotNetFieldProxy field = fields[0];
				if(Comparing.equal(myName, field.getName()))
				{
					mirror = local;
					wrappedFieldLocal = field;
					break;
				}
			}
		}

		if(mirror != null)
		{
			DotNetValueProxy value = frame.getLocalValue(mirror);

			if(wrappedFieldLocal != null && value != null)
			{
				DotNetValueProxy localValue = wrappedFieldLocal.getValue(frame, value);
				if(localValue != null)
				{
					context.pull(localValue, mirror);
					return true;
				}
			}
			else if(value != null)
			{
				context.pull(value, mirror);
				return true;
			}
		}
		return false;
	}
}
