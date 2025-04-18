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

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import consulo.csharp.impl.ide.debugger.CSharpEvaluateContext;
import consulo.csharp.lang.psi.CSharpFieldDeclaration;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.debugger.proxy.DotNetFieldOrPropertyProxy;
import consulo.dotnet.debugger.proxy.DotNetFieldProxy;
import consulo.dotnet.debugger.proxy.value.DotNetValueProxy;

/**
 * @author VISTALL
 * @since 05.08.2015
 */
public class FieldEvaluator extends FieldOrPropertyEvaluator<CSharpFieldDeclaration, DotNetFieldProxy>
{
	public FieldEvaluator(@Nullable CSharpTypeDeclaration typeDeclaration, CSharpFieldDeclaration variable)
	{
		super(typeDeclaration, variable);
	}

	@Override
	protected boolean isMyMirror(@Nonnull DotNetFieldOrPropertyProxy mirror)
	{
		return mirror instanceof DotNetFieldProxy;
	}

	@Override
	protected boolean invoke(@Nonnull DotNetFieldProxy mirror, @Nonnull CSharpEvaluateContext context, @Nullable DotNetValueProxy popValue)
	{
		DotNetValueProxy loadedValue = mirror.getValue(context.getFrame(), popValue);
		if(loadedValue != null)
		{
			context.pull(loadedValue, mirror);
			return true;
		}
		return false;
	}
}
