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

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpConversionMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpStubElements;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpNativeTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpMethodStub;
import com.intellij.lang.ASTNode;

/**
 * @author VISTALL
 * @since 09.01.14
 */
public class CSharpConversionMethodDeclarationImpl extends CSharpLikeMethodDeclarationImpl implements CSharpConversionMethodDeclaration
{
	public CSharpConversionMethodDeclarationImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	public CSharpConversionMethodDeclarationImpl(@NotNull CSharpMethodStub stub)
	{
		super(stub, CSharpStubElements.CONVERSION_METHOD_DECLARATION);
	}

	@Override
	public String getName()
	{
		return isImplicit() ? "<implicit>" : "<explicit>";
	}

	@Override
	public boolean isImplicit()
	{
		return getReturnTypeRef() == CSharpNativeTypeRef.IMPLICIT;
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitConversionMethodDeclaration(this);
	}
}
