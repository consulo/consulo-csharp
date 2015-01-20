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
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpStubElements;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.psi.DotNetTypeList;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.psi.PsiElement;
import com.intellij.util.ArrayUtil;

/**
 * @author VISTALL
 * @since 20.01.15
 */
public class CS1722 extends CompilerCheck<DotNetTypeList>
{
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull DotNetTypeList element)
	{
		if(element.getNode().getElementType() != CSharpStubElements.EXTENDS_LIST)
		{
			return null;
		}

		CSharpTypeDeclaration resolvedElement = null;
		DotNetType baseType = null;
		DotNetType[] types = element.getTypes();

		for(DotNetType type : types)
		{
			DotNetTypeRef typeRef = type.toTypeRef();
			PsiElement temp = typeRef.resolve(element).getElement();
			if(temp instanceof CSharpTypeDeclaration && !((CSharpTypeDeclaration) temp).isInterface())
			{
				resolvedElement = (CSharpTypeDeclaration) temp;
				baseType = type;
				break;
			}
		}

		if(baseType == null)
		{
			return null;
		}
		int i = ArrayUtil.indexOf(types, baseType);
		if(i != 0)
		{
			CSharpTypeDeclaration parent = (CSharpTypeDeclaration) element.getParent();
			return newBuilder(baseType, formatElement(parent), formatElement(resolvedElement));
		}
		return null;
	}
}
