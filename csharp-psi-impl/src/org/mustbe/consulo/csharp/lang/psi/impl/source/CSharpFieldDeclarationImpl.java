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
import org.mustbe.consulo.csharp.lang.psi.CSharpFieldDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpStubElements;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpVariableDeclStub;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetFieldDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetModifierList;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 04.12.13.
 */
public class CSharpFieldDeclarationImpl extends CSharpStubVariableImpl<CSharpVariableDeclStub<DotNetFieldDeclaration>> implements CSharpFieldDeclaration
{
	public CSharpFieldDeclarationImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	public CSharpFieldDeclarationImpl(@NotNull CSharpVariableDeclStub<DotNetFieldDeclaration> stub)
	{
		super(stub, CSharpStubElements.FIELD_DECLARATION);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitFieldDeclaration(this);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public DotNetType getType()
	{
		return CSharpStubVariableImplUtil.getType(this);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public DotNetModifierList getModifierList()
	{
		return CSharpStubVariableImplUtil.getModifierList(this);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public PsiElement getConstantKeywordElement()
	{
		return CSharpStubVariableImplUtil.getConstantKeywordElement(this);
	}

	@Nullable
	@Override
	public DotNetExpression getInitializer()
	{
		return findChildByClass(DotNetExpression.class);
	}

	@RequiredReadAction
	@Override
	public boolean isConstant()
	{
		CSharpVariableDeclStub<DotNetFieldDeclaration> stub = getStub();
		if(stub != null)
		{
			return stub.isConstant();
		}
		return CSharpStubVariableImplUtil.getConstantKeywordElement(this) != null;
	}
}
