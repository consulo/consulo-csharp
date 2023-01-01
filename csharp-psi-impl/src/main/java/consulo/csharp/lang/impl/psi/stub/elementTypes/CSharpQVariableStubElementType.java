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

package consulo.csharp.lang.impl.psi.stub.elementTypes;

import consulo.annotation.access.RequiredReadAction;
import consulo.dotnet.psi.DotNetQualifiedElement;
import consulo.dotnet.psi.DotNetVariable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 07.01.14.
 */
public abstract class CSharpQVariableStubElementType<P extends DotNetVariable & DotNetQualifiedElement> extends CSharpBaseVariableStubElementType<P>
{
	public CSharpQVariableStubElementType(@Nonnull String debugName)
	{
		super(debugName);
	}

	@Override
	protected boolean supportsParentQName()
	{
		return true;
	}

	@RequiredReadAction
	@Nullable
	@Override
	protected String getParentQName(@Nonnull P variable)
	{
		return variable.getPresentableParentQName();
	}
}
