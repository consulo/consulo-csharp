/*
 * Copyright 2013-2026 consulo.io
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
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpTypeRefByQName;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElement;
import org.jspecify.annotations.Nullable;

/**
 * {@code start..end} (C# 8) - a System.Range, each end optional; the ends convert to System.Index
 *
 * @author VISTALL
 */
public class CSharpRangeExpressionImpl extends CSharpExpressionImpl
{
	public static final String RANGE = "System.Range";
	public static final String INDEX = "System.Index";

	public CSharpRangeExpressionImpl(IElementType elementType)
	{
		super(elementType);
	}

	@Nullable
	public DotNetExpression getStartExpression()
	{
		PsiElement first = getFirstChild();
		return first instanceof DotNetExpression expression ? expression : null;
	}

	@Nullable
	public DotNetExpression getEndExpression()
	{
		PsiElement operator = findPsiChildByType(CSharpTokens.DOTDOT);
		for(PsiElement element = operator == null ? null : operator.getNextSibling(); element != null; element = element.getNextSibling())
		{
			if(element instanceof DotNetExpression expression)
			{
				return expression;
			}
		}
		return null;
	}

	@RequiredReadAction
	@Override
	public DotNetTypeRef toTypeRefImpl(boolean resolveFromParent)
	{
		return new CSharpTypeRefByQName(getProject(), getResolveScope(), RANGE);
	}

	@Override
	public void accept(CSharpElementVisitor visitor)
	{
		visitor.visitElement(this);
	}
}
