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

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.Nonnull;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.impl.ide.debugger.CSharpEvaluateContext;
import consulo.csharp.lang.impl.psi.CSharpAttributeUtil;
import consulo.csharp.lang.psi.CSharpIndexMethodDeclaration;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.debugger.proxy.DotNetFieldOrPropertyProxy;
import consulo.dotnet.debugger.proxy.DotNetMethodProxy;
import consulo.dotnet.debugger.proxy.DotNetNotSuspendedException;
import consulo.dotnet.debugger.proxy.DotNetPropertyProxy;
import consulo.dotnet.debugger.proxy.DotNetThrowValueException;
import consulo.dotnet.debugger.proxy.value.DotNetArrayValueProxy;
import consulo.dotnet.debugger.proxy.value.DotNetNumberValueProxy;
import consulo.dotnet.debugger.proxy.value.DotNetValueProxy;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 09.03.2016
 */
public class IndexMethodEvaluator extends FieldOrPropertyEvaluator<CSharpIndexMethodDeclaration, DotNetPropertyProxy>
{
	private List<DotNetTypeDeclaration> myParameterTypes;

	private List<DotNetValueProxy> myArgumentValues;

	public IndexMethodEvaluator(CSharpIndexMethodDeclaration variable, List<DotNetTypeDeclaration> parameterTypes)
	{
		super(null, variable);
		myParameterTypes = parameterTypes;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getName()
	{
		String value = CSharpAttributeUtil.findSingleAttributeValue(myElement, DotNetTypes.System.Runtime.CompilerServices.IndexerName, String.class);
		if(value != null)
		{
			return value;
		}
		return "Item";
	}

	@Override
	public void evaluate(@Nonnull CSharpEvaluateContext context) throws DotNetThrowValueException, DotNetNotSuspendedException
	{
		myArgumentValues = new ArrayList<>(myParameterTypes.size());
		for(int i = 0; i < myParameterTypes.size(); i++)
		{
			DotNetValueProxy argumentValue = context.popValue();
			if(argumentValue == null)
			{
				throw new IllegalArgumentException("no argument value");
			}
			myArgumentValues.add(argumentValue);
		}

		super.evaluate(context);
	}

	@Override
	protected boolean isMyMirror(@Nonnull DotNetFieldOrPropertyProxy mirror)
	{
		if(mirror instanceof DotNetPropertyProxy)
		{
			DotNetMethodProxy methodMirror = ((DotNetPropertyProxy) mirror).getGetMethod();
			return methodMirror != null && methodMirror.getParameters().length == myParameterTypes.size();
		}
		return false;
	}

	@Override
	protected boolean invoke(@Nonnull DotNetPropertyProxy mirror,
			@Nonnull CSharpEvaluateContext context,
			@Nullable DotNetValueProxy popValue) throws DotNetThrowValueException, DotNetNotSuspendedException
	{
		assert myArgumentValues != null;

		DotNetMethodProxy methodMirror = mirror.getGetMethod();
		if(methodMirror == null)
		{
			return false;
		}

		DotNetValueProxy loadedValue = methodMirror.invoke(context.getFrame(), popValue, myArgumentValues.toArray(new DotNetValueProxy[myArgumentValues.size()]));
		if(loadedValue != null)
		{
			context.pull(loadedValue, mirror);
			return true;
		}
		return false;
	}

	@Override
	protected boolean tryEvaluateNonObjectValue(CSharpEvaluateContext context, DotNetValueProxy value)
	{
		if(value instanceof DotNetArrayValueProxy && myArgumentValues.size() == 1)
		{
			DotNetValueProxy argumentValue = myArgumentValues.get(0);
			if(argumentValue instanceof DotNetNumberValueProxy)
			{
				int index = ((DotNetNumberValueProxy) argumentValue).getValue().intValue();

				context.pull(((DotNetArrayValueProxy) value).get(index), null);
				return true;
			}
		}
		return false;
	}
}
