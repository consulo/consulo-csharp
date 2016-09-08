/*
 * Copyright 2013-2014 must-be.org
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

import org.jetbrains.annotations.NotNull;
import consulo.annotations.RequiredReadAction;
import consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;

/**
 * @author VISTALL
 * @since 05.12.14
 */
public interface CSharpReferenceExpressionEx extends CSharpReferenceExpression
{
	@NotNull
	@RequiredReadAction
	DotNetTypeRef toTypeRefWithoutCaching(ResolveToKind kind, boolean resolveFromParent);

	@NotNull
	@RequiredReadAction
	ResolveResult[] multiResolve(final boolean incompleteCode, final boolean resolveFromParent);

	@NotNull
	@RequiredReadAction
	ResolveResult[] multiResolveImpl(ResolveToKind kind, boolean resolveFromParent);

	@NotNull
	@RequiredReadAction
	ResolveResult[] tryResolveFromQualifier(@NotNull PsiElement element);
}
