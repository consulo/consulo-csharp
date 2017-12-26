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

package consulo.csharp.lang.psi.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import consulo.csharp.lang.psi.CSharpEventDeclaration;
import consulo.csharp.lang.psi.CSharpFieldDeclaration;
import consulo.csharp.lang.psi.CSharpIndexMethodDeclaration;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.psi.impl.msil.MsilElementWrapper;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.impl.smartPointers.SmartPointerAnchorProvider;

/**
 * @author VISTALL
 * @since 04.11.2015
 */
public class CSharpAnchorProvider implements SmartPointerAnchorProvider
{
	@Nullable
	@Override
	@RequiredReadAction
	public PsiElement getAnchor(@NotNull PsiElement element)
	{
		if(element instanceof MsilElementWrapper || !element.isPhysical())
		{
			return null;
		}
		if(element instanceof CSharpTypeDeclaration ||
				element instanceof CSharpFieldDeclaration ||
				element instanceof CSharpMethodDeclaration ||
				element instanceof CSharpConstructorDeclaration ||
				element instanceof CSharpIndexMethodDeclaration ||
				//element instanceof CSharpConversionMethodDeclaration ||
				element instanceof CSharpPropertyDeclaration ||
				element instanceof CSharpEventDeclaration)
		{
			return ((PsiNameIdentifierOwner) element).getNameIdentifier();
		}
		return null;
	}

	@Nullable
	@Override
	public PsiElement restoreElement(@NotNull PsiElement anchor)
	{
		return anchor.getParent();
	}
}
