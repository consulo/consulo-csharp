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

package org.mustbe.consulo.csharp.lang.psi.impl.resolve;

import org.jetbrains.annotations.NotNull;
import consulo.annotations.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDefStatement;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveContextAdapter;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.psi.PsiElement;
import com.intellij.util.Processor;

/**
 * @author VISTALL
 * @since 05.03.2016
 */
public class CSharpTypeDefResolveContext extends CSharpResolveContextAdapter
{
	private CSharpTypeDefStatement myStatement;
	private String myName;

	public CSharpTypeDefResolveContext(CSharpTypeDefStatement statement)
	{
		myStatement = statement;
		myName = myStatement.getName();
	}

	@RequiredReadAction
	@Override
	public boolean processElements(@NotNull Processor<PsiElement> processor, boolean deep)
	{
		return processor.process(myStatement);
	}

	@RequiredReadAction
	@NotNull
	@Override
	public PsiElement[] findByName(@NotNull String name, boolean deep, @NotNull UserDataHolder holder)
	{
		return name.equals(myName) ? new PsiElement[] {myStatement} : PsiElement.EMPTY_ARRAY;
	}

	@NotNull
	@Override
	public PsiElement getElement()
	{
		return myStatement;
	}
}
