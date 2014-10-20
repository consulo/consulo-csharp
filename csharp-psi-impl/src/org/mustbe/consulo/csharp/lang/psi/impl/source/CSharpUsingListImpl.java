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

package org.mustbe.consulo.csharp.lang.psi.impl.source;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpFile;
import org.mustbe.consulo.csharp.lang.psi.CSharpFileFactory;
import org.mustbe.consulo.csharp.lang.psi.CSharpStubElements;
import org.mustbe.consulo.csharp.lang.psi.CSharpUsingList;
import org.mustbe.consulo.csharp.lang.psi.CSharpUsingListChild;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpEmptyStub;
import org.mustbe.consulo.dotnet.resolve.DotNetNamespaceAsElement;
import org.mustbe.consulo.dotnet.resolve.DotNetPsiSearcher;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.TokenType;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 28.11.13.
 */
public class CSharpUsingListImpl extends CSharpStubElementImpl<CSharpEmptyStub<CSharpUsingList>> implements CSharpUsingList
{
	public CSharpUsingListImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	public CSharpUsingListImpl(@NotNull CSharpEmptyStub<CSharpUsingList> stub)
	{
		super(stub, CSharpStubElements.USING_LIST);
	}

	@Override
	@NotNull
	public CSharpTypeDefStatementImpl[] getTypeDefs()
	{
		return getStubOrPsiChildren(CSharpStubElements.TYPE_DEF_STATEMENT, CSharpTypeDefStatementImpl.ARRAY_FACTORY);
	}

	@Override
	@NotNull
	public CSharpUsingNamespaceStatementImpl[] getUsingDirectives()
	{
		return getStubOrPsiChildren(CSharpStubElements.USING_NAMESPACE_STATEMENT, CSharpUsingNamespaceStatementImpl.ARRAY_FACTORY);
	}

	@NotNull
	@Override
	public DotNetNamespaceAsElement[] getUsingNamespaces()
	{
		CSharpUsingNamespaceStatementImpl[] usingDirectives = getUsingDirectives();
		List<DotNetNamespaceAsElement> namespaceAsElements = new ArrayList<DotNetNamespaceAsElement>(usingDirectives.length + 1);
		for(CSharpUsingNamespaceStatementImpl usingDirective : usingDirectives)
		{
			DotNetNamespaceAsElement resolve = usingDirective.resolve();
			if(resolve != null)
			{
				namespaceAsElements.add(resolve);
			}
		}

		PsiElement parent = getParent();
		if(parent instanceof CSharpFile)
		{
			namespaceAsElements.add(DotNetPsiSearcher.getInstance(getProject()).findNamespace("", getResolveScope()));
		}
		return ContainerUtil.toArray(namespaceAsElements, DotNetNamespaceAsElement.ARRAY_FACTORY);
	}

	@Override
	@NotNull
	public CSharpUsingListChild[] getStatements()
	{
		return getStubOrPsiChildren(CSharpStubElements.USING_CHILDREN, CSharpUsingListChild.ARRAY_FACTORY);
	}

	@Override
	public void addUsing(@NotNull String qName)
	{
		CSharpUsingNamespaceStatementImpl newStatement = CSharpFileFactory.createUsingStatement(getProject(), qName);

		CSharpUsingListChild[] statements = getStatements();

		//	CSharpUsingNamespaceStatementImpl last = statements[statements.length - 1];

		LeafPsiElement leafPsiElement = new LeafPsiElement(TokenType.WHITE_SPACE, "\n");

		getNode().addChild(leafPsiElement);

		addAfter(newStatement, leafPsiElement);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitUsingNamespaceList(this);
	}
}
