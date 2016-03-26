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
import org.mustbe.consulo.csharp.lang.psi.CSharpLocalVariable;
import org.mustbe.consulo.dotnet.debugger.proxy.DotNetStackFrameMirrorProxy;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.text.StringUtil;
import mono.debugger.LocalVariableMirror;
import mono.debugger.MethodMirror;
import mono.debugger.Value;

/**
 * @author VISTALL
 * @since 05.08.2015
 */
public class LocalVariableEvaluator extends Evaluator
{
	private String myName;

	@RequiredReadAction
	public LocalVariableEvaluator(CSharpLocalVariable localVariable)
	{
		myName = localVariable.getName();
	}

	@Override
	public void evaluate(@NotNull CSharpEvaluateContext context)
	{
		DotNetStackFrameMirrorProxy frame = context.getFrame();
		MethodMirror method = frame.location().method();

		LocalVariableMirror[] locals = method.locals(frame.location().codeIndex());

		LocalVariableMirror mirror = null;
		for(LocalVariableMirror local : locals)
		{
			String name = local.name();
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
			Value value = frame.localOrParameterValue(mirror);
			if(value != null)
			{
				context.pull(value, mirror);
			}
			else
			{
				throw new IllegalArgumentException("no variable");
			}
		}
		else
		{
			throw new IllegalArgumentException("no variable");
		}
	}
}
