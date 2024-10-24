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

import consulo.application.ReadAction;
import consulo.csharp.impl.ide.debugger.CSharpEvaluateContext;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.debugger.proxy.DotNetMethodProxy;
import consulo.dotnet.debugger.proxy.DotNetThrowValueException;
import consulo.dotnet.debugger.proxy.DotNetTypeProxy;
import consulo.dotnet.debugger.proxy.value.DotNetValueProxy;
import consulo.dotnet.psi.DotNetTypeDeclaration;

import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

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
	public void evaluate(@Nonnull CSharpEvaluateContext context) throws DotNetThrowValueException
	{
		List<DotNetValueProxy> values = new ArrayList<DotNetValueProxy>(myParameterTypes.size());
		for(int i = 0; i < myParameterTypes.size(); i++)
		{
			DotNetValueProxy argumentValue = context.popValue();
			if(argumentValue == null)
			{
				throw new IllegalArgumentException("no argument value");
			}
			values.add(argumentValue);
		}

		DotNetValueProxy popValue = context.popValue();
		if(popValue == null)
		{
			throw new IllegalArgumentException("no pop value");
		}

		DotNetTypeProxy typeMirror = null;
		if(myTypeDeclaration == null)
		{
			typeMirror = popValue.getType();
		}
		else
		{
			typeMirror = ReadAction.compute(() -> findTypeMirror(context, myTypeDeclaration));
		}

		if(typeMirror == null)
		{
			throw new IllegalArgumentException("cant calculate type");
		}

		DotNetTypeProxy[] parameterTypeMirrors = new DotNetTypeProxy[myParameterTypes.size()];
		for(int i = 0; i < parameterTypeMirrors.length; i++)
		{
			final int temp = i;
			DotNetTypeProxy parameterTypeMirror = ReadAction.compute(() -> findTypeMirror(context, myParameterTypes.get(temp)));
			if(parameterTypeMirror == null)
			{
				throw new IllegalArgumentException("cant find parameter type mirror");
			}
			parameterTypeMirrors[i] = parameterTypeMirror;
		}

		DotNetMethodProxy methodMirror = typeMirror.findMethodByName(myMethodName, true, parameterTypeMirrors);
		if(methodMirror == null)
		{
			throw new IllegalArgumentException("no method");
		}

		try
		{
			DotNetValueProxy invoke = methodMirror.invoke(context.getFrame(), substituteStaticContext(popValue), values.toArray(new DotNetValueProxy[values.size()]));
			if(invoke != null)
			{
				context.pull(invoke, methodMirror);
			}
		}
		catch(DotNetThrowValueException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			throw new IllegalArgumentException("invoking '" + methodMirror.getName() + "' has been failed");
		}
	}
}
