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
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpTypeDeclarationImplUtil;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import com.intellij.psi.PsiElement;
import com.intellij.util.ObjectUtils;

/**
 * @author VISTALL
 * @since 22.07.14
 */
public class CS1106 extends CompilerCheck<CSharpMethodDeclaration>
{
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpMethodDeclaration element)
	{
		DotNetParameter[] parameters = element.getParameters();
		if(parameters.length > 0 && parameters[0].hasModifier(CSharpModifier.THIS))
		{
			PsiElement parent = element.getParent();
			if(parent instanceof CSharpTypeDeclaration)
			{
				if(((CSharpTypeDeclaration) parent).getGenericParametersCount() > 0 || !CSharpTypeDeclarationImplUtil.isExplicitStaticType(parent))
				{
					return newBuilder(ObjectUtils.notNull(element.getNameIdentifier(), element), formatElement(element));
				}
			}
		}
		return super.checkImpl(languageVersion, element);
	}
}
