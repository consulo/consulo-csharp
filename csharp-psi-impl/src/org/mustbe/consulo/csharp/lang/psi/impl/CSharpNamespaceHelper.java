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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.index.CSharpIndexKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.util.QualifiedName;
import com.intellij.util.CommonProcessors;
import com.intellij.util.indexing.IdFilter;
import lombok.val;

/**
 * @author VISTALL
 * @since 28.12.13.
 */
public class CSharpNamespaceHelper
{
	public static final String ROOT = "<root>";
	public static final String NAMESPACE_SEPARATOR = ".";

	@Nullable
	public static CSharpNamespaceAsElement getNamespaceElementIfFind(@NotNull Project project, @NotNull final String qName,
			@NotNull GlobalSearchScope globalSearchScope)
	{
		assert !qName.isEmpty() : "Dont use empty namespace name. Use 'ROOT' field";

		val findFirstProcessor = new CommonProcessors.FindFirstProcessor<PsiElement>();

		StubIndex.getInstance().processElements(CSharpIndexKeys.NAMESPACE_BY_QNAME_INDEX, qName, project, globalSearchScope,
				PsiElement.class, findFirstProcessor);

		if(findFirstProcessor.getFoundValue() != null)
		{
			return new CSharpNamespaceAsElement(project, qName, globalSearchScope);
		}

		// for example u decl 'namespace NUnit.Test', but dont decl 'namespace NUnit'
		// and that 'using NUnit' ill be error
		val findFirstProcessor2 = new CommonProcessors.FindFirstProcessor<String>()
		{
			@Override
			protected boolean accept(String qName2)
			{
				return qName2.startsWith(qName);
			}
		};
		StubIndex.getInstance().processAllKeys(CSharpIndexKeys.NAMESPACE_BY_QNAME_INDEX, findFirstProcessor2, globalSearchScope,
				IdFilter.getProjectIdFilter(project, false));

		if(findFirstProcessor2.getFoundValue() != null)
		{
			return new CSharpNamespaceAsElement(project, qName, globalSearchScope);
		}
		return null;
	}

	@NotNull
	public static String toString(@NotNull QualifiedName namespace)
	{
		return namespace == QualifiedName.ROOT ? ROOT : namespace.toString();
	}

	@NotNull
	public static String getNamespaceForIndexing(@Nullable String namespace)
	{
		if(StringUtil.isEmpty(namespace))
		{
			return ROOT;
		}
		return namespace;
	}

	@NotNull
	public static String getNameWithNamespaceForIndexing(@Nullable String namespace, @NotNull String name)
	{
		if(StringUtil.isEmpty(namespace))
		{
			return name;
		}
		return namespace + "." + name;
	}
}
