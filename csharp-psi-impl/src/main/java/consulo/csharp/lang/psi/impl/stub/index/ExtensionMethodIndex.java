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

package consulo.csharp.lang.psi.impl.stub.index;

import com.intellij.psi.stubs.IntStubIndexExtension;
import com.intellij.psi.stubs.StubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 25.03.14
 */
public class ExtensionMethodIndex extends IntStubIndexExtension<DotNetLikeMethodDeclaration>
{
	@Nonnull
	public static ExtensionMethodIndex getInstance()
	{
		return StubIndexExtension.EP_NAME.findExtensionOrFail(ExtensionMethodIndex.class);
	}

	@Nonnull
	@Override
	public StubIndexKey<Integer, DotNetLikeMethodDeclaration> getKey()
	{
		return CSharpIndexKeys.EXTENSION_METHOD_BY_NAME_INDEX;
	}
}
