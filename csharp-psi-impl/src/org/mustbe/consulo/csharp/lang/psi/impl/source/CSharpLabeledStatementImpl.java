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

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetStatement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 06.01.14.
 */
public class CSharpLabeledStatementImpl extends CSharpElementImpl implements DotNetStatement, PsiNameIdentifierOwner, DotNetNamedElement
{
	public CSharpLabeledStatementImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@NotNull
	public DotNetStatement[] getStatements()
	{
		return findChildrenByClass(DotNetStatement.class);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitLabeledStatement(this);
	}

	@NotNull
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
	public PsiElement setName(@NonNls @NotNull String s) throws IncorrectOperationException
	{
		return null;
	}

	@Override
	public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state, PsiElement lastParent, @NotNull PsiElement
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
