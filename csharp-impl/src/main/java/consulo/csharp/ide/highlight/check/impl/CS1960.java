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
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetGenericParameterListOwner;
import consulo.dotnet.psi.DotNetModifierList;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * @author VISTALL
 * @since 06.11.14
 */
public class CS1960 extends CompilerCheck<DotNetGenericParameter>
{
	private static final CSharpModifier[] ourModifiers = new CSharpModifier[] {CSharpModifier.OUT, CSharpModifier.IN};

	@RequiredReadAction
	@Nullable
	@Override
	public CompilerCheckBuilder checkImpl(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull DotNetGenericParameter element)
	{
		DotNetModifierList modifierList = element.getModifierList();
		if(modifierList == null)
		{
			return null;
		}

		for(CSharpModifier ourModifier : ourModifiers)
		{
			PsiElement modifierElement = modifierList.getModifierElement(ourModifier);
			if(modifierElement != null)
			{
				DotNetGenericParameterListOwner parameterListOwner = PsiTreeUtil.getParentOfType(element, DotNetGenericParameterListOwner.class);
				assert parameterListOwner != null;

				if(parameterListOwner instanceof CSharpTypeDeclaration && ((CSharpTypeDeclaration) parameterListOwner).isInterface() ||
						parameterListOwner instanceof CSharpMethodDeclaration && ((CSharpMethodDeclaration) parameterListOwner).isDelegate())
				{
					return null;
				}

				return newBuilder(modifierElement).addQuickFix(new RemoveModifierFix(ourModifier, element));
			}
		}
		return null;
	}
}
