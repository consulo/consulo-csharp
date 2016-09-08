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

package org.mustbe.consulo.csharp.ide.navigation;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.CSharpTransform;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.search.searches.TypeInheritorsSearch;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.searches.DefinitionsScopedSearch;
import com.intellij.util.Processor;
import com.intellij.util.QueryExecutor;

/**
 * @author VISTALL
 * @since 17-May-16
 */
public class CSharpTypeImplementationSearcher implements QueryExecutor<PsiElement, DefinitionsScopedSearch.SearchParameters>
{
	@Override
	public boolean execute(@NotNull DefinitionsScopedSearch.SearchParameters queryParameters, @NotNull final Processor<PsiElement> consumer)
	{
		final PsiElement element = queryParameters.getElement();
		if(element instanceof DotNetTypeDeclaration)
		{
			return TypeInheritorsSearch.search((DotNetTypeDeclaration) element, queryParameters.getScope(), queryParameters.isCheckDeep(), true,
					CSharpTransform.INSTANCE).forEach(new Processor<DotNetTypeDeclaration>()

			{
				@Override
				public boolean process(DotNetTypeDeclaration typeDeclaration)
				{
					return consumer.process(typeDeclaration);
				}
			});
		}
		return true;
	}
}
