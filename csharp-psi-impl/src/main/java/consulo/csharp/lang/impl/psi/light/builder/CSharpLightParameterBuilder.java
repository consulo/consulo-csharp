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

package consulo.csharp.lang.impl.psi.light.builder;

import consulo.language.psi.PsiElement;
import consulo.project.Project;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpRefTypeRef;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetParameterListOwner;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 08.05.14
 */
public class CSharpLightParameterBuilder extends CSharpLightVariableBuilder<CSharpLightParameterBuilder> implements DotNetParameter
{
	private DotNetLikeMethodDeclaration myMethod;

	public CSharpLightParameterBuilder(Project project)
	{
		super(project);
	}

	public CSharpLightParameterBuilder(PsiElement element)
	{
		super(element);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetTypeRef toTypeRef(boolean resolveFromInitializer)
	{
		DotNetTypeRef typeRef = super.toTypeRef(resolveFromInitializer);
		if(hasModifier(CSharpModifier.REF))
		{
			return new CSharpRefTypeRef(getProject(), getResolveScope(), CSharpRefTypeRef.Type.ref, typeRef);
		}
		else if(hasModifier(CSharpModifier.OUT))
		{
			return new CSharpRefTypeRef(getProject(), getResolveScope(), CSharpRefTypeRef.Type.out, typeRef);
		}
		return typeRef;
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitParameter(this);
	}

	@Nullable
	@Override
	public DotNetParameterListOwner getOwner()
	{
		return myMethod;
	}

	@Override
	public int getIndex()
	{
		return 0;
	}

	public void setMethod(DotNetLikeMethodDeclaration method)
	{
		myMethod = method;
	}
}
