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

package org.mustbe.consulo.csharp.lang.psi.impl.resolve.baseResolveContext;

import gnu.trove.THashMap;

import java.util.Collection;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.impl.resolve.CSharpAdditionalMemberProvider;
import org.mustbe.consulo.csharp.lang.psi.impl.resolve.CSharpBaseResolveContext;
import org.mustbe.consulo.csharp.lang.psi.impl.resolve.CSharpElementGroupImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.wrapper.GenericUnwrapTool;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.AtomicNullableLazyValue;
import com.intellij.psi.PsiElement;
import com.intellij.util.Consumer;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;

/**
 * @author VISTALL
 * @since 07.03.2016
 */
public abstract class MapElementGroupCollector<K, E extends PsiElement> extends ElementGroupCollector<E>
{
	private AtomicNullableLazyValue<Map<K, CSharpElementGroup<E>>> myMapValue = new AtomicNullableLazyValue<Map<K, CSharpElementGroup<E>>>()
	{
		@Nullable
		@Override
		@RequiredReadAction
		protected Map<K, CSharpElementGroup<E>> compute()
		{
			MultiMap<K, E> elements = calcElements();
			final Map<K, CSharpElementGroup<E>> map;
			if(elements.isEmpty())
			{
				map = null;
			}
			else
			{
				map = new THashMap<K, CSharpElementGroup<E>>();
				final DotNetGenericExtractor extractor = getExtractor();
				final DotNetModifierListOwner parent = myResolveContext.getElement();
				final Project project = getProject();

				for(Map.Entry<K, Collection<E>> entry : elements.entrySet())
				{
					K key = entry.getKey();
					Collection<E> value = entry.getValue();

					if(extractor != DotNetGenericExtractor.EMPTY)
					{
						value = ContainerUtil.map(value, new Function<E, E>()
						{
							@Override
							@SuppressWarnings("unchecked")
							public E fun(final E element)
							{
								return element instanceof DotNetNamedElement ? (E) GenericUnwrapTool.extract((DotNetNamedElement) element, extractor, parent) : element;
							}
						});

					}

					CSharpElementGroup<E> group = new CSharpElementGroupImpl<E>(project, key, value);
					map.put(key, group);
				}
			}

			return map;
		}
	};

	public MapElementGroupCollector(@NotNull CSharpAdditionalMemberProvider.Target target, @NotNull CSharpBaseResolveContext<?> context)
	{
		super(target, context);
	}

	@NotNull
	@RequiredReadAction
	@SuppressWarnings("unchecked")
	private MultiMap<K, E> calcElements()
	{
		final MultiMap<K, E> multiMap = new MultiMap<K, E>();
		Consumer consumer = new Consumer<E>()
		{
			@Override
			@RequiredReadAction
			public void consume(PsiElement e)
			{
				K keyForElement = getKeyForElement((E) e);
				if(keyForElement == null)
				{
					return;
				}

				multiMap.getModifiable(keyForElement).add((E) e);
			}
		};

		CSharpElementVisitor visitor = createVisitor(consumer);

		myResolveContext.acceptChildren(visitor);

		for(CSharpAdditionalMemberProvider memberProvider : ourAdditionalMemberProviders)
		{
			if(memberProvider.getTarget() == myTarget)
			{
				memberProvider.processAdditionalMembers(myResolveContext.getElement(), getExtractor(), consumer);
			}
		}

		if(multiMap.isEmpty())
		{
			return MultiMap.empty();
		}
		return multiMap;
	}

	@Nullable
	@RequiredReadAction
	protected abstract K getKeyForElement(E element);

	@Nullable
	@RequiredReadAction
	public Map<K, CSharpElementGroup<E>> toMap()
	{
		return myMapValue.getValue();
	}
}
