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
import org.mustbe.consulo.dotnet.ide.DotNetElementPresentationUtil;
import org.mustbe.consulo.csharp.ide.CSharpErrorBundle;
import org.mustbe.consulo.csharp.ide.highlight.check.AbstractCompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpConstructorSuperCallImpl;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 17.05.14
 */
public class CS0516 extends AbstractCompilerCheck<CSharpConstructorSuperCallImpl>
{
	@Override
	public boolean accept(@NotNull CSharpConstructorSuperCallImpl element)
	{
		PsiElement psiElement = element.resolveToCallable();
		return psiElement != null && psiElement == element.getParent();
	}

	@Override
	public void checkImpl(@NotNull CSharpConstructorSuperCallImpl element, @NotNull CompilerCheckResult checkResult)
	{
		CSharpConstructorDeclaration parent = (CSharpConstructorDeclaration) element.getParent();
		CSharpTypeDeclaration type = (CSharpTypeDeclaration) parent.getParent();

		StringBuilder builder = new StringBuilder();
		builder.append(DotNetElementPresentationUtil.formatTypeWithGenericParameters(type));
		builder.append(".");
		builder.append(DotNetElementPresentationUtil.formatMethod(parent, 0));

		checkResult.setText(CSharpErrorBundle.message(myId, builder.toString()));
	}
}
