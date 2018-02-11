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

package consulo.csharp.lang.psi;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.intellij.util.ArrayFactory;
import consulo.dotnet.psi.DotNetElement;
import consulo.dotnet.psi.DotNetGenericParameter;

/**
 * @author VISTALL
 * @since 17.05.14
 */
public interface CSharpGenericConstraint extends DotNetElement
{
	CSharpGenericConstraint[] EMPTY_ARRAY = new CSharpGenericConstraint[0];

	ArrayFactory<CSharpGenericConstraint> ARRAY_FACTORY = count -> count == 0 ? EMPTY_ARRAY : new CSharpGenericConstraint[count];

	@Nullable
	DotNetGenericParameter resolve();

	@Nullable
	CSharpReferenceExpression getGenericParameterReference();

	@Nonnull
	CSharpGenericConstraintValue[] getGenericConstraintValues();
}
