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

import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.Processor;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.CSharpCastType;
import consulo.csharp.lang.psi.*;
import consulo.csharp.lang.psi.impl.resolve.baseResolveContext.MapElementGroupCollectors;
import consulo.csharp.lang.psi.impl.resolve.baseResolveContext.SimpleElementGroupCollectors;
import consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import consulo.csharp.lang.util.ContainerUtil2;
import consulo.dotnet.psi.DotNetElement;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.resolve.DotNetTypeResolveResult;
import consulo.util.dataholder.UserDataHolder;
import gnu.trove.THashSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * @author VISTALL
 * @since 26.10.14
 */
public abstract class CSharpBaseResolveContext<T extends DotNetElement & DotNetModifierListOwner> implements CSharpResolveContext
{
	private final NotNullLazyValue<SimpleElementGroupCollectors.IndexMethod> myIndexMethodCollectorValue = new NotNullLazyValue<SimpleElementGroupCollectors.IndexMethod>()
	{
		@Nonnull
		@Override
		protected SimpleElementGroupCollectors.IndexMethod compute()
		{
			return new SimpleElementGroupCollectors.IndexMethod(CSharpBaseResolveContext.this);
		}
	};

	private final NotNullLazyValue<SimpleElementGroupCollectors.Constructor> myConstructorCollectorValue = new NotNullLazyValue<SimpleElementGroupCollectors.Constructor>()
	{
		@Nonnull
		@Override
		protected SimpleElementGroupCollectors.Constructor compute()
		{
			return new SimpleElementGroupCollectors.Constructor(CSharpBaseResolveContext.this);
		}
	};

	private final NotNullLazyValue<SimpleElementGroupCollectors.DeConstructor> myDeConstructorCollectorValue = new NotNullLazyValue<SimpleElementGroupCollectors.DeConstructor>()
	{
		@Nonnull
		@Override
		protected SimpleElementGroupCollectors.DeConstructor compute()
		{
			return new SimpleElementGroupCollectors.DeConstructor(CSharpBaseResolveContext.this);
		}
	};

	private final NotNullLazyValue<MapElementGroupCollectors.ConversionMethod> myConversionMethodCollectorValue = new NotNullLazyValue<MapElementGroupCollectors.ConversionMethod>()
	{
		@Nonnull
		@Override
		protected MapElementGroupCollectors.ConversionMethod compute()
		{
			return new MapElementGroupCollectors.ConversionMethod(CSharpBaseResolveContext.this);
		}
	};

	private final NotNullLazyValue<MapElementGroupCollectors.OperatorMethod> myOperatorMethodCollectorValue = new NotNullLazyValue<MapElementGroupCollectors.OperatorMethod>()
	{
		@Nonnull
		@Override
		protected MapElementGroupCollectors.OperatorMethod compute()
		{
			return new MapElementGroupCollectors.OperatorMethod(CSharpBaseResolveContext.this);
		}
	};

	private final NotNullLazyValue<MapElementGroupCollectors.Other> myOtherCollectorValue = new NotNullLazyValue<MapElementGroupCollectors.Other>()
	{
		@Nonnull
		@Override
		protected MapElementGroupCollectors.Other compute()
		{
			return new MapElementGroupCollectors.Other(CSharpBaseResolveContext.this);
		}
	};

	@Nonnull
	protected final T myElement;
	@Nonnull
	protected final DotNetGenericExtractor myExtractor;
	@Nullable
	private Set<PsiElement> myRecursiveGuardSet;

	@RequiredReadAction
	public CSharpBaseResolveContext(@Nonnull T element, @Nonnull DotNetGenericExtractor extractor, @Nullable Set<PsiElement> recursiveGuardSet)
	{
		myElement = element;
		myExtractor = extractor;
		myRecursiveGuardSet = recursiveGuardSet;
	}

	public abstract void acceptChildren(CSharpElementVisitor visitor);

	@Nonnull
	@RequiredReadAction
	protected abstract List<DotNetTypeRef> getExtendTypeRefs();

