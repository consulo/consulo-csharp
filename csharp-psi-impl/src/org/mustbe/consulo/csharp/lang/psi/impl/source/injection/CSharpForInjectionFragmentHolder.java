/*
 * Copyright 2013-2015 must-be.org
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

package org.mustbe.consulo.csharp.lang.psi.impl.source.injection;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import com.intellij.psi.impl.source.tree.LazyParseablePsiElement;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 04.03.2015
 */
public class CSharpForInjectionFragmentHolder extends LazyParseablePsiElement
{
	private final CSharpReferenceExpression.ResolveToKind myKind;

	public CSharpForInjectionFragmentHolder(@NotNull IElementType type, CharSequence buffer, CSharpReferenceExpression.ResolveToKind kind)
	{
		super(type, buffer);
		myKind = kind;
	}

	@NotNull
	public CSharpReferenceExpression.ResolveToKind getKind()
	{
		return myKind;
	}
}
