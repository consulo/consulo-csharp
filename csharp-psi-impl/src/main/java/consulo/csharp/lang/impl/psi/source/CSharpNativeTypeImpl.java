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

import consulo.language.ast.IElementType;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpNativeType;
import consulo.csharp.lang.impl.psi.CSharpTokenSets;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.PsiElement;


/**
 * @author VISTALL
 * @since 05.12.14
 */
public class CSharpNativeTypeImpl extends CSharpTypeElementImpl implements CSharpNativeType
{
	public CSharpNativeTypeImpl(IElementType elementType)
	{
		super(elementType);
	}

	@Override
	public void accept(CSharpElementVisitor visitor)
	{
		visitor.visitNativeType(this);
	}

	@RequiredReadAction
	@Override
	public IElementType getTypeElementType()
	{
		return getTypeElement().getNode().getElementType();
	}

	@Override
	@RequiredReadAction
	public DotNetTypeRef toTypeRefImpl()
	{
		return CSharpNativeTypeImplUtil.toTypeRef(this);
	}

	@RequiredReadAction
	@Override
	public PsiElement getTypeElement()
	{
		return findNotNullChildByFilter(CSharpTokenSets.NATIVE_TYPES);
	}
}
