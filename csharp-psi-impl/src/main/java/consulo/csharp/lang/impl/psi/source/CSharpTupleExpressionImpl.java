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
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpTupleTypeRef;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.IElementType;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 26-Nov-16.
 */
public class CSharpTupleExpressionImpl extends CSharpExpressionImpl
{
	public CSharpTupleExpressionImpl(@Nonnull IElementType elementType)
	{
		super(elementType);
	}

	@Nonnull
	@RequiredReadAction
	public CSharpTupleElementImpl[] getElements()
	{
		return findChildrenByClass(CSharpTupleElementImpl.class);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetTypeRef toTypeRefImpl(boolean resolveFromParent)
	{
		CSharpTupleElementImpl[] elements = getElements();
		DotNetTypeRef[] typeRefs = new DotNetTypeRef[elements.length];
		for(int i = 0; i < elements.length; i++)
		{
			CSharpTupleElementImpl element = elements[i];
			DotNetExpression expression = element.getExpression();
			typeRefs[i] = expression == null ? DotNetTypeRef.ERROR_TYPE : expression.toTypeRef(resolveFromParent);
		}
		return new CSharpTupleTypeRef(getProject(), getResolveScope(), typeRefs, elements);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitTupleExpression(this);
	}
}
