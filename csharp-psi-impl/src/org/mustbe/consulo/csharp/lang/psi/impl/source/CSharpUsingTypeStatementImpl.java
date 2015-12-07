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
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpStubElements;
import org.mustbe.consulo.csharp.lang.psi.CSharpUsingTypeStatement;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.EmptyStub;
import com.intellij.psi.stubs.IStubElementType;

/**
 * @author VISTALL
 * @since 11.12.14
 */
public class CSharpUsingTypeStatementImpl extends CSharpStubElementImpl<EmptyStub<CSharpUsingTypeStatement>> implements CSharpUsingTypeStatement
{
	public CSharpUsingTypeStatementImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	public CSharpUsingTypeStatementImpl(@NotNull EmptyStub<CSharpUsingTypeStatement> stub,
			@NotNull IStubElementType<? extends EmptyStub<CSharpUsingTypeStatement>, ?> nodeType)
	{
		super(stub, nodeType);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitUsingTypeStatement(this);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public PsiElement getReferenceElement()
	{
		return getType();
	}

	@NotNull
	@Override
	public DotNetTypeRef getTypeRef()
	{
		DotNetType type = getType();
		if(type == null)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}
		return type.toTypeRef();
	}

	@Nullable
	@Override
	public DotNetType getType()
	{
		return getStubOrPsiChildByIndex(CSharpStubElements.TYPE_SET, 0);
	}
}
