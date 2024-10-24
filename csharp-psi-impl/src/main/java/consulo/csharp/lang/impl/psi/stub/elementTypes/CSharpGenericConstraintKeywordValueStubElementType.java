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

package consulo.csharp.lang.impl.psi.stub.elementTypes;

import java.io.IOException;

import jakarta.annotation.Nonnull;

import consulo.csharp.lang.psi.CSharpGenericConstraintKeywordValue;
import consulo.csharp.lang.impl.psi.source.CSharpGenericConstraintKeywordValueImpl;
import consulo.csharp.lang.impl.psi.stub.CSharpWithIntValueStub;
import consulo.language.ast.ASTNode;
import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubOutputStream;
import consulo.language.psi.PsiElement;
import consulo.language.psi.stub.StubInputStream;
import consulo.util.collection.ArrayUtil;

/**
 * @author VISTALL
 * @since 25.10.14
 */
public class CSharpGenericConstraintKeywordValueStubElementType extends
		CSharpAbstractStubElementType<CSharpWithIntValueStub<CSharpGenericConstraintKeywordValue>, CSharpGenericConstraintKeywordValue>
{
	public CSharpGenericConstraintKeywordValueStubElementType()
	{
		super("GENERIC_CONSTRAINT_KEYWORD_VALUE");
	}

	@Nonnull
	@Override
	public PsiElement createElement(@Nonnull ASTNode astNode)
	{
		return new CSharpGenericConstraintKeywordValueImpl(astNode);
	}

	@Override
	public CSharpGenericConstraintKeywordValue createPsi(@Nonnull CSharpWithIntValueStub<CSharpGenericConstraintKeywordValue> stub)
	{
		return new CSharpGenericConstraintKeywordValueImpl(stub, this);
	}

	@Override
	public CSharpWithIntValueStub<CSharpGenericConstraintKeywordValue> createStub(@Nonnull CSharpGenericConstraintKeywordValue keywordValue,
			StubElement stubElement)
	{
		int index = ArrayUtil.indexOf(CSharpGenericConstraintKeywordValue.KEYWORDS_AS_ARRAY, keywordValue.getKeywordElementType());
		assert index != -1;
		return new CSharpWithIntValueStub<CSharpGenericConstraintKeywordValue>(stubElement, this, index);
	}

	@Override
	public void serialize(@Nonnull CSharpWithIntValueStub<CSharpGenericConstraintKeywordValue> stub,
			@Nonnull StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeVarInt(stub.getValue());
	}

	@Nonnull
	@Override
	public CSharpWithIntValueStub<CSharpGenericConstraintKeywordValue> deserialize(@Nonnull StubInputStream stubInputStream,
			StubElement stubElement) throws IOException
	{
		int index = stubInputStream.readVarInt();
		return new CSharpWithIntValueStub<CSharpGenericConstraintKeywordValue>(stubElement, this, index);
	}
}
