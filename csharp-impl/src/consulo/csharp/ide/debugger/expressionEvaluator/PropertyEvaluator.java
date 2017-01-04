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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.csharp.ide.debugger.CSharpEvaluateContext;
import consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.debugger.proxy.DotNetFieldOrPropertyProxy;
import consulo.dotnet.debugger.proxy.DotNetMethodProxy;
import consulo.dotnet.debugger.proxy.DotNetPropertyProxy;
import consulo.dotnet.debugger.proxy.DotNetThrowValueException;
import consulo.dotnet.debugger.proxy.value.DotNetValueProxy;

/**
 * @author VISTALL
 * @since 09.03.2016
 */
public class PropertyEvaluator extends FieldOrPropertyEvaluator<CSharpPropertyDeclaration, DotNetPropertyProxy>
{
	public PropertyEvaluator(@Nullable CSharpTypeDeclaration typeDeclaration, CSharpPropertyDeclaration propertyDeclaration)
	{
		super(typeDeclaration, propertyDeclaration);
	}

	@Override
	protected boolean isMyMirror(@NotNull DotNetFieldOrPropertyProxy mirror)
	{
		return mirror instanceof DotNetPropertyProxy && !((DotNetPropertyProxy) mirror).isArrayProperty();
	}

	@Override
	protected boolean invoke(@NotNull DotNetPropertyProxy mirror, @NotNull CSharpEvaluateContext context, @Nullable DotNetValueProxy popValue)  throws DotNetThrowValueException
	{
		DotNetMethodProxy methodMirror = mirror.getGetMethod();
		if(methodMirror == null)
		{
			return false;
		}

		DotNetValueProxy loadedValue = methodMirror.invoke(context.getFrame(), popValue);
		if(loadedValue != null)
		{
			context.pull(loadedValue, mirror);
			return true;
		}
		return false;
	}
}
