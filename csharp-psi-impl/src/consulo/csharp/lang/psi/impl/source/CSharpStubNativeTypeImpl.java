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

package consulo.csharp.lang.psi.impl.source;

import org.jetbrains.annotations.NotNull;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpNativeType;
import consulo.csharp.lang.psi.CSharpTokenSets;
import consulo.csharp.lang.psi.impl.stub.CSharpWithIntValueStub;
import consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 13.12.13.
 */
public class CSharpStubNativeTypeImpl extends CSharpStubTypeElementImpl<CSharpWithIntValueStub<CSharpNativeType>> implements CSharpNativeType
{
	public CSharpStubNativeTypeImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	public CSharpStubNativeTypeImpl(@NotNull CSharpWithIntValueStub<CSharpNativeType> stub,
			@NotNull IStubElementType<? extends CSharpWithIntValueStub<CSharpNativeType>, ?> nodeType)
	{
		super(stub, nodeType);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitNativeType(this);
	}

	@RequiredReadAction
	@Override
	@NotNull
	public IElementType getTypeElementType()
	{
		CSharpWithIntValueStub<CSharpNativeType> stub = getStub();
		if(stub != null)
		{
			return CSharpTokenSets.NATIVE_TYPES_AS_ARRAY[stub.getValue()];
		}
		return getTypeElement().getNode().getElementType();
	}

	@RequiredReadAction
	@NotNull
	@Override
	public DotNetTypeRef toTypeRefImpl()
	{
		return CSharpNativeTypeImplUtil.toTypeRef(this);
	}

	@NotNull
	@Override
	public PsiElement getTypeElement()
	{
		return findNotNullChildByFilter(CSharpTokenSets.NATIVE_TYPES);
	}
}
