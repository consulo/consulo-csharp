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
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpStubElements;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpQualifiedTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpVariableStub;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetFieldDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * @author VISTALL
 * @since 08.01.14.
 */
public class CSharpEnumConstantDeclarationImpl extends CSharpStubVariableImpl<CSharpVariableStub<CSharpEnumConstantDeclarationImpl>> implements
		DotNetFieldDeclaration
{
	public CSharpEnumConstantDeclarationImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	public CSharpEnumConstantDeclarationImpl(@NotNull CSharpVariableStub<CSharpEnumConstantDeclarationImpl> stub)
	{
		super(stub, CSharpStubElements.ENUM_CONSTANT_DECLARATION);
	}

	@Override
	public boolean hasModifier(@NotNull DotNetModifier modifier)
	{
		if(modifier == CSharpModifier.STATIC || modifier == CSharpModifier.READONLY)
		{
			return true;
		}
		return false;
	}

	@Override
	public boolean isConstant()
	{
		return true;
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitEnumConstantDeclaration(this);
	}

	@Nullable
	@Override
	public DotNetType getType()
	{
		return null;
	}

	@NotNull
	@Override
	public DotNetTypeRef toTypeRef(boolean resolveFromInitializer)
	{
		DotNetTypeDeclaration parentOfType = PsiTreeUtil.getParentOfType(this, DotNetTypeDeclaration.class);
		assert parentOfType != null;

		DotNetTypeRef[] anExtends = parentOfType.getExtendTypeRefs();
		if(anExtends.length > 0)
		{
			return anExtends[0];
		}
		return new CSharpQualifiedTypeRef(parentOfType);
	}

	@Nullable
	@Override
	public DotNetExpression getInitializer()
	{
		return findChildByClass(DotNetExpression.class);
	}
}
