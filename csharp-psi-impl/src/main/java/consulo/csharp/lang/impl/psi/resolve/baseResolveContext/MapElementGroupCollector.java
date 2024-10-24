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

package consulo.csharp.lang.impl.psi.resolve.baseResolveContext;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.util.AtomicNullableLazyValue;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.impl.psi.resolve.CSharpAdditionalMemberProvider;
import consulo.csharp.lang.impl.psi.resolve.CSharpBaseResolveContext;
import consulo.csharp.lang.impl.psi.resolve.CSharpElementGroupImpl;
import consulo.csharp.lang.impl.psi.source.resolve.type.wrapper.GenericUnwrapTool;
import consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.language.psi.PsiElement;
import consulo.project.Project;
import consulo.util.collection.ContainerUtil;
import consulo.util.collection.MultiMap;
import jakarta.annotation.Nonnull;

import jakarta.annotation.Nullable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

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
				map = new LinkedHashMap<>();
				final DotNetGenericExtractor extractor = getExtractor();
				final DotNetModifierListOwner parent = myResolveContext.getElement();
				final Project project = getProject();

				for(Map.Entry<K, Collection<E>> entry : elements.entrySet())
				{
					K key = entry.getKey();
					Collection<E> value = entry.getValue();

					if(extractor != DotNetGenericExtractor.EMPTY)
					{
						value = ContainerUtil.map(value, element -> element instanceof DotNetNamedElement ? (E) GenericUnwrapTool.extract((DotNetNamedElement) element, extractor, parent) : element);
					}

					CSharpElementGroup<E> group = new CSharpElementGroupImpl<>(project, key, value);
					map.put(key, group);
				}
			}

			return map;
		}
	};

	public MapElementGroupCollector(@Nonnull CSharpAdditionalMemberProvider.Target target, @Nonnull CSharpBaseResolveContext<?> context)
	{
		super(target, context);
	}

	@Nonnull
	@RequiredReadAction
	@SuppressWarnings("unchecked")
	private MultiMap<K, E> calcElements()
	{
		final MultiMap<K, E> multiMap = MultiMap.createLinked();
		Consumer consumer = e ->
		{
			K keyForElement = getKeyForElement((E) e);
			if(keyForElement == null)
			{
				return;
			}

			multiMap.getModifiable(keyForElement).add((E) e);
		};

		CSharpElementVisitor visitor = createVisitor(consumer);

		myResolveContext.acceptChildren(visitor);

		for(CSharpAdditionalMemberProvider memberProvider : CSharpAdditionalMemberProvider.EP_NAME.getExtensionList())
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
