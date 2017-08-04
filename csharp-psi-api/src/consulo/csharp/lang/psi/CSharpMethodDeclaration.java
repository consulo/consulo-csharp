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

import org.jetbrains.annotations.Nullable;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.ArrayFactory;
import consulo.annotations.RequiredReadAction;
import consulo.dotnet.psi.DotNetMethodDeclaration;

/**
 * @author VISTALL
 * @since 30.11.13.
 */
public interface CSharpMethodDeclaration extends DotNetMethodDeclaration, CSharpGenericConstraintOwner, CSharpSimpleLikeMethodAsElement
{
	public static final CSharpMethodDeclaration[] EMPTY_ARRAY = new CSharpMethodDeclaration[0];

	public static ArrayFactory<CSharpMethodDeclaration> ARRAY_FACTORY = count -> count == 0 ? EMPTY_ARRAY : new CSharpMethodDeclaration[count];

	boolean isDelegate();

	@RequiredReadAction
	boolean isOperator();

	boolean isExtension();

	@Nullable
	@RequiredReadAction
	IElementType getOperatorElementType();
}
