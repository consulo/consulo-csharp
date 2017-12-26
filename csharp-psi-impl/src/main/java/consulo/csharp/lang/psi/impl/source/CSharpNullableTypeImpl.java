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
import consulo.csharp.lang.psi.CSharpNullableType;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.psi.impl.CSharpNullableTypeUtil;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 05.12.14
 */
public class CSharpNullableTypeImpl extends CSharpTypeElementImpl implements CSharpNullableType
{
	public CSharpNullableTypeImpl(@NotNull ASTNode node)
	{
		super(node);
	}
	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitNullableType(this);
	}

	@RequiredReadAction
	@Override
	@NotNull
	public DotNetTypeRef toTypeRefImpl()
	{
		DotNetType innerType = getInnerType();
		if(innerType == null)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}
		return CSharpNullableTypeUtil.box(this, innerType.toTypeRef());
	}

	@RequiredReadAction
	@Override
	@Nullable
	public DotNetType getInnerType()
	{
		return findChildByClass(DotNetType.class);
	}

	@RequiredReadAction
	@Override
	@NotNull
	public PsiElement getQuestElement()
	{
		return findNotNullChildByType(CSharpTokens.QUEST);
	}
}
