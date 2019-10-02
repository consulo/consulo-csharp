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

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpBodyWithBraces;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.dotnet.psi.DotNetStatement;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author VISTALL
 * @since 04.01.14.
 */
public class CSharpBlockStatementImpl extends CSharpElementImpl implements DotNetStatement, CSharpBodyWithBraces
{
	public CSharpBlockStatementImpl(@Nonnull ASTNode node)
	{
		super(node);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitBlockStatement(this);
	}

	@RequiredReadAction
	@Override
	public PsiElement getLeftBrace()
	{
		return findChildByType(CSharpTokens.LBRACE);
	}

	@RequiredReadAction
	@Override
	public PsiElement getRightBrace()
	{
		return findChildByType(CSharpTokens.RBRACE);
	}

	@Nonnull
	public DotNetStatement[] getStatements()
	{
		return findChildrenByClass(DotNetStatement.class);
	}

	@Override
	public boolean processDeclarations(@Nonnull PsiScopeProcessor processor, @Nonnull ResolveState state, PsiElement lastParent, @Nonnull PsiElement place)
	{
		DotNetStatement[] statements = getStatements();

		// local methods ignore position in block
		for(DotNetStatement statement : statements)
		{
			if(statement instanceof CSharpLocalMethodDeclarationStatementImpl)
			{
				if(!statement.processDeclarations(processor, state, lastParent, place))
				{
					return false;
				}
			}
		}

		List<DotNetStatement> elements = new ArrayList<>(statements.length);
		for(DotNetStatement statement : statements)
		{
			if(statement == lastParent)
			{
				break;
			}
			elements.add(statement);
		}

		Collections.reverse(elements);

		for(DotNetStatement statement : elements)
		{
			if(statement instanceof CSharpLocalMethodDeclarationStatementImpl)
			{
				continue;
			}

			if(!statement.processDeclarations(processor, state, lastParent, place))
			{
				return false;
			}
		}
		return true;
	}
}
