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

package org.mustbe.consulo.csharp.lang.psi.impl.resolve;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.wrapper.GenericUnwrapTool;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 07.03.2016
 */
public class MutableElementGroupWithCache<T extends PsiElement>
{
	private final Project myProject;
	private Object myKey;
	private Collection<T> myElements;
	private DotNetGenericExtractor myExtractor;

	private CSharpElementGroup<T> myGroup;

	public MutableElementGroupWithCache(Project project, Object key, Collection<T> elements, DotNetGenericExtractor extractor)
	{
		myProject = project;
		myKey = key;
		myElements = elements;
		myExtractor = extractor;
	}

	@NotNull
	public CSharpElementGroup<T> asGroup()
	{
		CSharpElementGroup<T> group = myGroup;
		if(group != null)
		{
			return group;
		}
		else
		{
			Collection<T> elements = myElements;
			if(myExtractor != DotNetGenericExtractor.EMPTY)
			{
				elements = ContainerUtil.map(elements, new Function<T, T>()
				{
					@Override
					@SuppressWarnings("unchecked")
					public T fun(final T element)
					{
						return  element instanceof DotNetNamedElement ? (T)GenericUnwrapTool.extract((DotNetNamedElement) element, myExtractor) : element;
					}
				});
			}
			group = new CSharpElementGroupImpl<T>(myProject, myKey, elements);

			myElements = null;
			myGroup = group;
			return group;
		}
	}
}
