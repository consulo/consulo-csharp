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

package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.CSharpTransform;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetPsiSearcher;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import org.mustbe.consulo.dotnet.resolve.SimpleTypeResolveResult;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 29.12.13.
 */
public class CSharpStaticTypeRef extends DotNetTypeRef.Adapter
{
	public static final CSharpStaticTypeRef IMPLICIT = new CSharpStaticTypeRef("implicit", "System.Object");
	public static final CSharpStaticTypeRef EXPLICIT = new CSharpStaticTypeRef("explicit", "System.Object");
	public static final CSharpStaticTypeRef DYNAMIC = new CSharpStaticTypeRef("dynamic", "System.Object");
	public static final CSharpStaticTypeRef __ARGLIST_TYPE = new CSharpStaticTypeRef("__arglist", null);

	private final String myPresentableText;
	private final String myWrapperQualifiedClass;

	private CSharpStaticTypeRef(String presentableText, @Nullable String wrapperQualifiedClass)
	{
		myPresentableText = presentableText;
		myWrapperQualifiedClass = wrapperQualifiedClass;
	}

	@NotNull
	@Override
	public String getPresentableText()
	{
		return myPresentableText;
	}

	@NotNull
	@Override
	public String getQualifiedText()
	{
		if(myWrapperQualifiedClass == null)
		{
			return getPresentableText();
		}
		return myWrapperQualifiedClass;
	}

	@RequiredReadAction
	@NotNull
	@Override
	public DotNetTypeResolveResult resolve(@NotNull PsiElement scope)
	{
		if(myWrapperQualifiedClass == null)
		{
			return DotNetTypeResolveResult.EMPTY;
		}
		DotNetTypeDeclaration type = DotNetPsiSearcher.getInstance(scope.getProject()).findType(myWrapperQualifiedClass, scope.getResolveScope(), CSharpTransform.INSTANCE);
		if(type == null)
		{
			return DotNetTypeResolveResult.EMPTY;
		}
		return new SimpleTypeResolveResult(type, DotNetGenericExtractor.EMPTY, true);
	}
}
