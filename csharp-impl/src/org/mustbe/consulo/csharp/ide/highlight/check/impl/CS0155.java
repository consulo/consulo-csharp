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
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpThrowStatementImpl;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetInheritUtil;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;

/**
 * @author VISTALL
 * @since 15.05.14
 */
public class CS0155 extends AbstractCompilerCheck<CSharpThrowStatementImpl>
{
	@Override
	public boolean accept(@NotNull CSharpThrowStatementImpl statement)
	{
		DotNetExpression expression = statement.getExpression();
		if(expression == null)
		{
			return false;
		}

		DotNetTypeRef dotNetTypeRef = expression.toTypeRef(true);

		return !DotNetInheritUtil.isParentOrSelf(DotNetTypes.System_Exception, dotNetTypeRef, statement, true);
	}
}
