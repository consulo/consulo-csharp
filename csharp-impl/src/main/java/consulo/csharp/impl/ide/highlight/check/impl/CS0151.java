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

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.impl.ide.highlight.CSharpHighlightContext;
import consulo.csharp.impl.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.impl.psi.source.CSharpSwitchStatementImpl;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeResolveResult;
import consulo.language.psi.PsiElement;
import consulo.util.collection.ArrayUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
	public HighlightInfoFactory checkImpl(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull DotNetExpression element)
	{
		if(languageVersion.isAtLeast(CSharpLanguageVersion._7_0))
		{
			return null;
		}

		if(element.getParent() instanceof CSharpSwitchStatementImpl)
		{
			DotNetTypeRef typeRef = element.toTypeRef(true);

			if(isValidTypeRef(typeRef))
			{
				return null;
			}

			return newBuilder(element, formatTypeRef(typeRef));
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
