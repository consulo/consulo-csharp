/*
 * Copyright 2013-2021 consulo.io
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

import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.tree.IElementType;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpTupleType;
import consulo.csharp.lang.psi.CSharpTupleVariable;
import consulo.dotnet.psi.DotNetStatement;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 26/12/2021
 */
public class CSharpDeconstructionStatementImpl extends CSharpElementImpl implements DotNetStatement
{
	public CSharpDeconstructionStatementImpl(@Nonnull IElementType elementType)
	{
		super(elementType);
	}

	@Nonnull
	@RequiredReadAction
	public CSharpTupleType getTupleType()
	{
		return findNotNullChildByClass(CSharpTupleType.class);
	}

	@Nonnull
	@RequiredReadAction
	public CSharpTupleVariable[] getVariables()
	{
		CSharpTupleType tupleType = getTupleType();
		return tupleType.getVariables();
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitDeconstructionStatement(this);
	}

	@Override
	@RequiredReadAction
	public boolean processDeclarations(@Nonnull PsiScopeProcessor processor, @Nonnull ResolveState state, PsiElement lastParent, @Nonnull PsiElement place)
	{
		for(CSharpTupleVariable variable : getVariables())
		{
			if(!variable.processDeclarations(processor, state, lastParent, place))
			{
				return false;
			}
		}
		return true;
	}
}
