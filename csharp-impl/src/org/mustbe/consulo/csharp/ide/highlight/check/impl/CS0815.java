/*
 * Copyright 2013-2014 must-be.org
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

package org.mustbe.consulo.csharp.ide.highlight.check.impl;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.ide.highlight.check.AbstractCompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpLocalVariable;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaTypeRef;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;

/**
 * @author VISTALL
 * @since 15.05.14
 */
public class CS0815 extends AbstractCompilerCheck<CSharpLocalVariable>
{
	@Override
	public boolean accept(@NotNull CSharpLocalVariable cSharpLocalVariable)
	{
		DotNetTypeRef dotNetTypeRef = cSharpLocalVariable.toTypeRef(false);
		if(dotNetTypeRef == DotNetTypeRef.AUTO_TYPE)
		{
			DotNetExpression initializer = cSharpLocalVariable.getInitializer();
			if(initializer == null)
			{
				return false;
			}
			DotNetTypeRef initializerType = initializer.toTypeRef(false);
			return initializerType instanceof CSharpLambdaTypeRef;
		}
		return false;
	}

	@Override
	public void checkImpl(
			@NotNull CSharpLocalVariable element, @NotNull CompilerCheckResult checkResult)
	{
		DotNetExpression initializer = element.getInitializer();
		assert initializer != null;
		checkResult.setTextRange(initializer.getTextRange());
	}
}
