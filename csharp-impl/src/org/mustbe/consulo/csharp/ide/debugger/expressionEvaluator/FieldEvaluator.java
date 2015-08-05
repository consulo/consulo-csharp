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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.debugger.CSharpEvaluateContext;
import org.mustbe.consulo.csharp.lang.psi.CSharpFieldDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import mono.debugger.FieldOrPropertyMirror;
import mono.debugger.ObjectValueMirror;
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
	public void evaluate(@NotNull CSharpEvaluateContext context)
	{
		TypeMirror typeMirror = null;
		ObjectValueMirror value = null;
		if(myTypeDeclaration == null)
		{
			value = (ObjectValueMirror) context.pop();
			typeMirror = value.type();
		}
		else
		{
			typeMirror = findTypeMirror(context, myTypeDeclaration);

			value = null;
		}

		if(typeMirror == null)
		{
			throw new IllegalArgumentException();
		}

		List<FieldOrPropertyMirror> fieldOrPropertyMirrors = typeMirror.fieldAndProperties(true);
		for(FieldOrPropertyMirror fieldOrPropertyMirror : fieldOrPropertyMirrors)
		{
			if(fieldOrPropertyMirror.name().equals(myFieldDeclaration.getName()))
			{
				Value<?> loadedValue = fieldOrPropertyMirror.value(context.getFrame().thread(), value);
				if(loadedValue != null)
				{
					context.pull(loadedValue);
				}
				break;
			}
		}
	}
}
