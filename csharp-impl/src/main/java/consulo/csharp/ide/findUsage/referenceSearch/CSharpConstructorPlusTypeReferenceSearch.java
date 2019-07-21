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

import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Processor;
import consulo.annotations.RequiredWriteAction;
import consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.psi.impl.light.builder.CSharpLightConstructorDeclarationBuilder;
import consulo.csharp.lang.psi.impl.resolve.additionalMembersImpl.StructOrGenericParameterConstructorProvider;
import consulo.dotnet.psi.DotNetNamedElement;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 16.04.2015
 */
public class CSharpConstructorPlusTypeReferenceSearch extends QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>
{
	@Override
	@RequiredWriteAction
	public void processQuery(@Nonnull ReferencesSearch.SearchParameters queryParameters, @Nonnull Processor<PsiReference> consumer)
	{
		PsiElement elementToSearch = queryParameters.getElementToSearch();

		if(elementToSearch instanceof CSharpTypeDeclaration)
		{
			String name = ((CSharpTypeDeclaration) elementToSearch).getName();
			if(name == null)
			{
				return;
			}

			for(DotNetNamedElement member : ((CSharpTypeDeclaration) elementToSearch).getMembers())
			{
				if(member instanceof CSharpConstructorDeclaration)
				{
					queryParameters.getOptimizer().searchWord(name, queryParameters.getEffectiveSearchScope(), true, member);
				}
			}

			CSharpLightConstructorDeclarationBuilder constructor = StructOrGenericParameterConstructorProvider.buildDefaultConstructor((DotNetNamedElement) elementToSearch, name);

			queryParameters.getOptimizer().searchWord(name, queryParameters.getEffectiveSearchScope(), true, constructor);
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
