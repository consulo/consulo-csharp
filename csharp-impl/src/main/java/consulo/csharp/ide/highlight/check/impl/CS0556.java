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
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.ide.highlight.CSharpHighlightContext;
import consulo.csharp.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpConversionMethodDeclaration;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.resolve.DotNetTypeRefUtil;
import consulo.dotnet.util.ArrayUtil2;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * @author VISTALL
 * @since 19.08.2015
 */
public class CS0556 extends CompilerCheck<CSharpConversionMethodDeclaration>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull CSharpConversionMethodDeclaration element)
	{
		CSharpTypeDeclaration typeDeclaration = PsiTreeUtil.getParentOfType(element, CSharpTypeDeclaration.class);
		if(typeDeclaration == null)
		{
			return null;
		}

		DotNetTypeRef typeRef1 = element.getReturnTypeRef();
		DotNetTypeRef typeRef2 = ArrayUtil2.safeGet(element.getParameterTypeRefs(), 0);
		if(typeRef2 == null)
		{
			return null;
		}

		if(!DotNetTypeRefUtil.isVmQNameEqual(typeRef1, element, typeDeclaration.getVmQName()) && !DotNetTypeRefUtil
				.isVmQNameEqual(typeRef2, element, typeDeclaration.getVmQName()))
		{
			PsiElement operatorElement = element.getOperatorElement();
			if(operatorElement == null)
			{
				return null;
			}
			return newBuilder(operatorElement);
		}
		return null;
	}
}
