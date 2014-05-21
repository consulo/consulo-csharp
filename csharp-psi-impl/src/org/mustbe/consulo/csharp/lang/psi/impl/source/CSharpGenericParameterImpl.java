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
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpStubElements;
import org.mustbe.consulo.csharp.lang.psi.impl.light.builder.CSharpLightConstructorDeclarationBuilder;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpGenericParameterStub;
import org.mustbe.consulo.dotnet.psi.DotNetConstructorDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import com.intellij.lang.ASTNode;
import com.intellij.util.Processor;

/**
 * @author VISTALL
 * @since 30.11.13.
 */
public class CSharpGenericParameterImpl extends CSharpStubMemberImpl<CSharpGenericParameterStub> implements DotNetGenericParameter
{
	public CSharpGenericParameterImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	public CSharpGenericParameterImpl(@NotNull CSharpGenericParameterStub stub)
	{
		super(stub, CSharpStubElements.GENERIC_PARAMETER);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitGenericParameter(this);
	}

	@Override
	public void processConstructors(@NotNull Processor<DotNetConstructorDeclaration> processor)
	{
		processor.process(getDefaultConstructor());
	}

	@NotNull
	public CSharpLightConstructorDeclarationBuilder getDefaultConstructor()
	{
		CSharpLightConstructorDeclarationBuilder builder = new CSharpLightConstructorDeclarationBuilder(getProject());
		builder.addModifier(CSharpModifier.PUBLIC);
		builder.setNavigationElement(this);
		builder.withParent(this);
		return builder;
	}
}
