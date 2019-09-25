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

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import consulo.csharp.lang.psi.CSharpCallArgument;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.dotnet.psi.DotNetExpression;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 15.09.14
 */
public class CSharpCallArgumentImpl extends CSharpElementImpl implements CSharpCallArgument
{
	public CSharpCallArgumentImpl(@Nonnull ASTNode node)
	{
		super(node);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitCallArgument(this);
	}

	@Nullable
	@Override
	public DotNetExpression getArgumentExpression()
	{
		return findChildByClass(DotNetExpression.class);
	}

	@Override
	public boolean processDeclarations(@Nonnull PsiScopeProcessor processor, @Nonnull ResolveState state, PsiElement lastParent, @Nonnull PsiElement place)
	{
		DotNetExpression expression = getArgumentExpression();
		return expression != null && expression.processDeclarations(processor, state, lastParent, place);
	}
}
