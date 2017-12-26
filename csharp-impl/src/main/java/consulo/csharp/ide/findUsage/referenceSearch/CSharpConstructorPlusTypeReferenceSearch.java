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

package consulo.csharp.ide.findUsage.referenceSearch;

import org.jetbrains.annotations.NotNull;
import consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.psi.DotNetNamedElement;
import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Processor;

/**
 * @author VISTALL
 * @since 16.04.2015
 */
public class CSharpConstructorPlusTypeReferenceSearch extends QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>
{
	@Override
	public void processQuery(@NotNull ReferencesSearch.SearchParameters queryParameters, @NotNull Processor<PsiReference> consumer)
	{
		PsiElement elementToSearch = queryParameters.getElementToSearch();

		if(elementToSearch instanceof CSharpTypeDeclaration)
		{
			for(DotNetNamedElement member : ((CSharpTypeDeclaration) elementToSearch).getMembers())
			{
				if(member instanceof CSharpConstructorDeclaration)
				{
					ReferencesSearch.search(member, queryParameters.getEffectiveSearchScope(), queryParameters.isIgnoreAccessScope()).forEach
							(consumer);
				}
			}
		}   /*
		else if(elementToSearch instanceof CSharpConstructorDeclaration)
		{
			PsiElement parent = elementToSearch.getParent();
			if(parent instanceof CSharpTypeDeclaration)
			{
				ReferencesSearch.search(parent, queryParameters.getEffectiveSearchScope(), queryParameters.isIgnoreAccessScope()).forEach
						(consumer);
			}
		} */

	}

}
