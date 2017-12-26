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
import com.intellij.lang.ASTNode;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTupleTypeRef;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.resolve.DotNetTypeRef;

/**
 * @author VISTALL
 * @since 26-Nov-16.
 */
public class CSharpTupleExpressionImpl extends CSharpExpressionImpl
{
	public CSharpTupleExpressionImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@NotNull
	@RequiredReadAction
	public CSharpTupleElementImpl[] getElements()
	{
		return findChildrenByClass(CSharpTupleElementImpl.class);
	}

	@RequiredReadAction
	@NotNull
	@Override
	public DotNetTypeRef toTypeRefImpl(boolean resolveFromParent)
	{
		CSharpTupleElementImpl[] elements = getElements();
		DotNetTypeRef[] typeRefs = new DotNetTypeRef[elements.length];
		for(int i = 0; i < elements.length; i++)
		{
			CSharpTupleElementImpl element = elements[i];
			DotNetExpression expression = element.getExpression();
			typeRefs[i] = expression == null ? DotNetTypeRef.ERROR_TYPE : expression.toTypeRef(resolveFromParent);
		}
		return new CSharpTupleTypeRef(this, typeRefs, elements);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitTupleExpression(this);
	}
}
