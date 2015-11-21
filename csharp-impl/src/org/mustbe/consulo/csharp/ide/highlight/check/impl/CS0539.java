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

package org.mustbe.consulo.csharp.ide.highlight.check.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpLikeMethodDeclarationImplUtil;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.psi.DotNetVirtualImplementOwner;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;

/**
 * @author VISTALL
 * @since 08.11.14
 */
public class CS0539 extends CompilerCheck<DotNetVirtualImplementOwner>
{
	@RequiredReadAction
	@Nullable
	@Override
	public CompilerCheckBuilder checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull DotNetVirtualImplementOwner element)
	{
		PsiElement nameIdentifier = ((PsiNameIdentifierOwner) element).getNameIdentifier();
		if(nameIdentifier == null)
		{
			return null;
		}
		final Pair<CSharpLikeMethodDeclarationImplUtil.ResolveVirtualImplementResult, PsiElement> resultPair = CSharpLikeMethodDeclarationImplUtil.resolveVirtualImplementation(element, element);
		switch(resultPair.getFirst())
		{
			case CANT_HAVE:
			case FOUND:
			default:
				return null;
			case NOT_FOUND:
				return newBuilder(nameIdentifier, formatElement(element));
		}
	}
}
