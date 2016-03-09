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

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.debugger.CSharpEvaluateContext;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import mono.debugger.FieldOrPropertyMirror;
import mono.debugger.NoObjectValueMirror;
import mono.debugger.ObjectValueMirror;
import mono.debugger.StructValueMirror;
import mono.debugger.TypeMirror;
import mono.debugger.Value;

/**
 * @author VISTALL
 * @since 09.03.2016
 */
public abstract class FieldOrPropertyEvaluator<T extends DotNetQualifiedElement & DotNetModifierListOwner, M extends FieldOrPropertyMirror> extends Evaluator
{
	@Nullable
	private CSharpTypeDeclaration myTypeDeclaration;
	protected T myElement;

	public FieldOrPropertyEvaluator(@Nullable CSharpTypeDeclaration typeDeclaration, T element)
	{
		myTypeDeclaration = typeDeclaration;
		myElement = element;
	}

	protected abstract boolean isMyMirror(@NotNull FieldOrPropertyMirror mirror);

	protected abstract boolean invoke(@NotNull M mirror, @NotNull CSharpEvaluateContext context, @NotNull Value<?> popValue);

	@Nullable
	@RequiredReadAction
	public String getName()
	{
		return myElement.getName();
	}

	@Override
	@RequiredReadAction
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

		String name = getName();
		if(name == null)
		{
			throw new IllegalArgumentException("invalid name");
		}

		if(popValue instanceof StructValueMirror)
		{
			Map<FieldOrPropertyMirror, Value<?>> values = ((StructValueMirror) popValue).map();

			for(Map.Entry<FieldOrPropertyMirror, Value<?>> entry : values.entrySet())
			{
				FieldOrPropertyMirror key = entry.getKey();
				if(isMyMirror(key))
				{
					Value<?> value = entry.getValue();
					if(key.name().equals(name))
					{
						context.pull(value, key);
						return;
					}
				}
			}
		}
		else if(popValue instanceof NoObjectValueMirror && myElement.hasModifier(DotNetModifier.STATIC))
		{
			if(invokeFieldOrProperty(context, name, popValue, typeMirror))
			{
				return;
			}
		}
		else
		{
			ObjectValueMirror objectValueMirror = ObjectValueMirrorUtil.extractObjectValueMirror(popValue);
			if(objectValueMirror != null && invokeFieldOrProperty(context, name, objectValueMirror, typeMirror))
			{
				return;
			}
		}
		throw new IllegalArgumentException("unsupported");
	}

	@SuppressWarnings("unchecked")
	private boolean invokeFieldOrProperty(@NotNull CSharpEvaluateContext context, @NotNull String name, @NotNull Value<?> popValue, @NotNull TypeMirror typeMirror)
	{
		List<FieldOrPropertyMirror> fieldOrPropertyMirrors = typeMirror.fieldAndProperties(true);
		for(FieldOrPropertyMirror fieldOrPropertyMirror : fieldOrPropertyMirrors)
		{
			if(isMyMirror(fieldOrPropertyMirror) && fieldOrPropertyMirror.name().equals(name) && invoke((M) fieldOrPropertyMirror, context, popValue))
			{
				return true;
			}
		}
		return false;
	}
}
