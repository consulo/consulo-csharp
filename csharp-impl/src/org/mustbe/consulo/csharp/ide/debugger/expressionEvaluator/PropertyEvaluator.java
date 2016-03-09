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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.debugger.CSharpEvaluateContext;
import org.mustbe.consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
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
public class PropertyEvaluator extends FieldOrPropertyEvaluator<CSharpPropertyDeclaration, PropertyMirror>
{
	public PropertyEvaluator(@Nullable CSharpTypeDeclaration typeDeclaration, CSharpPropertyDeclaration propertyDeclaration)
	{
		super(typeDeclaration, propertyDeclaration);
	}

	@Override
	protected boolean isMyMirror(@NotNull FieldOrPropertyMirror mirror)
	{
		return mirror instanceof PropertyMirror && !((PropertyMirror) mirror).isArrayProperty();
	}

	@Override
	protected boolean invoke(@NotNull PropertyMirror mirror, @NotNull CSharpEvaluateContext context, @NotNull Value<?> popValue)
	{
		MethodMirror methodMirror = mirror.methodGet();
		if(methodMirror == null)
		{
			return false;
		}

		Value<?> loadedValue = methodMirror.invoke(context.getFrame().thread(), InvokeFlags.DISABLE_BREAKPOINTS, popValue instanceof NoObjectValueMirror ? null : (ObjectValueMirror) popValue);
		if(loadedValue != null)
		{
			context.pull(loadedValue, mirror);
			return true;
		}
		return false;
	}
}
