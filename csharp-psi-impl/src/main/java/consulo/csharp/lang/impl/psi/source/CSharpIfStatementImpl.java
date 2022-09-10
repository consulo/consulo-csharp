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

import consulo.language.psi.PsiElement;
import consulo.language.ast.IElementType;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetStatement;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.resolve.ResolveState;
import consulo.util.collection.ArrayUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 04.01.14.
 */
public class CSharpIfStatementImpl extends CSharpElementImpl implements DotNetStatement
{
	public CSharpIfStatementImpl(@Nonnull IElementType elementType)
	{
		super(elementType);
	}

	@Nullable
	public DotNetExpression getConditionExpression()
	{
		return findChildByClass(DotNetExpression.class);
	}

	@Nonnull
	public PsiElement getIfKeywordElement()
	{
		return findNotNullChildByType(CSharpTokens.IF_KEYWORD);
	}

	@Nullable
	public PsiElement getElseKeywordElement()
	{
		return findPsiChildByType(CSharpTokens.ELSE_KEYWORD);
	}

	@Nullable
	public DotNetStatement getTrueStatement()
	{
		DotNetStatement[] childrenByClass = findChildrenByClass(DotNetStatement.class);
		if(childrenByClass.length == 2)
		{
			return childrenByClass[0];
		}
		DotNetStatement firstElement = ArrayUtil.getFirstElement(childrenByClass);
		if(firstElement == null || firstElement == getFalseStatement())
		{
			return null;
		}
		return firstElement;
	}

	@Nullable
	public DotNetStatement getFalseStatement()
	{
		PsiElement elseKeywordElement = getElseKeywordElement();
		if(elseKeywordElement == null)
		{
			return null;
		}

		DotNetStatement[] childrenByClass = findChildrenByClass(DotNetStatement.class);
		if(childrenByClass.length == 2)
		{
			return childrenByClass[1];
		}
		if(childrenByClass.length == 1)
		{
			DotNetStatement statement = childrenByClass[0];
			// for example statement like 'if() {} else <error>' need check offset, or we ill return true statement as false statement
			if(statement.getTextOffset() > elseKeywordElement.getTextOffset())
			{
				return statement;
			}
		}
		return null;
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitIfStatement(this);
	}

	@Override
	public boolean processDeclarations(@Nonnull PsiScopeProcessor processor, @Nonnull ResolveState state, PsiElement lastParent, @Nonnull PsiElement place)
	{
		DotNetExpression conditionExpression = getConditionExpression();
		if(conditionExpression != null)
		{
			if(!conditionExpression.processDeclarations(processor, state, lastParent, place))
			{
				return false;
			}
		}
		return super.processDeclarations(processor, state, lastParent, place);
	}
}
