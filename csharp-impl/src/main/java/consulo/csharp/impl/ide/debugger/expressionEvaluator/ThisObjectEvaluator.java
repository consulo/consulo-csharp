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

import consulo.csharp.impl.ide.debugger.CSharpEvaluateContext;
import consulo.dotnet.debugger.impl.nodes.DotNetDebuggerCompilerGenerateUtil;
import consulo.dotnet.debugger.proxy.*;
import consulo.dotnet.debugger.proxy.value.DotNetObjectValueProxy;
import consulo.dotnet.debugger.proxy.value.DotNetValueProxy;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.ObjectUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
	public void evaluate(@Nonnull CSharpEvaluateContext context) throws DotNetInvalidObjectException, DotNetInvalidStackFrameException, DotNetAbsentInformationException
	{
		DotNetStackFrameProxy frame = context.getFrame();

		context.pull(calcThisObject(frame, frame.getThisObject()), null);
	}

	@Nonnull
	public static DotNetValueProxy calcThisObject(@Nonnull DotNetStackFrameProxy proxy, DotNetValueProxy thisObject)
	{
		return ObjectUtil.notNull(tryToFindObjectInsideYieldOrAsyncThis(proxy, thisObject), thisObject);
	}

	@Nullable
	public static DotNetValueProxy tryToFindObjectInsideYieldOrAsyncThis(@Nonnull DotNetStackFrameProxy proxy, DotNetValueProxy thisObject)
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

				final DotNetFieldProxy thisFieldMirror = ContainerUtil.find(fields, fieldMirror ->
				{
					String name = fieldMirror.getName();
					return DotNetDebuggerCompilerGenerateUtil.isYieldOrAsyncThisField(name);
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
