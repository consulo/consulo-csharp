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
import org.mustbe.consulo.csharp.lang.psi.impl.msil.CSharpTransform;
import org.mustbe.consulo.dotnet.resolve.DotNetPsiSearcher;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
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

	private final String myPresentableText;
	private final String myWrapperQualifiedClass;

	private CSharpStaticTypeRef(String presentableText, String wrapperQualifiedClass)
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
		return myWrapperQualifiedClass;
	}

	@Override
	public boolean isNullable()
	{
		return false;
	}

	@Nullable
	@Override
	public PsiElement resolve(@NotNull PsiElement scope)
	{
		return DotNetPsiSearcher.getInstance(scope.getProject()).findType(myWrapperQualifiedClass, scope.getResolveScope(),
				DotNetPsiSearcher.TypeResoleKind.UNKNOWN, CSharpTransform.INSTANCE);
	}
}
