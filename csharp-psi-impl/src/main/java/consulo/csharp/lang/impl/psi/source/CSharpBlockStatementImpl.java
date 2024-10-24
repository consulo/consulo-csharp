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

import consulo.annotation.access.RequiredReadAction;
import consulo.application.util.CachedValueProvider;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.impl.psi.resolve.CSharpElementGroupImpl;
import consulo.csharp.lang.psi.CSharpBodyWithBraces;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import consulo.dotnet.psi.DotNetStatement;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiModificationTracker;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.resolve.ResolveState;
import consulo.language.psi.util.LanguageCachedValueUtil;
import consulo.util.collection.MultiMap;

import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author VISTALL
 * @since 04.01.14.
 */
public class CSharpBlockStatementImpl extends CSharpElementImpl implements DotNetStatement, CSharpBodyWithBraces
{
	public CSharpBlockStatementImpl(@Nonnull IElementType elementType)
	{
		super(elementType);
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
		return findPsiChildByType(CSharpTokens.LBRACE);
	}

	@RequiredReadAction
	@Override
	public PsiElement getRightBrace()
	{
		return findPsiChildByType(CSharpTokens.RBRACE);
	}

	@Nonnull
	@RequiredReadAction
	public DotNetStatement[] getStatements()
	{
		return LanguageCachedValueUtil.getCachedValue(this, () -> CachedValueProvider.Result.create(findChildrenByClass(DotNetStatement.class),PsiModificationTracker.MODIFICATION_COUNT));
	}

	@Nonnull
	private Collection<CSharpElementGroup<CSharpMethodDeclaration>> getLocalMethods()
	{
		return LanguageCachedValueUtil.getCachedValue(this, () -> {
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
