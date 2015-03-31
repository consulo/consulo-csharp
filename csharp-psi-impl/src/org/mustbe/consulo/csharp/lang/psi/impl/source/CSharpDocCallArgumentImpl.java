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

package org.mustbe.consulo.csharp.lang.psi.impl.source;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgument;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.impl.light.CSharpLightExpression;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import com.intellij.lang.ASTNode;

/**
 * @author VISTALL
 * @since 31.03.15
 */
public class CSharpDocCallArgumentImpl extends CSharpElementImpl implements CSharpCallArgument
{

	public CSharpDocCallArgumentImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitCallArgument(this);
	}

	@Nullable
	@Override
	public DotNetExpression getArgumentExpression()
	{
		DotNetType dotNetType = findChildByClass(DotNetType.class);
		if(dotNetType != null)
		{
			return new CSharpLightExpression(getManager(), dotNetType.toTypeRef());
		}
		return null;
	}
}
