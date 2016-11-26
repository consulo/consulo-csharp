/*
 * Copyright 2013-2016 must-be.org
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
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpTupleVariable;
import consulo.csharp.lang.psi.impl.source.resolve.type.wrapper.CSharpTupleTypeDeclaration;
import consulo.dotnet.psi.DotNetType;

/**
 * @author VISTALL
 * @since 26-Nov-16.
 */
public class CSharpTupleVariableImpl extends CSharpVariableImpl implements CSharpTupleVariable
{
	public CSharpTupleVariableImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitTupleVariable(this);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public DotNetType getType()
	{
		return findChildByClass(DotNetType.class);
	}

	@Override
	public boolean isEquivalentTo(PsiElement another)
	{
		PsiElement tupleElement = another.getUserData(CSharpTupleTypeDeclaration.TUPLE_ELEMENT);
		if(tupleElement != null && another.isEquivalentTo(tupleElement))
		{
			return true;
		}
		return super.isEquivalentTo(another);
	}
}
