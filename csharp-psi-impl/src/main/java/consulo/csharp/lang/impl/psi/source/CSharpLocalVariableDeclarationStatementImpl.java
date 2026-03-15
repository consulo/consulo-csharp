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

import consulo.language.psi.resolve.ResolveState;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.ast.IElementType;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpLocalVariable;
import consulo.csharp.lang.psi.CSharpLocalVariableDeclarationStatement;
import consulo.language.psi.PsiElement;


/**
 * @author VISTALL
 * @since 16.12.13.
 */
public class CSharpLocalVariableDeclarationStatementImpl extends CSharpElementImpl implements CSharpLocalVariableDeclarationStatement
{
	public CSharpLocalVariableDeclarationStatementImpl(IElementType elementType)
	{
		super(elementType);
	}

	@Override
	public void accept(CSharpElementVisitor visitor)
	{
		visitor.visitLocalVariableDeclarationStatement(this);
	}

	@Override
	public CSharpLocalVariable[] getVariables()
	{
		return findChildrenByClass(CSharpLocalVariable.class);
	}

	@Override
	public boolean processDeclarations(PsiScopeProcessor processor, ResolveState state, PsiElement lastParent, PsiElement
			place)
	{
		for(CSharpLocalVariable variable : getVariables())
		{
			if(!variable.processDeclarations(processor, state, lastParent, place))
			{
				return false;
			}
		}
		return true;
	}
}
