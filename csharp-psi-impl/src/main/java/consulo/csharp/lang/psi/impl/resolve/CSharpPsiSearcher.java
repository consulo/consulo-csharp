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

package consulo.csharp.lang.psi.impl.resolve;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndexKey;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.psi.impl.CSharpNamespaceAsElementImpl;
import consulo.csharp.lang.psi.impl.partial.CSharpPartialElementManager;
import consulo.csharp.lang.psi.impl.stub.index.CSharpIndexKeys;
import consulo.csharp.lang.psi.impl.stub.index.TypeByVmQNameIndex;
import consulo.dotnet.psi.DotNetQualifiedElement;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.resolve.DotNetNamespaceAsElement;
import consulo.dotnet.resolve.impl.IndexBasedDotNetPsiSearcher;

import javax.annotation.Nonnull;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author VISTALL
 * @since 13.07.14
 */
@Singleton
public class CSharpPsiSearcher extends IndexBasedDotNetPsiSearcher
{
	@Nonnull
	public static CSharpPsiSearcher getInstance(@Nonnull Project project)
	{
		return ServiceManager.getService(project, CSharpPsiSearcher.class);
	}

	@Inject
	public CSharpPsiSearcher(Project project)
	{
		super(project);
	}

	@Nonnull
	@Override
	protected DotNetNamespaceAsElement createNamespace(@Nonnull String indexKey, @Nonnull String qName)
	{
		return new CSharpNamespaceAsElementImpl(myProject, indexKey, qName, this);
	}

	@Nonnull
	@Override
	public StubIndexKey<String, DotNetQualifiedElement> getElementByQNameIndexKey()
	{
		return CSharpIndexKeys.MEMBER_BY_NAMESPACE_QNAME_INDEX;
	}

	@Nonnull
	@Override
	public StubIndexKey<String, DotNetQualifiedElement> getNamespaceIndexKey()
	{
		return CSharpIndexKeys.MEMBER_BY_ALL_NAMESPACE_QNAME_INDEX;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public Collection<? extends DotNetTypeDeclaration> findTypesImpl(@Nonnull String vmQName, @Nonnull GlobalSearchScope scope)
	{
		if(DumbService.isDumb(myProject))
		{
			return Collections.emptyList();
		}

		Collection<DotNetTypeDeclaration> declarations = TypeByVmQNameIndex.getInstance().get(vmQName.hashCode(), myProject, scope);

		List<CSharpTypeDeclaration> partials = Collections.emptyList();
		for(DotNetTypeDeclaration element : declarations)
		{
			if(!(element instanceof CSharpTypeDeclaration))
			{
				continue;
			}

			if(element.hasModifier(CSharpModifier.PARTIAL))
			{
				if(partials.isEmpty())
				{
					partials = new ArrayList<>(2);
				}

				partials.add((CSharpTypeDeclaration) element);
			}
		}

		if(partials.isEmpty())
		{
			return declarations;
		}

		if(partials.size() == 1)
		{
			return declarations;
		}
		else
		{
			CSharpTypeDeclaration typeDeclaration = CSharpPartialElementManager.getInstance(myProject).getOrCreateCompositeType(scope, vmQName, partials);

			List<DotNetTypeDeclaration> anotherList = new ArrayList<>(declarations.size() - partials.size() + 1);
			anotherList.add(typeDeclaration);

			declarations.removeAll(partials);
			anotherList.addAll(declarations);

			return anotherList;
		}
	}
}
