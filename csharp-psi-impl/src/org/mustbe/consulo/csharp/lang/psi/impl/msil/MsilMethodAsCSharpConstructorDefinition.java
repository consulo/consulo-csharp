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

package org.mustbe.consulo.csharp.lang.psi.impl.msil;

import org.mustbe.consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import org.mustbe.consulo.msil.lang.psi.MsilMethodEntry;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 23.05.14
 */
public class MsilMethodAsCSharpConstructorDefinition extends MsilMethodAsCSharpLikeMethodDefinition implements CSharpConstructorDeclaration
{
	private final MsilClassAsCSharpTypeDefinition myTypeDefinition;

	public MsilMethodAsCSharpConstructorDefinition(MsilClassAsCSharpTypeDefinition typeDefinition, MsilMethodEntry methodEntry)
	{
		super(methodEntry);
		myTypeDefinition = typeDefinition;
	}

	@Override
	public PsiElement getParent()
	{
		return myTypeDefinition;
	}

	@Override
	public String getName()
	{
		return myTypeDefinition.getName();
	}

	@Override
	public boolean isDeConstructor()
	{
		return false;
	}
}
