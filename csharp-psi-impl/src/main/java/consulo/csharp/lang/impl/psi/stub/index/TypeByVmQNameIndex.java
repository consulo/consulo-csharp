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

package consulo.csharp.lang.impl.psi.stub.index;

import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.language.psi.stub.IntStubIndexExtension;
import consulo.language.psi.stub.StubIndexExtension;
import consulo.language.psi.stub.StubIndexKey;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 13.07.14
 */
public class TypeByVmQNameIndex extends IntStubIndexExtension<DotNetTypeDeclaration>
{
	@Nonnull
	public static TypeByVmQNameIndex getInstance()
	{
		return StubIndexExtension.EP_NAME.findExtensionOrFail(TypeByVmQNameIndex.class);
	}

	@Nonnull
	@Override
	public StubIndexKey<Integer, DotNetTypeDeclaration> getKey()
	{
		return CSharpIndexKeys.TYPE_BY_VMQNAME_INDEX;
	}

	@Override
	public int getVersion()
	{
		return super.getVersion() + 1;
	}
}