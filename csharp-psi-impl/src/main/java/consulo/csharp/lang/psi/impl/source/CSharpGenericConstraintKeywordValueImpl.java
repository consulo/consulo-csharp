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
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpGenericConstraintKeywordValue;
import consulo.csharp.lang.psi.impl.stub.CSharpWithIntValueStub;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 30.11.13.
 */
public class CSharpGenericConstraintKeywordValueImpl extends CSharpStubElementImpl<CSharpWithIntValueStub<CSharpGenericConstraintKeywordValue>>
		implements CSharpGenericConstraintKeywordValue
{
	public CSharpGenericConstraintKeywordValueImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	public CSharpGenericConstraintKeywordValueImpl(@NotNull CSharpWithIntValueStub<CSharpGenericConstraintKeywordValue> stub,
			@NotNull IStubElementType<? extends CSharpWithIntValueStub<CSharpGenericConstraintKeywordValue>, ?> nodeType)
	{
		super(stub, nodeType);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitGenericConstraintKeywordValue(this);
	}

	@NotNull
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
