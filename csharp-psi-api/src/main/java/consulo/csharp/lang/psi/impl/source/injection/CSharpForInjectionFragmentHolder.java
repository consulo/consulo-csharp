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

package consulo.csharp.lang.psi.impl.source.injection;

import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.language.ast.IElementType;
import consulo.language.impl.psi.LazyParseablePsiElement;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 04.03.2015
 */
public class CSharpForInjectionFragmentHolder extends LazyParseablePsiElement
{
	private final CSharpReferenceExpression.ResolveToKind myKind;

	public CSharpForInjectionFragmentHolder(@Nonnull IElementType type, CharSequence buffer, CSharpReferenceExpression.ResolveToKind kind)
	{
		super(type, buffer);
		myKind = kind;
	}

	@Nonnull
	public CSharpReferenceExpression.ResolveToKind getKind()
	{
		return myKind;
	}
}
