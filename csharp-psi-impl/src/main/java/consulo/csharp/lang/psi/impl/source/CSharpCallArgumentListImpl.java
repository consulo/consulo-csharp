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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import consulo.csharp.lang.psi.CSharpCallArgument;
import consulo.csharp.lang.psi.CSharpCallArgumentList;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpFieldOrPropertySet;
import consulo.csharp.lang.psi.CSharpNamedCallArgument;
import consulo.csharp.lang.psi.CSharpTokens;
import com.intellij.lang.ASTNode;
import com.intellij.psi.ContributedReferenceHost;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceService;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.containers.ContainerUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.dotnet.psi.DotNetExpression;

/**
 * @author VISTALL
 * @since 16.12.13.
 */
public class CSharpCallArgumentListImpl extends CSharpElementImpl implements CSharpCallArgumentList, ContributedReferenceHost
{
	private static final TokenSet ourOpenSet = TokenSet.create(CSharpTokens.LPAR, CSharpTokens.LBRACKET);
	private static final TokenSet ourCloseSet = TokenSet.create(CSharpTokens.RPAR, CSharpTokens.RBRACKET);

	public CSharpCallArgumentListImpl(@Nonnull ASTNode node)
	{
		super(node);
	}

	@Nonnull
	@Override
	public PsiReference[] getReferences()
	{
		return PsiReferenceService.getService().getContributedReferences(this);
	}

	@Nullable
	@Override
	public PsiElement getOpenElement()
	{
		return findChildByFilter(ourOpenSet);
	}

	@Nullable
	@Override
	public PsiElement getCloseElement()
	{
		return findChildByFilter(ourCloseSet);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public CSharpCallArgument[] getArguments()
	{
		return findChildrenByClass(CSharpCallArgument.class);
	}

	@Override
	@Nonnull
	public DotNetExpression[] getExpressions()
	{
		CSharpCallArgument[] arguments = getArguments();
		List<DotNetExpression> list = new ArrayList<DotNetExpression>(arguments.length);
		for(CSharpCallArgument callArgument : arguments)
		{
			if(!(callArgument instanceof CSharpNamedCallArgument))
			{
				DotNetExpression argumentExpression = callArgument.getArgumentExpression();
				if(argumentExpression == null)
				{
					continue;
				}
				list.add(argumentExpression);
			}
		}
		return ContainerUtil.toArray(list, DotNetExpression.ARRAY_FACTORY);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitMethodCallParameterList(this);
	}

	@Nonnull
	@Override
	public CSharpFieldOrPropertySet[] getSets()
	{
		return findChildrenByClass(CSharpFieldOrPropertySet.class);
	}
}
