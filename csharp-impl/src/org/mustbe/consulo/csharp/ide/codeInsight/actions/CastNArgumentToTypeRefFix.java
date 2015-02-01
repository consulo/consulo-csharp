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

package org.mustbe.consulo.csharp.ide.codeInsight.actions;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeRefPresentationUtil;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.BundleBase;

/**
 * @author VISTALL
 * @since 30.12.14
 */
public class CastNArgumentToTypeRefFix extends CastExpressionToTypeRef
{
	@NotNull
	private final String myParameterName;

	public CastNArgumentToTypeRefFix(@NotNull DotNetExpression expression, @NotNull DotNetTypeRef expectedTypeRef, @NotNull String parameterName)
	{
		super(expression, expectedTypeRef);
		myParameterName = parameterName;
	}

	@NotNull
	@Override
	public String getText()
	{
		DotNetExpression element = myExpressionPointer.getElement();
		if(element == null)
		{
			return "invalid";
		}
		return BundleBase.format("Cast ''{0}'' argument to ''{1}''", myParameterName, CSharpTypeRefPresentationUtil.buildTextWithKeyword
				(myExpectedTypeRef, element));
	}
}
