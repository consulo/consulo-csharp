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

import consulo.language.psi.scope.GlobalSearchScope;
import consulo.project.Project;
import consulo.language.psi.PsiElement;
import consulo.language.ast.IElementType;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.csharp.lang.impl.ide.refactoring.CSharpRefactoringUtil;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpIdentifier;
import consulo.csharp.lang.psi.CSharpLinqVariable;
import consulo.csharp.lang.impl.psi.DotNetTypes2;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpGenericWrapperTypeRef;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpTypeRefByQName;
import consulo.csharp.lang.impl.psi.source.resolve.util.CSharpResolveUtil;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.util.IncorrectOperationException;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 29.11.14
 */
public class CSharpLinqVariableImpl extends CSharpElementImpl implements CSharpLinqVariable
{
	private static final CSharpTypeRefCacher<CSharpLinqVariableImpl> ourCacheSystem = new CSharpTypeRefCacher<CSharpLinqVariableImpl>(true)
	{
		@RequiredReadAction
		@Nonnull
		@Override
		protected DotNetTypeRef toTypeRefImpl(CSharpLinqVariableImpl element, boolean resolveFromParentOrInitializer)
		{
			DotNetType type = element.getType();
			if(type != null)
			{
				return type.toTypeRef();
			}
			return resolveFromParentOrInitializer ? element.resolveTypeRef() : DotNetTypeRef.AUTO_TYPE;
		}
	};

	public CSharpLinqVariableImpl(@Nonnull IElementType elementType)
	{
		super(elementType);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitLinqVariable(this);
	}

	@RequiredReadAction
	@Override
	public boolean isConstant()
	{
		return false;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public PsiElement getConstantKeywordElement()
	{
		return null;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetTypeRef toTypeRef(boolean resolve)
	{
		return ourCacheSystem.toTypeRef(this, resolve);
	}

	@Nonnull
	@RequiredReadAction
	private DotNetTypeRef resolveTypeRef()
	{
		PsiElement parent = getParent();

		if(parent instanceof CSharpLinqFromClauseImpl)
		{
			DotNetExpression inExpression = ((CSharpLinqFromClauseImpl) parent).getInExpression();
			if(inExpression == null)
			{
				return DotNetTypeRef.ERROR_TYPE;
			}

			DotNetTypeRef typeRef = inExpression.toTypeRef(true);

			return CSharpResolveUtil.resolveIterableType(typeRef);
		}
		else if(parent instanceof CSharpLinqLetClauseImpl)
		{
			DotNetExpression initializer = getInitializer();
			return initializer != null ? initializer.toTypeRef(false) : DotNetTypeRef.AUTO_TYPE;
		}
		else if(parent instanceof CSharpLinqJoinClauseImpl)
		{
			DotNetExpression inExpression = ((CSharpLinqJoinClauseImpl) parent).getInExpression();
			if(inExpression == null)
			{
				return DotNetTypeRef.ERROR_TYPE;
			}

			DotNetTypeRef typeRef = inExpression.toTypeRef(true);

			return CSharpResolveUtil.resolveIterableType(typeRef);
		}
		else if(parent instanceof CSharpLinqIntoClauseImpl)
		{
			PsiElement nextParent = parent.getParent();
			if(nextParent instanceof CSharpLinqJoinClauseImpl)
			{
				CSharpLinqVariableImpl variable = ((CSharpLinqJoinClauseImpl) nextParent).getVariable();
				if(variable != null)
				{
					Project project = getProject();
					GlobalSearchScope resolveScope = getResolveScope();

					return new CSharpGenericWrapperTypeRef(project, resolveScope, new CSharpTypeRefByQName(project, resolveScope, DotNetTypes2.System.Collections.Generic.IEnumerable$1), variable.toTypeRef(true));
				}
			}
			else if(nextParent instanceof CSharpLinqQueryContinuationImpl)
			{
				CSharpLinqQueryBodyImpl body = (CSharpLinqQueryBodyImpl) nextParent.getParent();

				PsiElement bodyParent = body.getParent();
				if(bodyParent instanceof CSharpLinqExpressionImpl)
				{
					CSharpLinqSelectOrGroupClauseImpl selectOrGroupClause = body.getSelectOrGroupClause();
					if(selectOrGroupClause != null)
					{
						DotNetExpression firstExpression = selectOrGroupClause.getFirstExpression();
						if(firstExpression != null)
						{
							return firstExpression.toTypeRef(true);
						}
					}
	
					CSharpLinqFromClauseImpl fromClause = ((CSharpLinqExpressionImpl) bodyParent).getFromClause();
					CSharpLinqVariableImpl variable = fromClause.getVariable();
					if(variable != null)
					{
						return variable.toTypeRef(true);
					}
				}
				else if(bodyParent instanceof CSharpLinqQueryContinuationImpl)
				{
					DotNetTypeRef bodyTypeRef = body.calcTypeRef(true);
					return bodyTypeRef == DotNetTypeRef.ERROR_TYPE ? DotNetTypeRef.ERROR_TYPE : CSharpResolveUtil.resolveIterableType(bodyTypeRef);
				}
				return DotNetTypeRef.ERROR_TYPE;
			}
		}
		return DotNetTypeRef.AUTO_TYPE;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public DotNetType getType()
	{
		return findChildByClass(DotNetType.class);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public DotNetExpression getInitializer()
	{
		return findChildByClass(DotNetExpression.class);
	}

	@RequiredReadAction
	@Override
	public boolean hasModifier(@Nonnull DotNetModifier modifier)
	{
		return false;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public DotNetModifierList getModifierList()
	{
		return null;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public CSharpIdentifier getNameIdentifier()
	{
		return findChildByClass(CSharpIdentifier.class);
	}

	@RequiredReadAction
	@Override
	public String getName()
	{
		CSharpIdentifier nameIdentifier = getNameIdentifier();
		return nameIdentifier == null ? null : CSharpPsiUtilImpl.getNameWithoutAt(nameIdentifier.getValue());
	}

	@RequiredWriteAction
	@Override
	public PsiElement setName(@Nonnull String name) throws IncorrectOperationException
	{
		CSharpRefactoringUtil.replaceNameIdentifier(this, name);
		return this;
	}
}
