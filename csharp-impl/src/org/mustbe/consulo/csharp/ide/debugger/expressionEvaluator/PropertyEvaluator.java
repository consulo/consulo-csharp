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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.debugger.CSharpEvaluateContext;
import org.mustbe.consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import mono.debugger.FieldOrPropertyMirror;
import mono.debugger.InvokeFlags;
import mono.debugger.MethodMirror;
import mono.debugger.NoObjectValueMirror;
import mono.debugger.ObjectValueMirror;
import mono.debugger.PropertyMirror;
import mono.debugger.StructValueMirror;
import mono.debugger.TypeMirror;
import mono.debugger.Value;

/**
 * @author VISTALL
 * @since 09.03.2016
 */
public class PropertyEvaluator extends Evaluator
{
	@Nullable
	private CSharpTypeDeclaration myTypeDeclaration;
	private CSharpPropertyDeclaration myPropertyDeclaration;

	public PropertyEvaluator(@Nullable CSharpTypeDeclaration typeDeclaration, CSharpPropertyDeclaration propertyDeclaration)
	{
		myTypeDeclaration = typeDeclaration;
		myPropertyDeclaration = propertyDeclaration;
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
			throw new IllegalArgumentException("unsupported");
		}
		else if(popValue instanceof ObjectValueMirror || popValue instanceof NoObjectValueMirror && myPropertyDeclaration.hasModifier(DotNetModifier.STATIC))
		{
			List<FieldOrPropertyMirror> fieldOrPropertyMirrors = typeMirror.fieldAndProperties(true);
			for(FieldOrPropertyMirror fieldOrPropertyMirror : fieldOrPropertyMirrors)
			{
				if(fieldOrPropertyMirror instanceof PropertyMirror && fieldOrPropertyMirror.name().equals(myPropertyDeclaration.getName()))
				{
					MethodMirror methodMirror = ((PropertyMirror) fieldOrPropertyMirror).methodGet();
					if(methodMirror == null)
					{
						continue;
					}

					Value<?> loadedValue = methodMirror.invoke(context.getFrame().thread(), InvokeFlags.DISABLE_BREAKPOINTS, popValue instanceof NoObjectValueMirror ? null : (ObjectValueMirror)
							popValue);
					if(loadedValue != null)
					{
						context.pull(loadedValue, fieldOrPropertyMirror);
						return;
					}
					break;
				}
			}
		}
		throw new IllegalArgumentException("no value");
	}
}
