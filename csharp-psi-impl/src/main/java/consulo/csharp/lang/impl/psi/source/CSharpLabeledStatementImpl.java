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

import consulo.language.ast.IElementType;
import consulo.language.psi.PsiNameIdentifierOwner;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetStatement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.resolve.ResolveState;
import consulo.language.util.IncorrectOperationException;


/**
 * @author VISTALL
 * @since 06.01.14.
 */
public class CSharpLabeledStatementImpl extends CSharpElementImpl implements DotNetStatement, PsiNameIdentifierOwner, DotNetNamedElement
{
	public CSharpLabeledStatementImpl(IElementType elementType)
	{
		super(elementType);
	}

	public DotNetStatement[] getStatements()
	{
		return findChildrenByClass(DotNetStatement.class);
	}

	@Override
	public void accept(CSharpElementVisitor visitor)
	{
		visitor.visitLabeledStatement(this);
	}

	@Override
	public PsiElement getNameIdentifier()
	{
		return findNotNullChildByType(CSharpTokens.IDENTIFIER);
	}

	@Override
	public String getName()
	{
		PsiElement nameIdentifier = getNameIdentifier();
		return nameIdentifier.getText();
	}

	@Override
	public PsiElement setName(String s) throws IncorrectOperationException
	{
		return null;
	}

	@Override
	public boolean processDeclarations(PsiScopeProcessor processor, ResolveState state, PsiElement lastParent, PsiElement
			place)
	{
		for(DotNetStatement statement : getStatements())
		{
			if(!statement.processDeclarations(processor, state, lastParent, place))
			{
				return false;
			}
		}
		return true;
	}
}
