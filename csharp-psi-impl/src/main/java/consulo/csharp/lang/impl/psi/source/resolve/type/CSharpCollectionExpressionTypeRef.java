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

package consulo.csharp.lang.impl.psi.source.resolve.type;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.source.CSharpCollectionExpressionImpl;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetInheritUtil;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetArrayTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeRefWithCachedResult;
import consulo.dotnet.psi.resolve.DotNetTypeResolveResult;
import consulo.language.psi.PsiElement;

import java.util.Set;

/**
 * The type of a collection expression - it has none of its own, and converts to an array, a span, or a type implementing
 * IEnumerable but string (C# spec 12.8.22, collection expression conversion).
 *
 * @author VISTALL
 */
public class CSharpCollectionExpressionTypeRef extends DotNetTypeRefWithCachedResult
{
	private static final Set<String> ourSpans = Set.of("System.Span`1", "System.ReadOnlySpan`1");

	private final CSharpCollectionExpressionImpl myExpression;

	@RequiredReadAction
	public CSharpCollectionExpressionTypeRef(CSharpCollectionExpressionImpl expression)
	{
		super(expression.getProject(), expression.getResolveScope());
		myExpression = expression;
	}

	@RequiredReadAction
	@Override
	protected DotNetTypeResolveResult resolveResult()
	{
		return DotNetTypeResolveResult.EMPTY;
	}

	@RequiredReadAction
	@Override
	public String getVmQName()
	{
		return "[]";
	}

	/**
	 * @return true if a collection expression converts to the type
	 */
	@RequiredReadAction
	public static boolean isCollectionType(DotNetTypeRef typeRef)
	{
		if(typeRef instanceof DotNetArrayTypeRef)
		{
			return true;
		}
		PsiElement element = typeRef.resolve().getElement();
		if(!(element instanceof DotNetTypeDeclaration type) || DotNetTypes.System.String.equals(type.getVmQName()))
		{
			return false;
		}
		return ourSpans.contains(type.getVmQName()) || DotNetInheritUtil.isParentOrSelf(DotNetTypes.System.Collections.IEnumerable, type, true);
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof CSharpCollectionExpressionTypeRef other && other.myExpression == myExpression;
	}

	@Override
	public int hashCode()
	{
		return myExpression.hashCode();
	}
}
