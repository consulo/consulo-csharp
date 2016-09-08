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

package org.mustbe.consulo.csharp.ide.highlight.check.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.highlight.CSharpHighlightContext;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpConversionMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpEnumConstantDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetQualifiedElement;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;

/**
 * @author VISTALL
 * @since 30.03.2015
 */
public class CS0542 extends CompilerCheck<DotNetQualifiedElement>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpHighlightContext highlightContext, @NotNull DotNetQualifiedElement element)
	{
		PsiElement parent = element.getParent();
		if(!(parent instanceof CSharpTypeDeclaration) | !(element instanceof PsiNameIdentifierOwner) || element instanceof CSharpConstructorDeclaration || element instanceof
				CSharpConversionMethodDeclaration || element instanceof CSharpEnumConstantDeclaration)
		{
			return null;
		}

		if(Comparing.equal(element.getName(), ((CSharpTypeDeclaration) parent).getName()))
		{
			PsiElement nameIdentifier = ((PsiNameIdentifierOwner) element).getNameIdentifier();
			if(nameIdentifier == null)
			{
				return null;
			}
			return newBuilder(nameIdentifier, element.getName());
		}
		return null;
	}
}
