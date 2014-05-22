/*
 * Copyright 2013-2014 must-be.org
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

package org.mustbe.consulo.csharp.lang.psi.impl;

import java.util.Collection;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.MsilWrapperProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpUsingListImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.index.CSharpIndexKeys;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.index.NamespaceByQNameIndex;
import org.mustbe.consulo.dotnet.psi.DotNetNamespaceDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetNamespaceAsElement;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.ResolveState;
import com.intellij.psi.impl.light.LightElement;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.util.CommonProcessors;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.indexing.IdFilter;
import lombok.val;

/**
 * @author VISTALL
 * @since 29.12.13.
 */
public class CSharpNamespaceAsElement extends LightElement implements DotNetNamespaceAsElement
{
	@NotNull
	private final String myQName;
	@NotNull
	private final GlobalSearchScope myScope;

	public CSharpNamespaceAsElement(@NotNull Project project, @NotNull String qName, @NotNull GlobalSearchScope scope)
	{
		super(PsiManager.getInstance(project), CSharpLanguage.INSTANCE);
		myQName = qName;
		myScope = scope;
	}

	@NotNull
	@Override
	public PsiElement getNavigationElement()
	{
		PsiElement firstNamespaceEntry = findFirstNamespaceEntry();
		return firstNamespaceEntry == null ? this : firstNamespaceEntry;
	}

	@Override
	public void navigate(boolean requestFocus)
	{
		PsiElement navigationElement = findFirstNamespaceEntry();
		if(navigationElement instanceof Navigatable)
		{
			((Navigatable) navigationElement).navigate(requestFocus);
		}
	}

	@Override
	public boolean isValid()
	{
		return findFirstNamespaceEntry() != null;
	}

	@Nullable
	public PsiElement findFirstNamespaceEntry()
	{
		val findFirstProcessor = new CommonProcessors.FindFirstProcessor<PsiElement>();
		StubIndex.getInstance().processElements(CSharpIndexKeys.NAMESPACE_BY_QNAME_INDEX, myQName, getProject(), myScope,
				PsiElement.class, findFirstProcessor);
		if(findFirstProcessor.getFoundValue() != null)
		{
			return findFirstProcessor.getFoundValue();
		}

		val findFirstProcessor2 = new CommonProcessors.FindFirstProcessor<String>()
		{
			@Override
			protected boolean accept(String qName2)
			{
				return qName2.startsWith(myQName);
			}
		};

		StubIndex.getInstance().processAllKeys(CSharpIndexKeys.NAMESPACE_BY_QNAME_INDEX, findFirstProcessor2, myScope,
				IdFilter.getProjectIdFilter(getProject(), false));

		if(findFirstProcessor2.getFoundValue() != null)
		{
			Collection<PsiElement> dotNetNamespaceDeclarations = NamespaceByQNameIndex.getInstance().get(findFirstProcessor2
					.getFoundValue(), getProject(), myScope);

			return ContainerUtil.getFirstItem(dotNetNamespaceDeclarations);
		}

		return null;
	}

	@Override
	public boolean equals(Object o)
	{
		if(this == o)
		{
			return true;
		}
		if(o == null || getClass() != o.getClass())
		{
			return false;
		}

		CSharpNamespaceAsElement that = (CSharpNamespaceAsElement) o;

		if(!getPresentableQName().equals(that.getPresentableQName()))
		{
			return false;
		}

		return true;
	}

	@Override
	public int hashCode()
	{
		int result = getPresentableQName().hashCode();
		result = 31 * result + myScope.hashCode();
		return result;
	}

	@Override
	public boolean processDeclarations(
			@NotNull final PsiScopeProcessor processor,
			@NotNull final ResolveState state,
			final PsiElement lastParent,
			@NotNull final PsiElement place)
	{
		boolean process = StubIndex.getInstance().processElements(CSharpIndexKeys.USING_LIST_INDEX, getPresentableQName(), getProject(), myScope,
				CSharpUsingListImpl.class, new Processor<CSharpUsingListImpl>()
		{
			@Override
			public boolean process(CSharpUsingListImpl usingList)
			{
				return usingList.processDeclarations(processor, state, lastParent, place);
			}
		});

		if(!process)
		{
			return false;
		}
		process = StubIndex.getInstance().processElements(CSharpIndexKeys.MEMBER_BY_NAMESPACE_QNAME_INDEX, getPresentableQName(),
				getProject(), myScope, PsiElement.class, new MsilWrapperProcessor<PsiElement>()
		{
			@Override
			public boolean processImpl(PsiElement namedElement)
			{
				if(namedElement instanceof DotNetNamespaceDeclaration)
				{
					return true;
				}

				if(!CSharpResolveUtil.checkConditionKey(processor, namedElement))
				{
					return true;
				}

				return processor.execute(namedElement, state);
			}
		});
		if(!process)
		{
			return false;
		}
		return StubIndex.getInstance().processElements(CSharpIndexKeys.MEMBER_BY_NAMESPACE_QNAME_INDEX, getPresentableQName(),
				getProject(), myScope, PsiElement.class, new MsilWrapperProcessor<PsiElement>()
		{
			@Override
			public boolean processImpl(PsiElement namedElement)
			{
				if(namedElement instanceof DotNetNamespaceDeclaration)
				{
					val e = new CSharpNamespaceAsElement(getProject(), ((DotNetNamespaceDeclaration) namedElement).getPresentableQName(), myScope);
					if(!CSharpResolveUtil.checkConditionKey(processor, namedElement))
					{
						return true;
					}
					return processor.execute(e, state);
				}
				return true;
			}
		});
	}

	@Override
	public String toString()
	{
		return "CSharpNamespaceAsElement: " + getPresentableQName();
	}

	@Override
	public String getName()
	{
		return StringUtil.getShortName(getPresentableQName());
	}

	@Override
	public PsiElement setName(@NonNls @NotNull String s) throws IncorrectOperationException
	{
		return null;
	}

	@Nullable
	@Override
	public String getPresentableParentQName()
	{
		return StringUtil.getPackageName(myQName);
	}

	@NotNull
	@Override
	public String getPresentableQName()
	{
		return myQName;
	}

	public GlobalSearchScope getScope()
	{
		return myScope;
	}
}
