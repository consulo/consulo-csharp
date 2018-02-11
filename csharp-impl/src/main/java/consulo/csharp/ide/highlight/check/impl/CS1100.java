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

package consulo.csharp.ide.highlight.check.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.ide.codeInsight.actions.RemoveModifierFix;
import consulo.csharp.ide.highlight.CSharpHighlightContext;
import consulo.csharp.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetParameter;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 15.05.14
 */
public class CS1100 extends CompilerCheck<CSharpMethodDeclaration>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull CSharpMethodDeclaration methodDeclaration)
	{
		DotNetParameter[] parameters = methodDeclaration.getParameters();
		for(int i = 0; i < parameters.length; i++)
		{
			DotNetParameter parameter = parameters[i];
			if(i == 0)
			{
				continue;
			}

			DotNetModifierList modifierList = parameter.getModifierList();
			if(modifierList == null)
			{
				continue;
			}
			PsiElement modifierElement = modifierList.getModifierElement(CSharpModifier.THIS);
			if(modifierElement != null)
			{
				return newBuilder(modifierElement, formatElement(methodDeclaration)).addQuickFix(new RemoveModifierFix(CSharpModifier.THIS,
						parameter));
			}
		}

		return null;
	}
}
