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

package consulo.csharp.impl.ide.highlight.check.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.impl.ide.codeInsight.actions.AddModifierFix;
import consulo.csharp.impl.ide.highlight.CSharpHighlightContext;
import consulo.csharp.impl.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.impl.psi.source.CSharpPsiUtilImpl;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.util.lang.ObjectUtil;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiNameIdentifierOwner;

/**
 * @author VISTALL
 * @since 03.01.15
 */
public class CS0708 extends CompilerCheck<DotNetModifierListOwner>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull DotNetModifierListOwner element)
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
				return newBuilder(ObjectUtil.notNull(nameIdentifier, element), formatElement(element)).withQuickFix(new AddModifierFix
						(DotNetModifier.STATIC, element));
			}
		}
		return null;
	}
}
