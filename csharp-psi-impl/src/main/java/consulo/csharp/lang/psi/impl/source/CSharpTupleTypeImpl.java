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

import javax.annotation.Nonnull;

import com.intellij.lang.ASTNode;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpTupleType;
import consulo.csharp.lang.psi.CSharpTupleVariable;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTupleTypeRef;
import consulo.dotnet.resolve.DotNetTypeRef;

/**
 * @author VISTALL
 * @since 26-Nov-16.
 */
public class CSharpTupleTypeImpl extends CSharpTypeElementImpl implements CSharpTupleType
{
	public CSharpTupleTypeImpl(@Nonnull ASTNode node)
	{
		super(node);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	protected DotNetTypeRef toTypeRefImpl()
	{
		CSharpTupleVariable[] variables = getVariables();
		DotNetTypeRef[] typeRefs = new DotNetTypeRef[variables.length];
		for(int i = 0; i < variables.length; i++)
		{
			CSharpTupleVariable variable = variables[i];
			typeRefs[i] = variable.toTypeRef(true);
		}
		return new CSharpTupleTypeRef(this, typeRefs, variables);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitTupleType(this);
	}

	@Nonnull
	@Override
	public CSharpTupleVariable[] getVariables()
	{
		return findChildrenByClass(CSharpTupleVariable.class);
	}
}
