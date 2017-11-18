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
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpFieldOrPropertySetBlock;
import consulo.csharp.lang.psi.CSharpNamedFieldOrPropertySet;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.dotnet.resolve.DotNetTypeRef;

/**
 * @author VISTALL
 * @since 18-Nov-17
 */
public class CSharpShortObjectInitializerExpressionImpl extends CSharpExpressionImpl
{
	private final ThreadLocal<Boolean> myTypeRefProcessing = ThreadLocal.withInitial(() -> Boolean.FALSE);

	public CSharpShortObjectInitializerExpressionImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@Nullable
	public CSharpFieldOrPropertySetBlock getFieldOrPropertySetBlock()
	{
		return findChildByClass(CSharpFieldOrPropertySetBlock.class);
	}

	@RequiredReadAction
	@NotNull
	@Override
	public DotNetTypeRef toTypeRefImpl(boolean resolveFromParent)
	{
		if(resolveFromParent)
		{
			if(myTypeRefProcessing.get())
			{
				return DotNetTypeRef.ERROR_TYPE;
			}

			myTypeRefProcessing.set(true);
			try
			{
				PsiElement parent = getParent();
				if(parent instanceof CSharpNamedFieldOrPropertySet)
				{
					CSharpReferenceExpression nameElement = ((CSharpNamedFieldOrPropertySet) parent).getNameElement();

					return nameElement.toTypeRef(true);
				}
			}
			finally
			{
				myTypeRefProcessing.set(false);
			}
		}
		return DotNetTypeRef.AUTO_TYPE;
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitShortObjectInitializerExpression(this);
	}
}
