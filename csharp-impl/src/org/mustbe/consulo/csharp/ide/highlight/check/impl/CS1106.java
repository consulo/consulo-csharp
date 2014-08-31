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
import org.mustbe.consulo.csharp.ide.CSharpErrorBundle;
import org.mustbe.consulo.csharp.ide.highlight.check.AbstractCompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 22.07.14
 */
public class CS1106 extends AbstractCompilerCheck<CSharpMethodDeclaration>
{
	@Override
	public boolean accept(@NotNull CSharpMethodDeclaration element)
	{
		DotNetParameter[] parameters = element.getParameters();
		if(parameters.length > 0 && parameters[0].hasModifier(CSharpModifier.THIS))
		{
			PsiElement parent = element.getParent();
			if(parent instanceof CSharpTypeDeclaration)
			{
				return ((CSharpTypeDeclaration) parent).getGenericParametersCount() > 0 || !((CSharpTypeDeclaration) parent).hasModifier
						(DotNetModifier.STATIC);

			}
		}
		return super.accept(element);
	}

	@Override
	public void checkImpl(@NotNull CSharpMethodDeclaration element, @NotNull CompilerCheckResult checkResult)
	{
		checkResult.setText(CSharpErrorBundle.message(myId, formatElement(element)));
		PsiElement nameIdentifier = element.getNameIdentifier();
		if(nameIdentifier != null)
		{
			checkResult.setTextRange(nameIdentifier.getTextRange());
		}
	}
}
