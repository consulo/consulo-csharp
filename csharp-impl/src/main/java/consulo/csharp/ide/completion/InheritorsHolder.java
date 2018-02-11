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
package consulo.csharp.ide.completion;

import gnu.trove.THashSet;

import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import consulo.csharp.lang.psi.impl.source.CSharpPsiUtilImpl;
import consulo.dotnet.psi.DotNetMethodDeclaration;
import consulo.dotnet.psi.DotNetQualifiedElement;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.AutoCompletionPolicy;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.psi.PsiElement;
import com.intellij.util.Consumer;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author peter
 */
class InheritorsHolder implements Consumer<LookupElement>
{
	private final Set<String> myAddedTypeLikes = new THashSet<>();
	private final CompletionResultSet myResult;

	public InheritorsHolder(CompletionResultSet result)
	{
		myResult = result;
	}

	@Override
	public void consume(LookupElement lookupElement)
	{
		final Object object = lookupElement.getObject();
		if(object instanceof PsiElement && CSharpPsiUtilImpl.isTypeLikeElement((PsiElement) object))
		{
			registerTypeLike((DotNetQualifiedElement) object);
		}
		myResult.addElement(AutoCompletionPolicy.NEVER_AUTOCOMPLETE.applyPolicy(lookupElement));
	}

	public void registerTypeLike(DotNetQualifiedElement psiClass)
	{
		ContainerUtil.addIfNotNull(myAddedTypeLikes, getVmQName(psiClass));
	}

	@Nullable
	private static String getVmQName(DotNetQualifiedElement psiClass)
	{
		if(psiClass instanceof DotNetTypeDeclaration)
		{
			return ((DotNetTypeDeclaration) psiClass).getVmQName();
		}
		else if(psiClass instanceof DotNetMethodDeclaration)
		{
			return psiClass.getPresentableQName();
		}
		throw new UnsupportedOperationException(psiClass.getClass().toString());
	}

	public boolean alreadyProcessed(@Nonnull LookupElement element)
	{
		final Object object = element.getObject();
		return object instanceof DotNetQualifiedElement && alreadyProcessed((DotNetQualifiedElement) object);
	}

	public boolean alreadyProcessed(@Nonnull DotNetQualifiedElement object)
	{
		final String name = getVmQName(object);
		return name == null || myAddedTypeLikes.contains(name);
	}
}
