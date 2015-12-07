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

import org.consulo.lombok.annotations.ArrayFactoryFields;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpIdentifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpStubElements;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDefStatement;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.EmptyStub;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 11.02.14
 */
@ArrayFactoryFields
public class CSharpTypeDefStatementImpl extends CSharpStubElementImpl<EmptyStub<CSharpTypeDefStatement>> implements CSharpTypeDefStatement
{
	public CSharpTypeDefStatementImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	public CSharpTypeDefStatementImpl(@NotNull EmptyStub<CSharpTypeDefStatement> stub)
	{
		super(stub, CSharpStubElements.TYPE_DEF_STATEMENT);
	}

	@Override
	@RequiredReadAction
	public String getName()
	{
		CSharpIdentifier nameIdentifier = getNameIdentifier();
		return nameIdentifier == null ? null : nameIdentifier.getValue();
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitTypeDefStatement(this);
	}

	@Override
	public PsiElement setName(@NonNls @NotNull String s) throws IncorrectOperationException
	{
		return null;
	}

	@RequiredReadAction
	@Override
	@Nullable
	public DotNetType getType()
	{
		return getStubOrPsiChildByIndex(CSharpStubElements.TYPE_SET, 0);
	}

	@RequiredReadAction
	@Override
	@NotNull
	public DotNetTypeRef toTypeRef()
	{
		DotNetType type = getType();
		return type == null ? DotNetTypeRef.ERROR_TYPE : type.toTypeRef();
	}

	@Nullable
	@Override
	@RequiredReadAction
	public CSharpIdentifier getNameIdentifier()
	{
		return getStubOrPsiChild(CSharpStubElements.IDENTIFIER);
	}

	@Nullable
	@Override
	@RequiredReadAction
	public PsiElement getReferenceElement()
	{
		return getType();
	}

	@RequiredReadAction
	@Override
	public int getTextOffset()
	{
		PsiElement nameIdentifier = getNameIdentifier();
		return nameIdentifier == null ? super.getTextOffset() : nameIdentifier.getTextOffset();
	}
}
