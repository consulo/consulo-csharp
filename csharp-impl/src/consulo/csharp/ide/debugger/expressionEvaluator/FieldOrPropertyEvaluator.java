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

import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.application.ReadAction;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.ide.debugger.CSharpEvaluateContext;
import consulo.csharp.ide.debugger.CSharpStaticValueProxy;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.psi.DotNetQualifiedElement;
import consulo.dotnet.debugger.DotNetDebuggerSearchUtil;
import consulo.dotnet.debugger.proxy.DotNetFieldOrPropertyProxy;
import consulo.dotnet.debugger.proxy.DotNetThrowValueException;
import consulo.dotnet.debugger.proxy.DotNetTypeProxy;
import consulo.dotnet.debugger.proxy.value.DotNetObjectValueProxy;
import consulo.dotnet.debugger.proxy.value.DotNetStructValueProxy;
import consulo.dotnet.debugger.proxy.value.DotNetValueProxy;

/**
 * @author VISTALL
 * @since 09.03.2016
 */
public abstract class FieldOrPropertyEvaluator<T extends DotNetQualifiedElement & DotNetModifierListOwner, M extends DotNetFieldOrPropertyProxy> extends Evaluator
{
	@Nullable
	private CSharpTypeDeclaration myTypeDeclaration;
	protected T myElement;

	public FieldOrPropertyEvaluator(@Nullable CSharpTypeDeclaration typeDeclaration, T element)
	{
		myTypeDeclaration = typeDeclaration;
		myElement = element;
	}

	protected abstract boolean isMyMirror(@NotNull DotNetFieldOrPropertyProxy mirror);

	protected abstract boolean invoke(@NotNull M mirror, @NotNull CSharpEvaluateContext context, @Nullable DotNetValueProxy popValue) throws DotNetThrowValueException;

	@Nullable
	@RequiredReadAction
	public String getName()
	{
		return myElement.getName();
	}

	@Override
	public void evaluate(@NotNull CSharpEvaluateContext context) throws DotNetThrowValueException
	{
		DotNetValueProxy popValue = context.popValue();
		if(popValue == null)
		{
			throw new IllegalArgumentException("no pop value");
		}

		DotNetTypeProxy typeMirror;
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

		String name = ReadAction.compute(this::getName);
		if(name == null)
		{
			throw new IllegalArgumentException("invalid name");
		}

		if(popValue instanceof DotNetStructValueProxy)
		{
			Map<DotNetFieldOrPropertyProxy, DotNetValueProxy> values = ((DotNetStructValueProxy) popValue).getValues();

			for(Map.Entry<DotNetFieldOrPropertyProxy, DotNetValueProxy> entry : values.entrySet())
			{
				DotNetFieldOrPropertyProxy key = entry.getKey();
				if(isMyMirror(key))
				{
					DotNetValueProxy value = entry.getValue();
					if(key.getName().equals(name))
					{
						context.pull(value, key);
						return;
					}
				}
			}
		}
		else if(popValue == CSharpStaticValueProxy.INSTANCE && ReadAction.compute(() -> myElement.hasModifier(DotNetModifier.STATIC)))
		{
			if(invokeFieldOrProperty(context, name, popValue, typeMirror))
			{
				return;
			}
		}
		else
		{
			DotNetObjectValueProxy objectValueMirror = ObjectValueMirrorUtil.extractObjectValueMirror(popValue);
			if(objectValueMirror != null && invokeFieldOrProperty(context, name, objectValueMirror, typeMirror))
			{
				return;
			}

			if(tryEvaluateNonObjectValue(context, popValue))
			{
				return;
			}
		}
		throw new IllegalArgumentException("can't find member with name '" + name + "' from parent : " + typeMirror.getFullName());
	}

	protected boolean tryEvaluateNonObjectValue(CSharpEvaluateContext context, DotNetValueProxy value)
	{
		return false;
	}

	@SuppressWarnings("unchecked")
	private boolean invokeFieldOrProperty(@NotNull CSharpEvaluateContext context,
			@NotNull String name,
			@NotNull DotNetValueProxy popValue,
			@NotNull DotNetTypeProxy typeMirror) throws DotNetThrowValueException
	{
		DotNetFieldOrPropertyProxy[] fieldOrPropertyMirrors = DotNetDebuggerSearchUtil.getFieldAndProperties(typeMirror, true);
		for(DotNetFieldOrPropertyProxy fieldOrPropertyMirror : fieldOrPropertyMirrors)
		{
			if(isMyMirror(fieldOrPropertyMirror) && fieldOrPropertyMirror.getName().equals(name) && invoke((M) fieldOrPropertyMirror, context, substituteStaticContext(popValue)))
			{
				return true;
			}
		}
		return false;
	}
}
