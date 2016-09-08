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

package org.mustbe.consulo.csharp.ide.actions.navigate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodUtil;
import org.mustbe.consulo.csharp.lang.psi.CSharpSimpleLikeMethod;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.codeInsight.navigation.actions.TypeDeclarationProvider;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 06.06.2015
 */
public class GotoTypeDeclarationProvider extends TypeDeclarationProvider
{
	@Nullable
	@Override
	@RequiredReadAction
	public PsiElement[] getSymbolTypeDeclarations(@NotNull PsiElement symbol, Editor editor, int offset)
	{
		if(symbol.getLanguage() != CSharpLanguage.INSTANCE)
		{
			return null;
		}
		DotNetTypeRef typeRef = null;
		if(symbol instanceof DotNetVariable)
		{
			typeRef = ((DotNetVariable) symbol).toTypeRef(true);
		}
		else if(symbol instanceof CSharpSimpleLikeMethod)
		{
			typeRef = ((CSharpSimpleLikeMethod) symbol).getReturnTypeRef();
		}

		if(typeRef == null)
		{
			return null;
		}

		PsiElement element = typeRef.resolve().getElement();
		if(element instanceof DotNetTypeDeclaration || CSharpMethodUtil.isDelegate(element))
		{
			return new PsiElement[] {element};
		}
		return null;
	}
}
