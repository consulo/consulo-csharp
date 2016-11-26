/*
 * Copyright 2013-2016 must-be.org
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
import consulo.csharp.lang.psi.CSharpTupleType;
import consulo.csharp.lang.psi.CSharpTupleVariable;
import consulo.dotnet.resolve.DotNetTypeRef;

/**
 * @author VISTALL
 * @since 26-Nov-16.
 */
public class CSharpTupleTypeImpl extends CSharpTypeElementImpl implements CSharpTupleType
{
	public CSharpTupleTypeImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@RequiredReadAction
	@NotNull
	@Override
	protected DotNetTypeRef toTypeRefImpl()
	{
		return DotNetTypeRef.ERROR_TYPE;
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitTupleType(this);
	}

	@NotNull
	@Override
	public CSharpTupleVariable[] getVariables()
	{
		return findChildrenByClass(CSharpTupleVariable.class);
	}
}
