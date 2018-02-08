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

import java.util.Collection;
import java.util.Collections;

import org.jetbrains.annotations.NotNull;
import com.intellij.psi.PsiElement;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;

/**
 * @author VISTALL
 * @since 07.10.14
 */
public class ExtensionMethodByNameSelector implements CSharpResolveSelector
{
	private final String myName;

	public ExtensionMethodByNameSelector(String name)
	{
		myName = name;
	}

	@RequiredReadAction
	@NotNull
	@Override
	public Collection<PsiElement> doSelectElement(@NotNull CSharpResolveContext context, boolean deep)
	{
		CSharpElementGroup<CSharpMethodDeclaration> groupByName = context.findExtensionMethodGroupByName(myName);
		if(groupByName == null)
		{
			return Collections.emptyList();
		}
		return Collections.singletonList(groupByName);
	}
}