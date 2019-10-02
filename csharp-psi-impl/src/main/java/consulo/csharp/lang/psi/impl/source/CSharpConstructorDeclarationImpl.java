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

package consulo.csharp.lang.psi.impl.source;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import consulo.csharp.lang.psi.CSharpConstructorSuperCall;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpStubElements;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import consulo.csharp.lang.psi.impl.stub.CSharpMethodDeclStub;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.util.BitUtil;
import consulo.annotations.RequiredReadAction;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.resolve.DotNetTypeRef;

/**
 * @author VISTALL
 * @since 28.11.13.
 */
public class CSharpConstructorDeclarationImpl extends CSharpStubLikeMethodDeclarationImpl<CSharpMethodDeclStub> implements CSharpConstructorDeclaration
{
	public CSharpConstructorDeclarationImpl(@Nonnull ASTNode node)
	{
		super(node);
	}

	public CSharpConstructorDeclarationImpl(@Nonnull CSharpMethodDeclStub stub, @Nonnull IStubElementType<? extends CSharpMethodDeclStub,
			?> nodeType)
	{
		super(stub, nodeType);
	}

	@Nullable
	@Override
	public CSharpConstructorSuperCall getConstructorSuperCall()
	{
		return findChildByClass(CSharpConstructorSuperCall.class);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetTypeRef getReturnTypeRef()
	{
		return new CSharpTypeRefByQName(this, DotNetTypes.System.Void);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
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
		CSharpMethodDeclStub stub = getGreenStub();
		if(stub != null)
		{
			return BitUtil.isSet(stub.getOtherModifierMask(), CSharpMethodDeclStub.DE_CONSTRUCTOR_MASK);
		}

		return findChildByType(CSharpTokens.TILDE) != null;
	}
}
