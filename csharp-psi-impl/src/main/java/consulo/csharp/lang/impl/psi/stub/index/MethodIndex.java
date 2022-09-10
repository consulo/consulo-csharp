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

import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.language.psi.stub.StringStubIndexExtension;
import consulo.language.psi.stub.StubIndexExtension;
import consulo.language.psi.stub.StubIndexKey;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 18.12.13.
 */
public class MethodIndex extends StringStubIndexExtension<DotNetLikeMethodDeclaration>
{
	public static MethodIndex getInstance()
	{
		return StubIndexExtension.EP_NAME.findExtension(MethodIndex.class);
	}

	@Nonnull
	@Override
	public StubIndexKey<String, DotNetLikeMethodDeclaration> getKey()
	{
		return CSharpIndexKeys.METHOD_INDEX;
	}
}
