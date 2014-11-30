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
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.reflactoring.CSharpRefactoringUtil;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpLinqVariable;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.DotNetTypes2;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpGenericWrapperTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierList;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 29.11.14
 */
public class CSharpLinqVariableImpl extends CSharpElementImpl implements CSharpLinqVariable
{
	public CSharpLinqVariableImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitLinqVariable(this);
	}

	@Override
	public boolean isConstant()
	{
		return false;
	}

	@NotNull
	@Override
	public DotNetTypeRef toTypeRef(boolean resolve)
	{
		DotNetType type = getType();
		if(type != null)
		{
			return type.toTypeRef();
		}
		return resolve ? resolveTypeRef() : DotNetTypeRef.AUTO_TYPE;
	}

	@NotNull
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

			return CSharpResolveUtil.resolveIterableType(this, typeRef);
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

			return CSharpResolveUtil.resolveIterableType(this, typeRef);
		}
		else if(parent instanceof CSharpLinqIntoClauseImpl)
		{
			PsiElement nextParent = parent.getParent();
			if(nextParent instanceof CSharpLinqJoinClauseImpl)
			{
				CSharpLinqVariableImpl variable = ((CSharpLinqJoinClauseImpl) nextParent).getVariable();
				if(variable != null)
				{
					return new CSharpGenericWrapperTypeRef(new CSharpTypeRefByQName(DotNetTypes2.System.Collections.Generic.IEnumerable$1),
							new DotNetTypeRef[]{variable.toTypeRef(true)});
				}
			}
		}
		return DotNetTypeRef.AUTO_TYPE;
	}

	@Nullable
	@Override
	public DotNetType getType()
	{
		return findChildByClass(DotNetType.class);
	}

	@Nullable
	@Override
	public DotNetExpression getInitializer()
	{
		return findChildByClass(DotNetExpression.class);
	}

	@Override
	public boolean hasModifier(@NotNull DotNetModifier modifier)
	{
		return false;
	}

	@Nullable
	@Override
	public DotNetModifierList getModifierList()
	{
		return null;
	}

	@Nullable
	@Override
	public PsiElement getNameIdentifier()
	{
		return findChildByType(CSharpTokens.IDENTIFIER);
	}

	@Override
	public String getName()
	{
		PsiElement nameIdentifier = getNameIdentifier();
		return nameIdentifier == null ? null : nameIdentifier.getText();
	}

	@Override
	public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException
	{
		CSharpRefactoringUtil.replaceNameIdentifier(this, name);
		return this;
	}
}
