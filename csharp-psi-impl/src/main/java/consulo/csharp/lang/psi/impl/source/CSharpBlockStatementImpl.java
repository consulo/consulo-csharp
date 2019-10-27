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
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.util.containers.MultiMap;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpBodyWithBraces;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.psi.impl.resolve.CSharpElementGroupImpl;
import consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import consulo.dotnet.psi.DotNetStatement;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

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
	@RequiredReadAction
	public DotNetStatement[] getStatements()
	{
		return CachedValuesManager.getCachedValue(this, () -> CachedValueProvider.Result.create(findChildrenByClass(DotNetStatement.class), PsiModificationTracker.MODIFICATION_COUNT));
	}

	@Nonnull
	private Collection<CSharpElementGroup<CSharpMethodDeclaration>> getLocalMethods()
	{
		return CachedValuesManager.getCachedValue(this, () -> {
			MultiMap<String, CSharpMethodDeclaration> multiMap = new MultiMap<>();

			for(DotNetStatement statement : getStatements())
			{
				if(statement instanceof CSharpLocalMethodDeclarationStatementImpl)
				{
					CSharpMethodDeclaration method = ((CSharpLocalMethodDeclarationStatementImpl) statement).getMethod();

					multiMap.putValue(method.getName(), method);
				}
			}

			List<CSharpElementGroup<CSharpMethodDeclaration>> list = multiMap
					.entrySet()
					.stream()
					.map(e -> new CSharpElementGroupImpl<>(getProject(), e.getKey(), e.getValue()))
					.collect(Collectors.toList());

			return CachedValueProvider.Result.create(list, PsiModificationTracker.MODIFICATION_COUNT);
		});
	}

	@Override
	public boolean processDeclarations(@Nonnull PsiScopeProcessor processor, @Nonnull ResolveState state, PsiElement lastParent, @Nonnull PsiElement place)
	{
		DotNetStatement[] statements = getStatements();

		// local methods ignore position in block
		for(CSharpElementGroup<CSharpMethodDeclaration> group : getLocalMethods())
		{
			if(!processor.execute(group, state))
			{
				return false;
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
