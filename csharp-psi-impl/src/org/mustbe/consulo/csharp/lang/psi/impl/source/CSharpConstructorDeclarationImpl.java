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

package org.mustbe.consulo.csharp.lang.psi.impl.source;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpStubElements;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpMethodDeclStub;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.util.BitUtil;

/**
 * @author VISTALL
 * @since 28.11.13.
 */
public class CSharpConstructorDeclarationImpl extends CSharpLikeMethodDeclarationImpl<CSharpMethodDeclStub> implements CSharpConstructorDeclaration
{
	public CSharpConstructorDeclarationImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	public CSharpConstructorDeclarationImpl(@NotNull CSharpMethodDeclStub stub, @NotNull IStubElementType<? extends CSharpMethodDeclStub,
			?> nodeType)
	{
		super(stub, nodeType);
	}

	@NotNull
	@Override
	public DotNetTypeRef getReturnTypeRef()
	{
		return new CSharpTypeRefByQName(DotNetTypes.System.Void);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitConstructorDeclaration(this);
	}

	@RequiredReadAction
	@Override
	@Nullable
	public PsiElement getNameIdentifier()
	{
		return getStubOrPsiChild(CSharpStubElements.IDENTIFIER);
	}

	@Override
	public boolean isDeConstructor()
	{
		CSharpMethodDeclStub stub = getStub();
		if(stub != null)
		{
			return BitUtil.isSet(stub.getOtherModifierMask(), CSharpMethodDeclStub.DE_CONSTRUCTOR_MASK);
		}

		return findChildByType(CSharpTokens.TILDE) != null;
	}
}
