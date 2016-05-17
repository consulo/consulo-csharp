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

package org.mustbe.consulo.csharp.ide.findUsage.referenceSearch;

import java.util.Collection;
import java.util.Collections;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.overrideSystem.OverrideUtil;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetVirtualImplementOwner;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Processor;

/**
 * @author VISTALL
 * @since 17-May-16
 */
public class CSharpImplementedReferenceSearch extends QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>
{
	@Override
	public void processQuery(@NotNull ReferencesSearch.SearchParameters queryParameters, @NotNull Processor<PsiReference> consumer)
	{
		final PsiElement elementToSearch = queryParameters.getElementToSearch();
		if(elementToSearch instanceof CSharpMethodDeclaration)
		{
			Collection<DotNetVirtualImplementOwner> targets = ApplicationManager.getApplication().runReadAction(new Computable<Collection<DotNetVirtualImplementOwner>>()
			{
				@Override
				public Collection<DotNetVirtualImplementOwner> compute()
				{
					if(((CSharpMethodDeclaration) elementToSearch).hasModifier(DotNetModifier.ABSTRACT))
					{
						return OverrideUtil.collectOverridenMembers((DotNetVirtualImplementOwner) elementToSearch);
					}
					return Collections.emptyList();
				}
			});

			for(DotNetVirtualImplementOwner target : targets)
			{
				if(!ReferencesSearch.search(target, queryParameters.getEffectiveSearchScope()).forEach(consumer))
				{
					return;
				}
			}
		}
	}
}
