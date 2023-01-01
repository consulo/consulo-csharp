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

package consulo.csharp.lang.psi.resolve;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElement;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;

/**
 * @author VISTALL
 * @since 07.10.14
 */
public class OperatorByTokenSelector implements CSharpResolveSelector
{
	private final IElementType myToken;

	public OperatorByTokenSelector(IElementType token)
	{
		myToken = token;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public Collection<PsiElement> doSelectElement(@Nonnull CSharpResolveContext context, boolean deep)
	{
		CSharpElementGroup<CSharpMethodDeclaration> groupByTokenType = context.findOperatorGroupByTokenType(myToken, deep);
		if(groupByTokenType == null)
		{
			return Collections.emptyList();
		}
		return Collections.singletonList(groupByTokenType);
	}
}
