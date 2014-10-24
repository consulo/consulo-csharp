/*
 * Copyright 2013-2014 must-be.org
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

package org.mustbe.consulo.csharp.lang.psi.impl.stub.elementTypes;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpElements;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpParameterListImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpEmptyStub;
import org.mustbe.consulo.dotnet.psi.DotNetParameterList;
import com.intellij.lang.ASTNode;

/**
 * @author VISTALL
 * @since 15.01.14
 */
public class CSharpParameterListStubElementType extends CSharpEmptyStubElementType<DotNetParameterList>
{
	public CSharpParameterListStubElementType()
	{
		super("PARAMETER_LIST");
	}

	@Override
	public boolean shouldCreateStub(ASTNode node)
	{
		ASTNode treeParent = node.getTreeParent();
		return !(treeParent != null && treeParent.getElementType() == CSharpElements.ANONYM_METHOD_EXPRESSION) && super.shouldCreateStub(node);
	}

	@NotNull
	@Override
	public DotNetParameterList createElement(@NotNull ASTNode astNode)
	{
		return new CSharpParameterListImpl(astNode);
	}

	@Override
	public DotNetParameterList createPsi(@NotNull CSharpEmptyStub<DotNetParameterList> cSharpParameterListStub)
	{
		return new CSharpParameterListImpl(cSharpParameterListStub);
	}
}
