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
import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefFromQualifiedName;
import org.mustbe.consulo.dotnet.psi.DotNetAttribute;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetPsiFacade;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.light.LightElement;

/**
 * @author VISTALL
 * @since 23.05.14
 */
public class CSharpLightAttributeBuilder extends LightElement implements DotNetAttribute
{
	private final PsiElement myScope;
	private final String myQualifiedName;

	public CSharpLightAttributeBuilder(PsiElement scope, String qualifiedName)
	{
		super(PsiManager.getInstance(scope.getProject()), CSharpLanguage.INSTANCE);
		myScope = scope;
		myQualifiedName = qualifiedName;
	}

	@Nullable
	@Override
	public DotNetTypeDeclaration resolveToType()
	{
		return DotNetPsiFacade.getInstance(myScope.getProject()).findType(myQualifiedName, myScope.getResolveScope(), 0);
	}

	@NotNull
	@Override
	public DotNetTypeRef toTypeRef()
	{
		return new CSharpTypeRefFromQualifiedName(myQualifiedName, 0);
	}

	@Override
	public String toString()
	{
		return "CSharpLightAttributeBuilder: " + myQualifiedName;
	}
}
