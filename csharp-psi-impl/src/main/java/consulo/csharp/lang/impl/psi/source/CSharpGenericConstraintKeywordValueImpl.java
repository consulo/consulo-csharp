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

import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpGenericConstraintKeywordValue;
import consulo.csharp.lang.impl.psi.stub.CSharpWithIntValueStub;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElement;
import consulo.language.psi.stub.IStubElementType;

/**
 * @author VISTALL
 * @since 30.11.13.
 */
public class CSharpGenericConstraintKeywordValueImpl extends CSharpStubElementImpl<CSharpWithIntValueStub<CSharpGenericConstraintKeywordValue>>
		implements CSharpGenericConstraintKeywordValue
{
	public CSharpGenericConstraintKeywordValueImpl(@Nonnull ASTNode node)
	{
		super(node);
	}

	public CSharpGenericConstraintKeywordValueImpl(@Nonnull CSharpWithIntValueStub<CSharpGenericConstraintKeywordValue> stub,
			@Nonnull IStubElementType<? extends CSharpWithIntValueStub<CSharpGenericConstraintKeywordValue>, ?> nodeType)
	{
		super(stub, nodeType);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitGenericConstraintKeywordValue(this);
	}

	@Nonnull
	@Override
	public IElementType getKeywordElementType()
	{
		CSharpWithIntValueStub<CSharpGenericConstraintKeywordValue> stub = getGreenStub();
		if(stub != null)
		{
			return KEYWORDS_AS_ARRAY[stub.getValue()];
		}
		PsiElement element = findNotNullChildByType(KEYWORDS);
		return element.getNode().getElementType();
	}
}
