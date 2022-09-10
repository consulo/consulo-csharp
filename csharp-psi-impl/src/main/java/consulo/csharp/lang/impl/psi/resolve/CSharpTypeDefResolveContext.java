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

package consulo.csharp.lang.impl.psi.resolve;

import consulo.application.util.function.Processor;
import consulo.language.psi.PsiElement;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpTypeDefStatement;
import consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import consulo.util.dataholder.UserDataHolder;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;

/**
 * @author VISTALL
 * @since 05.03.2016
 */
public class CSharpTypeDefResolveContext implements CSharpResolveContext
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
	public boolean processElements(@Nonnull Processor<PsiElement> processor, boolean deep)
	{
		return processor.process(myStatement);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public Collection<PsiElement> findByName(@Nonnull String name, boolean deep, @Nonnull UserDataHolder holder)
	{
		return name.equals(myName) ? Collections.singletonList(myStatement) : Collections.emptyList();
	}

	@Nonnull
	@Override
	public PsiElement getElement()
	{
		return myStatement;
	}
}
