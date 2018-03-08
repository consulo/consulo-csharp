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
import java.util.List;

import javax.annotation.Nonnull;

import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.ContainerUtil;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import consulo.csharp.lang.util.ContainerUtil2;
import consulo.dotnet.lang.psi.impl.BaseDotNetNamespaceAsElement;
import consulo.dotnet.psi.DotNetInheritUtil;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.resolve.DotNetNamespaceAsElement;

/**
 * @author VISTALL
 * @since 07.10.14
 */
public class AttributeByNameSelector implements CSharpResolveSelector
{
	public static final String AttributeSuffix = "Attribute";

	private String myNameWithAt;

	public AttributeByNameSelector(String nameWithAt)
	{
		myNameWithAt = nameWithAt;
	}

	@Nonnull
	@Override
	@RequiredReadAction
	public Collection<PsiElement> doSelectElement(@Nonnull CSharpResolveContext context, boolean deep)
	{
		if(myNameWithAt.isEmpty())
		{
			return Collections.emptyList();
		}

		UserDataHolderBase options = new UserDataHolderBase();
		options.putUserData(BaseDotNetNamespaceAsElement.FILTER, DotNetNamespaceAsElement.ChildrenFilter.ONLY_ELEMENTS);

		if(myNameWithAt.charAt(0) == '@')
		{
			return CSharpResolveUtil.mergeGroupsToIterable(context.findByName(myNameWithAt.substring(1, myNameWithAt.length()), deep, options));
		}
		else
		{
			Collection<PsiElement> array = ContainerUtil2.concat(context.findByName(myNameWithAt, deep, options), context.findByName(myNameWithAt + AttributeSuffix, deep, options));

			List<PsiElement> collection = CSharpResolveUtil.mergeGroupsToIterable(array);

			return ContainerUtil.findAll(collection, element -> element instanceof CSharpTypeDeclaration && DotNetInheritUtil.isAttribute((DotNetTypeDeclaration) element));
		}
	}
}
