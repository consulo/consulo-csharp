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
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import mono.debugger.InvokeFlags;
import mono.debugger.MethodMirror;
import mono.debugger.TypeMirror;
import mono.debugger.Value;

/**
 * @author VISTALL
 * @since 07.03.2016
 */
public class MethodEvaluator extends Evaluator
{
	private String myMethodName;
	private CSharpTypeDeclaration myTypeDeclaration;

	public MethodEvaluator(String methodName, CSharpTypeDeclaration typeDeclaration, int parameterListSize)
	{
		myMethodName = methodName;
		myTypeDeclaration = typeDeclaration;
	}

	@Override
	public void evaluate(@NotNull CSharpEvaluateContext context)
	{
		Value<?> popValue = context.popValue();
		if(popValue == null)
		{
			throw new IllegalArgumentException("no pop value");
		}

		TypeMirror typeMirror = null;
		if(myTypeDeclaration == null)
		{
			typeMirror = popValue.type();
		}
		else
		{
			typeMirror = findTypeMirror(context, myTypeDeclaration);
		}

		if(typeMirror == null)
		{
			throw new IllegalArgumentException("cant calculate type");
		}

		MethodMirror methodMirror = typeMirror.findMethodByName(myMethodName, true);
		if(methodMirror == null)
		{
			throw new IllegalArgumentException("no method");
		}

		Value<?> invoke = methodMirror.invoke(context.getFrame().thread(), InvokeFlags.DISABLE_BREAKPOINTS, popValue);
		if(invoke != null)
		{
			context.pull(invoke, methodMirror);
		}
	}
}
