/*
 * Copyright 2013-2019 consulo.io
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

import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.tree.IElementType;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.dotnet.psi.DotNetStatement;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2019-10-01
 */
public class CSharpLocalMethodDeclarationStatementImpl extends CSharpElementImpl implements DotNetStatement
{
	public CSharpLocalMethodDeclarationStatementImpl(@Nonnull IElementType elementType)
	{
		super(elementType);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitLocalMethodDeclarationStatement(this);
	}

	@Nonnull
	public CSharpMethodDeclaration getMethod()
	{
		return findNotNullChildByClass(CSharpMethodDeclaration.class);
	}

	@Override
	public boolean processDeclarations(@Nonnull PsiScopeProcessor processor, @Nonnull ResolveState state, PsiElement lastParent, @Nonnull PsiElement place)
	{
		CSharpMethodDeclaration method = getMethod();
		if(!processor.execute(method, state))
		{
			return false;
		}
		return super.processDeclarations(processor, state, lastParent, place);
	}
}
