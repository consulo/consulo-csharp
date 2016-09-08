/*
 * Copyright 2013-2016 must-be.org
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
import consulo.annotations.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.highlight.CSharpHighlightContext;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpSwitchStatementImpl;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.psi.PsiElement;
import com.intellij.util.ArrayUtil;

/**
 * @author VISTALL
 * @since 5/11/2016
 */
public class CS0151 extends CompilerCheck<DotNetExpression>
{
	private static String[] ourSwitchTypes = new String[]{
			DotNetTypes.System.SByte,
			DotNetTypes.System.Byte,
			DotNetTypes.System.Int16,
			DotNetTypes.System.UInt16,
			DotNetTypes.System.Int32,
			DotNetTypes.System.UInt32,
			DotNetTypes.System.Int64,
			DotNetTypes.System.UInt64,

			DotNetTypes.System.Boolean,
			DotNetTypes.System.String,
			DotNetTypes.System.Char,
	};

	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpHighlightContext highlightContext, @NotNull DotNetExpression element)
	{
		if(element.getParent() instanceof CSharpSwitchStatementImpl)
		{
			DotNetTypeRef typeRef = element.toTypeRef(true);

			if(isValidTypeRef(typeRef))
			{
				return null;
			}

			return newBuilder(element, formatTypeRef(typeRef, element));
		}

		return null;
	}

	@RequiredReadAction
	private boolean isValidTypeRef(DotNetTypeRef typeRef)
	{
		DotNetTypeResolveResult typeResolveResult = typeRef.resolve();
		PsiElement resolvedElement = typeResolveResult.getElement();

		if(resolvedElement instanceof CSharpTypeDeclaration)
		{
			CSharpTypeDeclaration typeDeclaration = (CSharpTypeDeclaration) resolvedElement;
			if(typeDeclaration.isEnum())
			{
				return true;
			}
			String vmQName = typeDeclaration.getVmQName();
			if(ArrayUtil.contains(vmQName, ourSwitchTypes))
			{
				return true;
			}

			if(DotNetTypes.System.Nullable$1.equals(vmQName))
			{
				int genericParametersCount = typeDeclaration.getGenericParametersCount();
				if(genericParametersCount > 0)
				{
					DotNetGenericParameter genericParameter = typeDeclaration.getGenericParameters()[0];

					DotNetGenericExtractor genericExtractor = typeResolveResult.getGenericExtractor();
					DotNetTypeRef extractedTypRef = genericExtractor.extract(genericParameter);
					if(extractedTypRef == null)
					{
						return false;
					}

					return isValidTypeRef(extractedTypRef);
				}
			}
		}

		return false;
	}
}
