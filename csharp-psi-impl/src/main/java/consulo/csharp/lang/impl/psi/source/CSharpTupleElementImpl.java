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

import consulo.language.psi.PsiElement;
import consulo.language.ast.IElementType;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.csharp.lang.impl.ide.refactoring.CSharpRefactoringUtil;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpIdentifier;
import consulo.csharp.lang.impl.psi.source.resolve.type.wrapper.CSharpTupleTypeDeclaration;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.language.psi.PsiNameIdentifierOwner;
import consulo.util.collection.ArrayUtil;
import consulo.language.util.IncorrectOperationException;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 26-Nov-16.
 */
public class CSharpTupleElementImpl extends CSharpElementImpl implements PsiNameIdentifierOwner, DotNetNamedElement
{
	public CSharpTupleElementImpl(@Nonnull IElementType elementType)
	{
		super(elementType);
	}

	@RequiredReadAction
	public int getPosition()
	{
		PsiElement parent = getParent();
		if(parent instanceof CSharpTupleExpressionImpl tupleExpression)
		{
			return ArrayUtil.indexOf(tupleExpression.getElements(), this);
		}
		return -1;
	}

	@RequiredReadAction
	@Override
	public String getName()
	{
		CSharpIdentifier element = findChildByClass(CSharpIdentifier.class);
		return element == null ? null : CSharpPsiUtilImpl.getNameWithoutAt(element.getValue());
	}

	@Nullable
	@RequiredReadAction
	public DotNetExpression getExpression()
	{
		return findChildByClass(DotNetExpression.class);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitTupleElement(this);
	}

	@RequiredReadAction
	@Override
	public int getTextOffset()
	{
		PsiElement nameIdentifier = getNameIdentifier();
		return nameIdentifier == null ? super.getTextOffset() : nameIdentifier.getTextOffset();
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
	public PsiElement setName(@Nonnull String s) throws IncorrectOperationException
	{
		CSharpRefactoringUtil.replaceNameIdentifier(this, s);
		return this;
	}

	@Override
	public boolean isEquivalentTo(PsiElement another)
	{
		PsiElement tupleElement = another.getOriginalElement().getUserData(CSharpTupleTypeDeclaration.TUPLE_ELEMENT);
		if(tupleElement != null && super.isEquivalentTo(tupleElement))
		{
			return true;
		}
		return super.isEquivalentTo(another);
	}
}
