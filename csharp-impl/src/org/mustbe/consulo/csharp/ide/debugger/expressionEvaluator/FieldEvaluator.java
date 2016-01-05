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

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.debugger.CSharpEvaluateContext;
import org.mustbe.consulo.csharp.lang.psi.CSharpFieldDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import mono.debugger.FieldOrPropertyMirror;
import mono.debugger.NoObjectValueMirror;
import mono.debugger.ObjectValueMirror;
import mono.debugger.StructValueMirror;
import mono.debugger.TypeMirror;
import mono.debugger.Value;

/**
 * @author VISTALL
 * @since 05.08.2015
 */
public class FieldEvaluator extends Evaluator
{
	@Nullable
	private CSharpTypeDeclaration myTypeDeclaration;
	private CSharpFieldDeclaration myFieldDeclaration;

	public FieldEvaluator(@Nullable CSharpTypeDeclaration typeDeclaration, CSharpFieldDeclaration fieldDeclaration)
	{
		myTypeDeclaration = typeDeclaration;
		myFieldDeclaration = fieldDeclaration;
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

		if(popValue instanceof StructValueMirror)
		{
			Map<FieldOrPropertyMirror, Value<?>> values = ((StructValueMirror) popValue).map();

			for(Map.Entry<FieldOrPropertyMirror, Value<?>> entry : values.entrySet())
			{
				FieldOrPropertyMirror key = entry.getKey();
				Value<?> value = entry.getValue();
				if(key.name().equals(myFieldDeclaration.getName()))
				{
					context.pull(value, key);
					break;
				}
			}
		}
		else if(popValue instanceof ObjectValueMirror || popValue instanceof NoObjectValueMirror && myFieldDeclaration.hasModifier(DotNetModifier.STATIC))
		{
			List<FieldOrPropertyMirror> fieldOrPropertyMirrors = typeMirror.fieldAndProperties(true);
			for(FieldOrPropertyMirror fieldOrPropertyMirror : fieldOrPropertyMirrors)
			{
				if(fieldOrPropertyMirror.name().equals(myFieldDeclaration.getName()))
				{
					Value<?> loadedValue = fieldOrPropertyMirror.value(context.getFrame().thread(), popValue instanceof NoObjectValueMirror ? null : (ObjectValueMirror) popValue);
					if(loadedValue != null)
					{
						context.pull(loadedValue, fieldOrPropertyMirror);
					}
					break;
				}
			}
		}
		else
		{
			throw new IllegalArgumentException("no value");
		}
	}
}
