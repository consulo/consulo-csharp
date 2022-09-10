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

package consulo.csharp.lang.impl.psi.source;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpIdentifier;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.impl.psi.stub.CSharpIdentifierStub;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.stub.IStubElementType;

/**
 * @author VISTALL
 * @since 26.07.2015
 */
public class CSharpStubIdentifierImpl extends CSharpStubElementImpl<CSharpIdentifierStub> implements CSharpIdentifier
{
	public CSharpStubIdentifierImpl(@Nonnull ASTNode node)
	{
		super(node);
	}

	public CSharpStubIdentifierImpl(@Nonnull CSharpIdentifierStub stub, @Nonnull IStubElementType<? extends CSharpIdentifierStub, ?> nodeType)
	{
		super(stub, nodeType);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitIdentifier(this);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getValue()
	{
		CSharpIdentifierStub stub = getGreenStub();
		if(stub != null)
		{
			return stub.getValue();
		}

		PsiElement psiElement = findChildByType(CSharpTokens.IDENTIFIER);
		return psiElement != null ? psiElement.getText() : null;
	}
}
