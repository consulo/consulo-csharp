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
import org.mustbe.consulo.csharp.ide.codeInsight.actions.RemoveModifierFix;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRefUtil;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 31.08.14
 */
public class CS0722 extends CompilerCheck<DotNetLikeMethodDeclaration>
{
	@Nullable
	@Override
	public CompilerCheckResult checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull DotNetLikeMethodDeclaration element)
	{
		DotNetType type = element.getReturnType();
		PsiElement resolve = DotNetTypeRefUtil.resolve(type);
		if(resolve instanceof DotNetTypeDeclaration && ((DotNetTypeDeclaration) resolve).hasModifier(DotNetModifier.STATIC))
		{
			return result(type, formatElement(element)).addQuickFix(new RemoveModifierFix(DotNetModifier.STATIC, (DotNetModifierListOwner) resolve));
		}
		return null;
	}
}
