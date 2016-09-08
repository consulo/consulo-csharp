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

package consulo.csharp.lang.psi.impl.source;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.csharp.lang.psi.CSharpCallArgument;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import com.intellij.lang.ASTNode;
import consulo.csharp.lang.psi.impl.light.CSharpLightExpression;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetType;

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
