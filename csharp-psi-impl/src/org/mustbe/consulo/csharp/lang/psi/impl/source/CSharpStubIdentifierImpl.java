/*
 * Copyright 2013-2015 must-be.org
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

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpIdentifier;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpIdentifierStub;
import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.IStubElementType;

/**
 * @author VISTALL
 * @since 26.07.2015
 */
public class CSharpStubIdentifierImpl extends CSharpStubElementImpl<CSharpIdentifierStub> implements CSharpIdentifier
{
	public CSharpStubIdentifierImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	public CSharpStubIdentifierImpl(@NotNull CSharpIdentifierStub stub, @NotNull IStubElementType<? extends CSharpIdentifierStub, ?> nodeType)
	{
		super(stub, nodeType);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitIdentifier(this);
	}

	@RequiredReadAction
	@NotNull
	@Override
	public String getValue()
	{
		CSharpIdentifierStub stub = getStub();
		if(stub != null)
		{
			return stub.getValue();
		}
		return getText();
	}
}
