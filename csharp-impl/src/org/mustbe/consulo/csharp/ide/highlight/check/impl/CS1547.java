/*
 * Copyright 2013-2015 must-be.org
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
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpNativeType;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpTypeOfExpressionImpl;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.psi.DotNetFieldDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 07.12.2015
 */
public class CS1547 extends CompilerCheck<CSharpNativeType>
{
	private static final String VOID = "void";

	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpNativeType element)
	{
		IElementType typeElementType = element.getTypeElementType();
		if(typeElementType == CSharpTokens.VOID_KEYWORD)
		{
			PsiElement parent = element.getParent();
			if(parent instanceof CSharpTypeOfExpressionImpl)
			{
				return null;
			}
			if(!(parent instanceof DotNetLikeMethodDeclaration))
			{
				if(parent instanceof DotNetFieldDeclaration)
				{
					if(((DotNetFieldDeclaration) parent).isConstant() || ((DotNetFieldDeclaration) parent).getInitializer() != null)
					{
						return newBuilder(element, VOID);
					}

					PsiElement lastChild = parent.getLastChild();
					// dont show error while typing
					if(lastChild instanceof PsiErrorElement)
					{
						return null;
					}
				}
				return newBuilder(element, VOID);
			}

			DotNetType returnType = ((DotNetLikeMethodDeclaration) parent).getReturnType();
			if(returnType != element)
			{
				return newBuilder(element, VOID);
			}
		}
		return null;
	}
}
