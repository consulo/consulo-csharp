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
import org.mustbe.consulo.csharp.ide.codeInsight.actions.AddModifierFix;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpFixedStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpUnsafeStatementImpl;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.psi.DotNetModifierListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import org.mustbe.consulo.dotnet.psi.DotNetStatement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * @author VISTALL
 * @since 15.05.14
 */
public class CS0214 extends CompilerCheck<DotNetStatement>
{
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull DotNetStatement statement)
	{
		if(statement instanceof CSharpUnsafeStatementImpl || statement instanceof CSharpFixedStatementImpl)
		{
			DotNetQualifiedElement qualifiedElement = PsiTreeUtil.getParentOfType(statement, DotNetQualifiedElement.class);
			if(!(qualifiedElement instanceof DotNetModifierListOwner))
			{
				return null;
			}

			if(!((DotNetModifierListOwner) qualifiedElement).hasModifier(CSharpModifier.UNSAFE))
			{
				PsiElement target = statement instanceof CSharpUnsafeStatementImpl ? ((CSharpUnsafeStatementImpl) statement).getUnsafeElement() :
						((CSharpFixedStatementImpl)statement).getFixedElement();

				return newBuilder(target).addQuickFix(new AddModifierFix(CSharpModifier.UNSAFE, (DotNetModifierListOwner) qualifiedElement));
			}
		}
		return null;
	}
}
