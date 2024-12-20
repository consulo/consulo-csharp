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

package consulo.csharp.lang.impl.psi.light;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpAttribute;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpTypeRefByQName;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.PsiElement;
import jakarta.annotation.Nonnull;

import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 23.05.14
 */
public class CSharpLightAttributeBuilder extends CSharpAbstractLightAttributeBuilder implements CSharpAttribute
{
	private final String myQualifiedName;

	private final DotNetTypeRef myTypeRef;

	@RequiredReadAction
	public CSharpLightAttributeBuilder(PsiElement scope, String qualifiedName)
	{
		super(scope.getProject());
		myQualifiedName = qualifiedName;
		myTypeRef = new CSharpTypeRefByQName(scope, qualifiedName);
	}

	@Nullable
	@Override
	public DotNetTypeDeclaration resolveToType()
	{
		return (DotNetTypeDeclaration) toTypeRef().resolve().getElement();
	}

	@Nonnull
	@Override
	public DotNetTypeRef toTypeRef()
	{
		return myTypeRef;
	}

	@Override
	public String toString()
	{
		return "CSharpLightAttributeBuilder: " + myQualifiedName;
	}
}
