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
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetXAccessor;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.PsiElement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * @author VISTALL
 * @since 15.01.14
 */
public class CSharpLightPropertyDeclaration extends CSharpLightVariable<CSharpPropertyDeclaration> implements CSharpPropertyDeclaration
{
	private final Supplier<DotNetTypeRef> myTypeRef;
	private final Supplier<DotNetTypeRef> myVirtualTypeRefForImpl;

	public CSharpLightPropertyDeclaration(CSharpPropertyDeclaration original, Supplier<DotNetTypeRef> typeRef, Supplier<DotNetTypeRef> virtualTypeRefForImpl)
	{
		super(original);
		myTypeRef = typeRef;
		myVirtualTypeRefForImpl = virtualTypeRefForImpl;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetTypeRef toTypeRef(boolean resolveFromInitializer)
	{
		return myTypeRef.get();
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitPropertyDeclaration(this);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetXAccessor[] getAccessors()
	{
		return myOriginal.getAccessors();
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetNamedElement[] getMembers()
	{
		return myOriginal.getMembers();
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getPresentableParentQName()
	{
		return myOriginal.getPresentableParentQName();
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getPresentableQName()
	{
		return myOriginal.getPresentableQName();
	}

	@RequiredReadAction
	@Override
	public PsiElement getLeftBrace()
	{
		return myOriginal.getLeftBrace();
	}

	@RequiredReadAction
	@Override
	public PsiElement getRightBrace()
	{
		return myOriginal.getRightBrace();
	}

	@Nullable
	@Override
	public DotNetType getTypeForImplement()
	{
		return myOriginal.getTypeForImplement();
	}

	@Nonnull
	@Override
	public DotNetTypeRef getTypeRefForImplement()
	{
		return myVirtualTypeRefForImpl.get();
	}

	@RequiredReadAction
	@Override
	public boolean isAutoGet()
	{
		return myOriginal.isAutoGet();
	}
}
