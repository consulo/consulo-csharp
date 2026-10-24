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
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpCollectionExpressionTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.IElementType;

/**
 * {@code [1, 2, ..rest]} (C# 12) - no natural type, it converts to the array or the collection type it is given to.
 *
 * @author VISTALL
 */
public class CSharpCollectionExpressionImpl extends CSharpExpressionImpl
{
	public CSharpCollectionExpressionImpl(IElementType elementType)
	{
		super(elementType);
	}

	@RequiredReadAction
	@Override
	public DotNetTypeRef toTypeRefImpl(boolean resolveFromParent)
	{
		return new CSharpCollectionExpressionTypeRef(this);
	}

	@Override
	public void accept(CSharpElementVisitor visitor)
	{
		visitor.visitElement(this);
	}
}
