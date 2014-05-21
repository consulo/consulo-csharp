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
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpDummyDefStub;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IStubElementType;

/**
 * @author VISTALL
 * @since 06.03.14
 */
public class CSharpDummyDeclarationImpl extends CSharpStubMemberImpl<CSharpDummyDefStub>
{
	public static final String DUMMY = "<dummy>";

	public CSharpDummyDeclarationImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	public CSharpDummyDeclarationImpl(@NotNull CSharpDummyDefStub stub, @NotNull IStubElementType<? extends CSharpDummyDefStub, ?> nodeType)
	{
		super(stub, nodeType);
	}

	@Override
	public String getName()
	{
		return DUMMY;
	}

	@Nullable
	@Override
	public PsiElement getNameIdentifier()
	{
		return null;
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitDummyDeclaration(this);
	}
}
