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

package consulo.csharp.lang.impl.psi.source;

import consulo.language.ast.IElementType;
import consulo.csharp.lang.psi.CSharpCallArgument;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.impl.psi.light.CSharpLightExpression;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 31.03.15
 */
public class CSharpDocCallArgumentImpl extends CSharpElementImpl implements CSharpCallArgument
{
	public CSharpDocCallArgumentImpl(@Nonnull IElementType elementType)
	{
		super(elementType);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
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
