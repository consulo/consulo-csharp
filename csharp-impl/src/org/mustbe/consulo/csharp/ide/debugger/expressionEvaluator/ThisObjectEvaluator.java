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
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.debugger.CSharpEvaluateContext;
import com.intellij.openapi.util.Condition;
import com.intellij.util.ObjectUtil;
import com.intellij.util.containers.ContainerUtil;
import consulo.dotnet.debugger.nodes.DotNetDebuggerCompilerGenerateUtil;
import consulo.dotnet.debugger.proxy.DotNetAbsentInformationException;
import consulo.dotnet.debugger.proxy.DotNetFieldProxy;
import consulo.dotnet.debugger.proxy.DotNetInvalidObjectException;
import consulo.dotnet.debugger.proxy.DotNetInvalidStackFrameException;
import consulo.dotnet.debugger.proxy.DotNetStackFrameProxy;
import consulo.dotnet.debugger.proxy.DotNetTypeProxy;
import consulo.dotnet.debugger.proxy.value.DotNetObjectValueProxy;
import consulo.dotnet.debugger.proxy.value.DotNetValueProxy;

/**
 * @author VISTALL
 * @since 05.08.2015
 */
public class ThisObjectEvaluator extends Evaluator
{
	public static final ThisObjectEvaluator INSTANCE = new ThisObjectEvaluator();

	private ThisObjectEvaluator()
	{
	}

	@Override
	public void evaluate(@NotNull CSharpEvaluateContext context) throws DotNetInvalidObjectException, DotNetInvalidStackFrameException, DotNetAbsentInformationException
	{
		DotNetStackFrameProxy frame = context.getFrame();

		context.pull(calcThisObject(frame, frame.getThisObject()), null);
	}

	@NotNull
	public static DotNetValueProxy calcThisObject(@NotNull DotNetStackFrameProxy proxy, DotNetValueProxy thisObject)
	{
		return ObjectUtil.notNull(tryToFindObjectInsideYieldOrAsyncThis(proxy, thisObject), thisObject);
	}

	@Nullable
	public static DotNetValueProxy tryToFindObjectInsideYieldOrAsyncThis(@NotNull DotNetStackFrameProxy proxy, DotNetValueProxy thisObject)
	{
		if(!(thisObject instanceof DotNetObjectValueProxy))
		{
			return null;
		}

		DotNetObjectValueProxy objectValueMirror = (DotNetObjectValueProxy) thisObject;

		DotNetTypeProxy type;
		try
		{
			type = thisObject.getType();
			assert type != null;

			if(DotNetDebuggerCompilerGenerateUtil.isYieldOrAsyncNestedType(type))
			{
				DotNetTypeProxy parentType = type.getDeclarationType();

				if(parentType == null)
				{
					return null;
				}

				DotNetFieldProxy[] fields = type.getFields();

				final DotNetFieldProxy thisFieldMirror = ContainerUtil.find(fields, new Condition<DotNetFieldProxy>()
				{
					@Override
					public boolean value(DotNetFieldProxy fieldMirror)
					{
						String name = fieldMirror.getName();
						return DotNetDebuggerCompilerGenerateUtil.isYieldOrAsyncThisField(name);
					}
				});

				if(thisFieldMirror != null)
				{
					DotNetValueProxy value = thisFieldMirror.getValue(proxy, objectValueMirror);
					if(value != null)
					{
						return value;
					}
				}
			}
		}
		catch(Exception ignored)
		{
		}
		return null;
	}
}
