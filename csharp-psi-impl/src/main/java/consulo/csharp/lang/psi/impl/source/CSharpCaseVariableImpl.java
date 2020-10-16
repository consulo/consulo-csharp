/*
 * Copyright 2013-2019 consulo.io
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

import com.intellij.psi.tree.IElementType;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.dotnet.psi.DotNetType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2019-10-05
 */
public class CSharpCaseVariableImpl extends CSharpVariableImpl
{
	public CSharpCaseVariableImpl(@Nonnull IElementType elementType)
	{
		super(elementType);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitCaseVariable(this);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public DotNetType getType()
	{
		return findNotNullChildByClass(DotNetType.class);
	}
}
