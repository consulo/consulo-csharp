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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.ide.codeInsight.actions.AddModifierFix;
import consulo.csharp.ide.highlight.CSharpHighlightContext;
import consulo.csharp.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.impl.source.CSharpPsiUtilImpl;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.util.ObjectUtil;

/**
 * @author VISTALL
 * @since 03.01.15
 */
public class CS0708 extends CompilerCheck<DotNetModifierListOwner>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpHighlightContext highlightContext, @NotNull DotNetModifierListOwner element)
	{
		PsiElement parent = element.getParent();
		if(parent instanceof DotNetTypeDeclaration && ((DotNetTypeDeclaration) parent).hasModifier(DotNetModifier.STATIC))
		{
			if(CSharpPsiUtilImpl.isTypeLikeElement(element))
			{
				return null;
			}
			if(!element.hasModifier(DotNetModifier.STATIC))
			{
				PsiElement nameIdentifier = ((PsiNameIdentifierOwner) element).getNameIdentifier();
				return newBuilder(ObjectUtil.notNull(nameIdentifier, element), formatElement(element)).addQuickFix(new AddModifierFix
						(DotNetModifier.STATIC, element));
			}
		}
		return null;
	}
}