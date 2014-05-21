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

import java.io.IOException;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpNamespaceHelper;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpUsingListImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpUsingListStub;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.index.CSharpIndexKeys;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;

/**
 * @author VISTALL
 * @since 15.01.14
 */
public class CSharpUsingListStubElementType extends CSharpAbstractStubElementType<CSharpUsingListStub, CSharpUsingListImpl>
{
	public CSharpUsingListStubElementType()
	{
		super("USING_LIST");
	}

	@Override
	public CSharpUsingListImpl createPsi(@NotNull ASTNode astNode)
	{
		return new CSharpUsingListImpl(astNode);
	}

	@Override
	public CSharpUsingListImpl createPsi(@NotNull CSharpUsingListStub cSharpUsingListStub)
	{
		return new CSharpUsingListImpl(cSharpUsingListStub);
	}

	@Override
	public CSharpUsingListStub createStub(@NotNull CSharpUsingListImpl cSharpUsingNamespaceList, StubElement stubElement)
	{
		String parentQName = null;
		PsiElement parent = cSharpUsingNamespaceList.getParent();
		if(parent instanceof PsiFile)
		{
			parentQName = CSharpNamespaceHelper.ROOT;
		}
		else if(parent instanceof DotNetQualifiedElement)
		{
			parentQName = ((DotNetQualifiedElement) parent).getPresentableQName();
		}

		if(parentQName == null)
		{
			parentQName = "<error>";
		}
		return new CSharpUsingListStub(stubElement, this, parentQName);
	}

	@Override
	public void serialize(@NotNull CSharpUsingListStub cSharpUsingListStub, @NotNull StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeName(cSharpUsingListStub.getParentQName());
	}

	@NotNull
	@Override
	public CSharpUsingListStub deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException
	{
		StringRef ref = stubInputStream.readName();
		return new CSharpUsingListStub(stubElement, this, ref);
	}

	@Override
	public void indexStub(@NotNull CSharpUsingListStub cSharpUsingListStub, @NotNull IndexSink indexSink)
	{
		indexSink.occurrence(CSharpIndexKeys.USING_LIST_INDEX, cSharpUsingListStub.getParentQName());
	}
}