	@Nonnull
	@RequiredReadAction
	private CSharpResolveContext getSuperContext()
	{
		THashSet<PsiElement> alreadyProcessedItem = new THashSet<>();
		if(myRecursiveGuardSet != null)
		{
			alreadyProcessedItem.addAll(myRecursiveGuardSet);
		}
		return getSuperContextImpl(alreadyProcessedItem);
	}

	@Nonnull
	@RequiredReadAction
	private CSharpResolveContext getSuperContextImpl(Set<PsiElement> alreadyProcessedItem)
	{
		List<DotNetTypeRef> superTypes = getExtendTypeRefs();

		if(superTypes.isEmpty())
		{
			return EMPTY;
		}

		List<CSharpResolveContext> contexts = new ArrayList<>(superTypes.size());
		for(DotNetTypeRef dotNetTypeRef : superTypes)
		{
			DotNetTypeResolveResult typeResolveResult = dotNetTypeRef.resolve();
			PsiElement resolvedElement = typeResolveResult.getElement();

			if(resolvedElement != null && alreadyProcessedItem.add(resolvedElement))
			{
				DotNetGenericExtractor genericExtractor = typeResolveResult.getGenericExtractor();

				contexts.add(CSharpResolveContextUtil.createContext(genericExtractor, myElement.getResolveScope(), resolvedElement, alreadyProcessedItem));
			}
		}

		if(contexts.isEmpty())
		{
			return EMPTY;
		}
		return new CSharpCompositeResolveContext(myElement.getProject(), contexts.toArray(new CSharpResolveContext[contexts.size()]));
	}

	@RequiredReadAction
	@Nullable
	@Override
	public CSharpElementGroup<CSharpIndexMethodDeclaration> indexMethodGroup(boolean deep)
	{
		CSharpElementGroup<CSharpIndexMethodDeclaration> elementGroup = myIndexMethodCollectorValue.getValue().toGroup();

		if(elementGroup == null)
		{
			return deep ? getSuperContext().indexMethodGroup(true) : null;
		}
		else
		{
			CSharpElementGroup<CSharpIndexMethodDeclaration> deepGroup = deep ? getSuperContext().indexMethodGroup(true) : null;

			if(deepGroup == null)
			{
				return elementGroup;
			}
			return new CSharpCompositeElementGroupImpl<>(myElement.getProject(), Arrays.asList(elementGroup, deepGroup));
		}
	}

	@RequiredReadAction
	@Nullable
	@Override
	public CSharpElementGroup<CSharpConstructorDeclaration> constructorGroup()
	{
		return myConstructorCollectorValue.getValue().toGroup();
	}

	@RequiredReadAction
	@Nullable
	@Override
	public CSharpElementGroup<CSharpConstructorDeclaration> deConstructorGroup()
	{
		return myDeConstructorCollectorValue.getValue().toGroup();
	}

	@RequiredReadAction
	@Nullable
	@Override
	public CSharpElementGroup<CSharpMethodDeclaration> findOperatorGroupByTokenType(@Nonnull IElementType type, boolean deep)
	{
		Map<IElementType, CSharpElementGroup<CSharpMethodDeclaration>> map = myOperatorMethodCollectorValue.getValue().toMap();
		if(map == null)
		{
			return deep ? getSuperContext().findOperatorGroupByTokenType(type, true) : null;
		}
		else
		{
			CSharpElementGroup<CSharpMethodDeclaration> deepGroup = deep ? getSuperContext().findOperatorGroupByTokenType(type, true) : null;

			if(deepGroup == null)
			{
				return map.get(type);
			}

			CSharpElementGroup<CSharpMethodDeclaration> thisGroup = map.get(type);
			if(thisGroup == null)
			{
				return deepGroup;
			}
			return new CSharpCompositeElementGroupImpl<>(myElement.getProject(), Arrays.asList(thisGroup, deepGroup));
		}
	}

