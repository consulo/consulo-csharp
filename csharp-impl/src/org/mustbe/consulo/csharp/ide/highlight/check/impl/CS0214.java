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
import org.mustbe.consulo.csharp.ide.highlight.check.AbstractCompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpFixedStatementImpl;
import org.mustbe.consulo.dotnet.psi.DotNetModifierListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * @author VISTALL
 * @since 15.05.14
 */
public class CS0214 extends AbstractCompilerCheck<CSharpFixedStatementImpl>
{
	@Override
	public boolean accept(@NotNull CSharpFixedStatementImpl statement)
	{
		DotNetQualifiedElement qualifiedElement = PsiTreeUtil.getParentOfType(statement, DotNetQualifiedElement.class);
		if(!(qualifiedElement instanceof DotNetModifierListOwner))
		{
			return false;
		}

		return !((DotNetModifierListOwner) qualifiedElement).hasModifier(CSharpModifier.UNSAFE);
	}

	@Override
	public void checkImpl(
			@NotNull CSharpFixedStatementImpl statement, @NotNull CompilerCheckResult checkResult)
	{
		PsiElement fixedElement = statement.getFixedElement();
		checkResult.setTextRange(fixedElement.getTextRange());
	}
}
