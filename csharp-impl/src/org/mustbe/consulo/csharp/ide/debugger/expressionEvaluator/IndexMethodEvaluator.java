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
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.debugger.CSharpEvaluateContext;
import org.mustbe.consulo.csharp.lang.psi.CSharpAttributeUtil;
import org.mustbe.consulo.csharp.lang.psi.CSharpIndexMethodDeclaration;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import mono.debugger.FieldOrPropertyMirror;
import mono.debugger.InvokeFlags;
import mono.debugger.MethodMirror;
import mono.debugger.NoObjectValueMirror;
import mono.debugger.ObjectValueMirror;
import mono.debugger.PropertyMirror;
import mono.debugger.Value;

/**
 * @author VISTALL
 * @since 09.03.2016
 */
public class IndexMethodEvaluator extends FieldOrPropertyEvaluator<CSharpIndexMethodDeclaration, PropertyMirror>
{
	private List<DotNetTypeDeclaration> myParameterTypes;

	private List<Value<?>> myArgumentValues;

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

	@RequiredReadAction
	@Override
	public void evaluate(@NotNull CSharpEvaluateContext context)
	{
		myArgumentValues = new ArrayList<Value<?>>(myParameterTypes.size());
		for(int i = 0; i < myParameterTypes.size(); i++)
		{
			Value<?> argumentValue = context.popValue();
			if(argumentValue == null)
			{
				throw new IllegalArgumentException("no argument value");
			}
			myArgumentValues.add(argumentValue);
		}

		super.evaluate(context);
	}

	@Override
	protected boolean isMyMirror(@NotNull FieldOrPropertyMirror mirror)
	{
		if(mirror instanceof PropertyMirror)
		{
			MethodMirror methodMirror = ((PropertyMirror) mirror).methodGet();
			return methodMirror != null && methodMirror.parameters().length == myParameterTypes.size();
		}
		return false;
	}

	@Override
	protected boolean invoke(@NotNull PropertyMirror mirror, @NotNull CSharpEvaluateContext context, @NotNull Value<?> popValue)
	{
		assert myArgumentValues != null;

		MethodMirror methodMirror = mirror.methodGet();
		if(methodMirror == null)
		{
			return false;
		}

		Value<?> loadedValue = methodMirror.invoke(context.getFrame().thread(), InvokeFlags.DISABLE_BREAKPOINTS, popValue instanceof NoObjectValueMirror ? null : (ObjectValueMirror) popValue,
				myArgumentValues.toArray(new Value[myArgumentValues.size()]));
		if(loadedValue != null)
		{
			context.pull(loadedValue, mirror);
			return true;
		}
		return false;
	}
}
