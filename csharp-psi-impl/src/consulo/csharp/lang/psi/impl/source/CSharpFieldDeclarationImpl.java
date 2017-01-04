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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpFieldDeclaration;
import consulo.csharp.lang.psi.CSharpStubElements;
import consulo.csharp.lang.psi.impl.stub.CSharpVariableDeclStub;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import consulo.annotations.RequiredReadAction;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetFieldDeclaration;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetType;

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
