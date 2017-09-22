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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpRefTypeRef;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;

/**
 * @author VISTALL
 * @since 11.02.14
 */
public class CSharpOutRefWrapExpressionImpl extends CSharpExpressionImpl implements DotNetExpression
{
	private static final TokenSet ourStartTypes = TokenSet.create(CSharpTokens.OUT_KEYWORD, CSharpTokens.REF_KEYWORD);

	public CSharpOutRefWrapExpressionImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitOurRefWrapExpression(this);
	}

	@RequiredReadAction
	@NotNull
	@Override
	public DotNetTypeRef toTypeRefImpl(boolean resolveFromParent)
	{
		DotNetExpression innerExpression = getInnerExpression();
		if(innerExpression == null)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}

		DotNetTypeRef typeRef = innerExpression.toTypeRef(resolveFromParent);
		PsiElement startElement = getStartElement();
		CSharpRefTypeRef.Type type = CSharpRefTypeRef.Type.ref;
		if(startElement.getNode().getElementType() == CSharpTokens.OUT_KEYWORD)
		{
			type = CSharpRefTypeRef.Type.out;
		}
		return new CSharpRefTypeRef(getProject(), type, typeRef);
	}

	@NotNull
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
