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

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.reflactoring.CSharpRefactoringUtil;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpLocalVariable;
import org.mustbe.consulo.csharp.lang.psi.CSharpLocalVariableDeclarationStatement;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierList;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
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

	@NotNull
	@Override
	public DotNetTypeRef toTypeRef(boolean resolveFromInitializer)
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
			return super.toTypeRef(resolveFromInitializer);
		}
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

	@Override
	public int getTextOffset()
	{
		PsiElement nameIdentifier = getNameIdentifier();
		return nameIdentifier != null ? nameIdentifier.getTextOffset() : super.getTextOffset();
	}

	@Override
	public String getName()
	{
		return CSharpPsiUtilImpl.getNameWithoutAt(this);
	}

	@Nullable
	@Override
	public PsiElement getNameIdentifier()
	{
		return findChildByType(CSharpTokens.IDENTIFIER);
	}

	@Override
	public PsiElement setName(@NonNls @NotNull String s) throws IncorrectOperationException
	{
		CSharpRefactoringUtil.replaceNameIdentifier(this, s);
		return this;
	}

	@Override
	public boolean isConstant()
	{
		return findChildByType(CSharpTokens.CONST_KEYWORD) != null;
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
		return super.getUseScope();
	}

	@Override
	public void delete() throws IncorrectOperationException
	{
		List<PsiElement> collectForDelete = new SmartList<PsiElement>();
		PsiElement parent = getParent();
		if(parent instanceof CSharpLocalVariableDeclarationStatement)
		{
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
		}
		else
		{
			collectForDelete.add(this);
		}

		for(PsiElement element : collectForDelete)
		{
			element.delete();
		}
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
}
