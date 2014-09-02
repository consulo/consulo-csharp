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

package org.mustbe.consulo.csharp.lang.psi.impl.light;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.impl.light.builder.CSharpLightTypeDeclarationBuilder;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.CSharpTransform;
import org.mustbe.consulo.dotnet.lang.psi.impl.source.resolve.type.DotNetTypeRefByQName;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 02.09.14
 */
public class CSharpLightAttributeWithSelfTypeBuilder extends CSharpAbstractLightAttributeBuilder
{
	private final CSharpLightTypeDeclarationBuilder myType;

	public CSharpLightAttributeWithSelfTypeBuilder(PsiElement scope, String qualifiedName)
	{
		super(scope.getProject());
		myType = new CSharpLightTypeDeclarationBuilder(scope);
		myType.withParentQName(StringUtil.getPackageName(qualifiedName)) ;
		myType.withName(StringUtil.getShortName(qualifiedName));
	}

	@Nullable
	@Override
	public DotNetTypeDeclaration resolveToType()
	{
		return myType;
	}

	@NotNull
	@Override
	public DotNetTypeRef toTypeRef()
	{
		return new DotNetTypeRefByQName(myType.getVmQName(), CSharpTransform.INSTANCE);
	}

	@Override
	public String toString()
	{
		return "CSharpLightAttributeWithSelfTypeBuilder: " + myType.getVmQName();
	}
}
