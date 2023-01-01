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
import consulo.annotation.access.RequiredWriteAction;
import consulo.content.scope.SearchScope;
import consulo.csharp.lang.impl.ide.refactoring.CSharpRefactoringUtil;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpConstantTypeRef;
import consulo.csharp.lang.impl.psi.source.resolve.util.CSharpResolveUtil;
import consulo.csharp.lang.psi.CSharpIdentifier;
import consulo.csharp.lang.psi.CSharpLocalVariable;
import consulo.csharp.lang.psi.CSharpLocalVariableDeclarationStatement;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiWhiteSpace;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.resolve.ResolveState;
import consulo.language.psi.scope.LocalSearchScope;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.IncorrectOperationException;
import consulo.logging.Logger;
import consulo.util.collection.SmartList;
import org.jetbrains.annotations.NonNls;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @author VISTALL
 * @since 16.12.13.
 */
public class CSharpLocalVariableImpl extends CSharpVariableImpl implements CSharpLocalVariable
{
	private static final Logger LOG = Logger.getInstance(CSharpLocalVariableImpl.class);

	public CSharpLocalVariableImpl(@Nonnull IElementType elementType)
	{
		super(elementType);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
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
	@Nonnull
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
			DotNetTypeRef defaultTypeRef = super.toTypeRefImpl(resolveFromInitializer);
			if(isConstant())
			{
				DotNetExpression initializer = getInitializer();
				if(initializer instanceof CSharpConstantExpressionImpl)
				{
					return new CSharpConstantTypeRef((CSharpConstantExpressionImpl) initializer, defaultTypeRef);
				}
			}
			return defaultTypeRef;
		}
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

	@RequiredReadAction
	@Nullable
	@Override
	public String getNameWithAt()
	{
		return CSharpPsiUtilImpl.getNameWithAt(this);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public PsiElement getNameIdentifier()
	{
		return findChildByClass(CSharpIdentifier.class);
	}

	@RequiredWriteAction
	@Override
	public PsiElement setName(@NonNls @Nonnull String s) throws IncorrectOperationException
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
		return findPsiChildByType(CSharpTokens.CONST_KEYWORD);
	}

	@Nonnull
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
		LOG.error("Global usage scope for local variable, parent: " + parent.getClass().getName());
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

	@Override
	@RequiredReadAction
	public boolean processDeclarations(@Nonnull PsiScopeProcessor processor, @Nonnull ResolveState state, PsiElement lastParent, @Nonnull PsiElement place)
	{
		if(!processor.execute(this, state))
		{
			return false;
		}

		DotNetExpression initializer = getInitializer();
		if(initializer != null && !initializer.processDeclarations(processor, state, lastParent, place))
		{
			return false;
		}
		return super.processDeclarations(processor, state, lastParent, place);
	}
}
