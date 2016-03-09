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

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.ide.debugger.CSharpEvaluateContext;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
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
	private List<DotNetTypeDeclaration> myParameterTypes;

	public MethodEvaluator(String methodName, CSharpTypeDeclaration typeDeclaration, List<DotNetTypeDeclaration> parameterTypes)
	{
		myMethodName = methodName;
		myTypeDeclaration = typeDeclaration;
		myParameterTypes = parameterTypes;
	}

	@Override
	public void evaluate(@NotNull CSharpEvaluateContext context)
	{
		List<Value<?>> values = new ArrayList<Value<?>>(myParameterTypes.size());
		for(int i = 0; i < myParameterTypes.size(); i++)
		{
			Value<?> argumentValue = context.popValue();
			if(argumentValue == null)
			{
				throw new IllegalArgumentException("no argument value");
			}
			values.add(argumentValue);
		}

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

		TypeMirror[] parameterTypeMirrors = new TypeMirror[myParameterTypes.size()];
		for(int i = 0; i < parameterTypeMirrors.length; i++)
		{
			TypeMirror parameterTypeMirror = findTypeMirror(context, myParameterTypes.get(i));
			if(parameterTypeMirror == null)
			{
				throw new IllegalArgumentException("cant find parameter type mirror");
			}
			parameterTypeMirrors[i] = parameterTypeMirror;
		}

		MethodMirror methodMirror = typeMirror.findMethodByName(myMethodName, true, parameterTypeMirrors);
		if(methodMirror == null)
		{
			throw new IllegalArgumentException("no method");
		}

		Value<?> invoke = methodMirror.invoke(context.getFrame().thread(), InvokeFlags.DISABLE_BREAKPOINTS, popValue, values.toArray(new Value[values.size()]));
		if(invoke != null)
		{
			context.pull(invoke, methodMirror);
		}
	}
}
