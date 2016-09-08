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

package consulo.csharp.ide.highlight.check.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.ide.codeInsight.actions.RemoveModifierFix;
import consulo.csharp.ide.highlight.CSharpHighlightContext;
import consulo.csharp.ide.highlight.check.CompilerCheck;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.resolve.DotNetTypeRefUtil;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 31.08.14
 */
public class CS0722 extends CompilerCheck<DotNetLikeMethodDeclaration>
{
	@RequiredReadAction
	@Nullable
	@Override
	public CompilerCheckBuilder checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpHighlightContext highlightContext, @NotNull DotNetLikeMethodDeclaration element)
	{
		DotNetType type = element.getReturnType();
		PsiElement resolve = DotNetTypeRefUtil.resolve(type);
		if(resolve instanceof DotNetTypeDeclaration && ((DotNetTypeDeclaration) resolve).hasModifier(DotNetModifier.STATIC))
		{
			return newBuilder(type, formatElement(element)).addQuickFix(new RemoveModifierFix(DotNetModifier.STATIC, (DotNetModifierListOwner) resolve));
		}
		return null;
	}
}
