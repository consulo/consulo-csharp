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

import jakarta.annotation.Nonnull;

import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.language.psi.stub.StubIndexKey;
import consulo.language.psi.stub.StringStubIndexExtension;
import consulo.language.psi.stub.StubIndexExtension;

/**
 * @author VISTALL
 * @since 18.07.15
 */
@ExtensionImpl
public class DelegateMethodIndex extends StringStubIndexExtension<CSharpMethodDeclaration>
{
	public static DelegateMethodIndex getInstance()
	{
		return StubIndexExtension.EP_NAME.findExtension(DelegateMethodIndex.class);
	}

	@Nonnull
	@Override
	public StubIndexKey<String, CSharpMethodDeclaration> getKey()
	{
		return CSharpIndexKeys.DELEGATE_METHOD_BY_NAME_INDEX;
	}
}
