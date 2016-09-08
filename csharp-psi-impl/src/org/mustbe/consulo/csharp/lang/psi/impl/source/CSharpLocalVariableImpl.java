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

import java.util.List;

import consulo.lombok.annotations.Logger;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.refactoring.CSharpRefactoringUtil;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpIdentifier;
import consulo.csharp.lang.psi.CSharpLocalVariable;
import org.mustbe.consulo.csharp.lang.psi.CSharpLocalVariableDeclarationStatement;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.SmartList;

/**
 * @author VISTALL
 * @since 16.12.13.
 */
@Logger
public class CSharpLocalVariableImpl extends CSharpVariableImpl implements CSharpLocalVariable
{
	public CSharpLocalVariableImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitLocalVariable(this);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public DotNetType getType()
	{
		DotNetType type = findChildByClass(DotNetType.class);
		// int a, b
		if(type == null && getNameIdentifier() != null)
		{
			CSharpLocalVariableImpl localVariable = PsiTreeUtil.getPrevSiblingOfType(this, CSharpLocalVariableImpl.class);
			assert localVariable != null;
			return localVariable.getType();
		}
		return type;
	}

	@RequiredReadAction
	@NotNull
	@Override
	public DotNetTypeRef toTypeRefImpl(boolean resolveFromInitializer)
	{
		PsiElement parent = getParent();
		if(parent instanceof CSharpForeachStatementImpl)
		{
			DotNetType type = getType();
			if(type == null)
			{
				return DotNetTypeRef.ERROR_TYPE;
			}

			DotNetTypeRef typeRef = type.toTypeRef();
			if(typeRef == DotNetTypeRef.AUTO_TYPE && resolveFromInitializer)
			{
				return CSharpResolveUtil.resolveIterableType((CSharpForeachStatementImpl) parent);
			}
			else
			{
				return typeRef;
			}
		}
		else
		{
			return super.toTypeRefImpl(resolveFromInitializer);
		}
	}

	@RequiredReadAction
	@Override
	public boolean hasModifier(@NotNull DotNetModifier modifier)
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
	@Override
	public int getTextOffset()
	{
		PsiElement nameIdentifier = getNameIdentifier();
		return nameIdentifier != null ? nameIdentifier.getTextOffset() : super.getTextOffset();
	}

	@RequiredReadAction
	@Override
	public String getName()
	{
		return CSharpPsiUtilImpl.getNameWithoutAt(this);
	}

	@Nullable
	@Override
	public PsiElement getNameIdentifier()
	{
		return findChildByClass(CSharpIdentifier.class);
	}

	@Override
	public PsiElement setName(@NonNls @NotNull String s) throws IncorrectOperationException
	{
		CSharpRefactoringUtil.replaceNameIdentifier(this, s);
		return this;
	}

	@RequiredReadAction
	@Override
	public boolean isConstant()
	{
		return getConstantKeywordElement() != null;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public PsiElement getConstantKeywordElement()
	{
		return findChildByType(CSharpTokens.CONST_KEYWORD);
	}

	@NotNull
	@Override
	public SearchScope getUseScope()
	{
		PsiElement parent = getParent();
		if(parent instanceof CSharpLocalVariableDeclarationStatement)
		{
			return new LocalSearchScope(parent.getParent());
		}
		else if(parent instanceof CSharpForeachStatementImpl ||
				parent instanceof CSharpUsingStatementImpl ||
				parent instanceof CSharpFixedStatementImpl ||
				parent instanceof CSharpForStatementImpl ||
				parent instanceof CSharpCatchStatementImpl)
		{
			return new LocalSearchScope(parent);
		}
		LOGGER.error("Global usage scope for local variable, parent: " + parent.getClass().getName());
		return super.getUseScope();
	}

	@Override
	public void delete() throws IncorrectOperationException
	{
		PsiElement parent = getParent();
		if(parent instanceof CSharpLocalVariableDeclarationStatement)
		{
			List<PsiElement> collectForDelete = new SmartList<PsiElement>();

			CSharpLocalVariable[] variables = ((CSharpLocalVariableDeclarationStatement) parent).getVariables();
			if(variables.length == 1)
			{
				collectForDelete.add(parent);
			}
			/*else
			{
				// first variable cant remove type
				if(variables[0] == this)
				{
					DotNetExpression initializer = getInitializer();
					if(initializer != null)
					{
						removeWithPrevSibling(initializer, CSharpTokens.EQ, collectForDelete);
						removeWithNextSibling(initializer, CSharpTokens.COMMA, collectForDelete);
						collectForDelete.add(initializer);
					}

					PsiElement nameIdentifier = getNameIdentifier();
					if(nameIdentifier != null)
					{
						collectForDelete.add(nameIdentifier);
						removeWithNextSibling(nameIdentifier, CSharpTokens.COMMA, collectForDelete);
					}
				}
				else
				{
					removeWithPrevSibling(this, CSharpTokens.COMMA, collectForDelete);
					collectForDelete.add(this);
				}
			}  */

			for(PsiElement element : collectForDelete)
			{
				element.delete();
			}
		}
		else if(parent instanceof CSharpCatchStatementImpl)
		{
			((CSharpCatchStatementImpl) parent).deleteVariable();
		}
		else
		{
			deleteInternal();
		}
	}

	public void deleteInternal()
	{
		super.delete();
	}

	private static void removeWithNextSibling(PsiElement element, IElementType token, List<PsiElement> collectForDelete)
	{
		PsiElement nextSibling = element.getNextSibling();
		if(nextSibling instanceof PsiWhiteSpace)
		{
			collectForDelete.add(nextSibling);
			nextSibling = nextSibling.getNextSibling();
		}

		if(nextSibling != null && nextSibling.getNode().getElementType() == token)
		{
			collectForDelete.add(nextSibling);
			nextSibling = nextSibling.getNextSibling();
		}

		if(nextSibling instanceof PsiWhiteSpace)
		{
			collectForDelete.add(nextSibling);
		}
	}

	private static void removeWithPrevSibling(PsiElement element, IElementType token, List<PsiElement> collectForDelete)
	{
		PsiElement prevSibling = element.getPrevSibling();
		if(prevSibling instanceof PsiWhiteSpace)
		{
			collectForDelete.add(prevSibling);
			prevSibling = prevSibling.getPrevSibling();
		}

		if(prevSibling != null && prevSibling.getNode().getElementType() == token)
		{
			collectForDelete.add(prevSibling);
		}
	}

	@Nullable
	@Override
	public DotNetType getSelfType()
	{
		return findChildByClass(DotNetType.class);
	}
}
