/*
 * Copyright 2013-2014 must-be.org
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

package org.mustbe.consulo.csharp.lang.psi.impl.source;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetStatement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.util.ArrayUtil;

/**
 * @author VISTALL
 * @since 04.01.14.
 */
public class CSharpIfStatementImpl extends CSharpElementImpl implements DotNetStatement
{
	public CSharpIfStatementImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@Nullable
	public DotNetExpression getConditionExpression()
	{
		return findChildByClass(DotNetExpression.class);
	}

	@NotNull
	public PsiElement getIfKeywordElement()
	{
		return findNotNullChildByType(CSharpTokens.IF_KEYWORD);
	}

	@Nullable
	public PsiElement getElseKeywordElement()
	{
		return findChildByType(CSharpTokens.ELSE_KEYWORD);
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
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitIfStatement(this);
	}
}