	@RequiredReadAction
	@Nullable
	@Override
	public CSharpElementGroup<CSharpConversionMethodDeclaration> findConversionMethodGroup(@Nonnull CSharpCastType castType, boolean deep)
	{
		Map<CSharpCastType, CSharpElementGroup<CSharpConversionMethodDeclaration>> map = myConversionMethodCollectorValue.getValue().toMap();
		if(map == null)
		{
			return deep ? getSuperContext().findConversionMethodGroup(castType, true) : null;
		}
		else
		{
			CSharpElementGroup<CSharpConversionMethodDeclaration> deepGroup = deep ? getSuperContext().findConversionMethodGroup(castType, true) : null;

			if(deepGroup == null)
			{
				return map.get(castType);
			}

			CSharpElementGroup<CSharpConversionMethodDeclaration> thisGroup = map.get(castType);
			if(thisGroup == null)
			{
				return deepGroup;
			}
			return new CSharpCompositeElementGroupImpl<>(myElement.getProject(), Arrays.asList(thisGroup, deepGroup));
		}
	}

	@RequiredReadAction
	@Nullable
	@Override
	public CSharpElementGroup<CSharpMethodDeclaration> findExtensionMethodGroupByName(@Nonnull String name)
	{
		Map<String, CSharpElementGroup<PsiElement>> map = myOtherCollectorValue.getValue().toMap();
		if(map == null)
		{
			return null;
		}
		CSharpElementGroup<PsiElement> elementGroup = map.get(name);
		if(elementGroup == null)
		{
			return null;
		}

		return filterElementGroupToExtensionGroup(elementGroup);
	}

	@Nullable
	private static CSharpElementGroup<CSharpMethodDeclaration> filterElementGroupToExtensionGroup(CSharpElementGroup<PsiElement> elementGroup)
	{
		final List<CSharpMethodDeclaration> extensions = new SmartList<>();
		elementGroup.process(element ->
		{
			if(element instanceof CSharpMethodDeclaration && ((CSharpMethodDeclaration) element).isExtension())
			{
				extensions.add((CSharpMethodDeclaration) element);
			}
			return true;
		});
		if(extensions.isEmpty())
		{
			return null;
		}
		return new CSharpElementGroupImpl<>(elementGroup.getProject(), elementGroup.getKey(), extensions);
	}

	@RequiredReadAction
	@Override
	public boolean processExtensionMethodGroups(@Nonnull Processor<CSharpElementGroup<CSharpMethodDeclaration>> processor)
	{
		Map<String, CSharpElementGroup<PsiElement>> map = myOtherCollectorValue.getValue().toMap();
		if(map == null)
		{
			return true;
		}

		for(CSharpElementGroup<PsiElement> elementGroup : map.values())
		{
			CSharpElementGroup<CSharpMethodDeclaration> group = filterElementGroupToExtensionGroup(elementGroup);
			if(group == null)
			{
				continue;
			}

			if(!processor.process(group))
			{
				return false;
			}
		}

		return true;
	}

	@RequiredReadAction
	@Override
	@Nonnull
	public Collection<PsiElement> findByName(@Nonnull String name, boolean deep, @Nonnull UserDataHolder holder)
	{
		Map<String, CSharpElementGroup<PsiElement>> map = myOtherCollectorValue.getValue().toMap();

		Collection<PsiElement> selectedElements;
		if(map == null)
		{
			selectedElements = Collections.emptyList();
		}
		else
		{
			CSharpElementGroup<PsiElement> group = map.get(name);
			if(group == null)
			{
				selectedElements = Collections.emptyList();
			}
			else
			{
				selectedElements = Collections.singletonList(group);
			}
		}

		if(deep)
		{
			selectedElements = ContainerUtil2.concat(selectedElements, getSuperContext().findByName(name, true, holder));
		}
		return selectedElements;
	}

	@RequiredReadAction
	@Override
	public boolean processElements(@Nonnull final Processor<PsiElement> processor, boolean deep)
	{
		if(processElementsImpl(processor))
		{
			return !deep || getSuperContext().processElements(processor, true);
		}
		else
		{
			return false;
		}
	}

	@RequiredReadAction
	public boolean processElementsImpl(@Nonnull Processor<PsiElement> processor)
	{
		Map<String, CSharpElementGroup<PsiElement>> map = myOtherCollectorValue.getValue().toMap();

		return map == null || ContainerUtil.process(map.values(), processor);
	}

	@Nonnull
	public DotNetGenericExtractor getExtractor()
	{
		return myExtractor;
	}

	@Override
	@Nonnull
	public T getElement()
	{
		return myElement;
	}
}
