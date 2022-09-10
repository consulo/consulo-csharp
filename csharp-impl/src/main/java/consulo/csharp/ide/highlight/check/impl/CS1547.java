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

import consulo.language.psi.PsiErrorElement;
import consulo.language.ast.IElementType;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.ide.highlight.CSharpHighlightContext;
import consulo.csharp.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpNativeType;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.impl.psi.source.CSharpTypeOfExpressionImpl;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetFieldDeclaration;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetType;
import consulo.language.psi.PsiElement;
import consulo.util.dataholder.Key;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 07.12.2015
 */
public class CS1547 extends CompilerCheck<CSharpNativeType>
{
	public static final Key<Boolean> ourReturnTypeFlag = Key.create("CS1547");

	private static final String VOID = "void";

	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull CSharpNativeType element)
	{
		if(highlightContext.getFile().getUserData(ourReturnTypeFlag) == Boolean.TRUE)
		{
			return null;
		}

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
