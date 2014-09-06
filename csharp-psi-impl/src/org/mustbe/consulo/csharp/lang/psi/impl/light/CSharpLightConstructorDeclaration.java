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
import org.mustbe.consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.CSharpTransform;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.lang.psi.impl.source.resolve.type.DotNetTypeRefByQName;
import org.mustbe.consulo.dotnet.psi.DotNetParameterList;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 15.01.14
 */
public class CSharpLightConstructorDeclaration extends CSharpLightLikeMethodDeclaration<CSharpConstructorDeclaration> implements
		CSharpConstructorDeclaration
{
	public CSharpLightConstructorDeclaration(CSharpConstructorDeclaration original, @Nullable DotNetParameterList parameterList)
	{
		super(original, parameterList);
	}

	@NotNull
	@Override
	public DotNetTypeRef getReturnTypeRef()
	{
		return new DotNetTypeRefByQName(DotNetTypes.System.Void, CSharpTransform.INSTANCE, false);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitConstructorDeclaration(this);
	}

	@Override
	public boolean isDeConstructor()
	{
		return myOriginal.isDeConstructor();
	}

	@Nullable
	@Override
	public PsiElement getNameIdentifier()
	{
		return null;
	}
}
