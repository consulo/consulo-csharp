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
import consulo.language.ast.TokenSet;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpRefTypeRef;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.resolve.DotNetTypeRef;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 11.02.14
 */
public class CSharpOutRefWrapExpressionImpl extends CSharpExpressionImpl implements CSharpOutRefExpression
{
	public static final TokenSet ourStartTypes = TokenSet.create(CSharpTokens.OUT_KEYWORD, CSharpTokens.REF_KEYWORD);

	private final ThreadLocal<Boolean> myTypeRefProcessing = ThreadLocal.withInitial(() -> Boolean.FALSE);

	public CSharpOutRefWrapExpressionImpl(@Nonnull IElementType elementType)
	{
		super(elementType);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitOurRefWrapExpression(this);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public CSharpRefTypeRef.Type getExpressionType()
	{
		PsiElement startElement = getStartElement();
		CSharpRefTypeRef.Type type = CSharpRefTypeRef.Type.ref;
		if(startElement.getNode().getElementType() == CSharpTokens.OUT_KEYWORD)
		{
			type = CSharpRefTypeRef.Type.out;
		}
		return type;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetTypeRef toTypeRefImpl(boolean resolveFromParent)
	{
		DotNetExpression innerExpression = getInnerExpression();
		if(innerExpression == null)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}

		CSharpRefTypeRef.Type type = getExpressionType();

		DotNetTypeRef typeRef = innerExpression.toTypeRef(resolveFromParent);
		if(typeRef == DotNetTypeRef.ERROR_TYPE)
		{
			boolean isPlaceholder = innerExpression instanceof CSharpReferenceExpression && ((CSharpReferenceExpression) innerExpression).isPlaceholderReference();
			if(isPlaceholder)
			{
				if(resolveFromParent)
				{
					if(myTypeRefProcessing.get())
					{
						return new CSharpOutRefAutoTypeRef(type);
					}

					try
					{
						myTypeRefProcessing.set(Boolean.TRUE);

						DotNetTypeRef innerType = CSharpOutRefVariableImpl.searchTypeRefFromCall(this);

						return new CSharpRefTypeRef(getProject(), getResolveScope(), type, innerType);
					}
					finally
					{
						myTypeRefProcessing.set(Boolean.FALSE);
					}
				}
				else
				{
					return new CSharpOutRefAutoTypeRef(type);
				}
			}

			return typeRef;
		}


		return new CSharpRefTypeRef(getProject(), getResolveScope(), type, typeRef);
	}

	@Nonnull
	public PsiElement getStartElement()
	{
		return findNotNullChildByFilter(ourStartTypes);
	}

	@Nullable
	public DotNetExpression getInnerExpression()
	{
		return findChildByClass(DotNetExpression.class);
	}
}
