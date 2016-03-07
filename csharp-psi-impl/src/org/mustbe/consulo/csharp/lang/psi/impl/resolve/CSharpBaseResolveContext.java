package org.mustbe.consulo.csharp.lang.psi.impl.resolve;

import gnu.trove.THashSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpConversionMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpIndexMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.resolve.baseResolveContext.MapElementGroupCollectors;
import org.mustbe.consulo.csharp.lang.psi.impl.resolve.baseResolveContext.SimpleElementGroupCollectors;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import org.mustbe.consulo.dotnet.psi.DotNetElement;
import org.mustbe.consulo.dotnet.psi.DotNetModifierListOwner;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Processor;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 26.10.14
 */
public abstract class CSharpBaseResolveContext<T extends DotNetElement & DotNetModifierListOwner> implements CSharpResolveContext
{
	private final NotNullLazyValue<SimpleElementGroupCollectors.IndexMethod> myIndexMethodCollectorValue = new NotNullLazyValue<SimpleElementGroupCollectors.IndexMethod>()
	{
		@NotNull
		@Override
		protected SimpleElementGroupCollectors.IndexMethod compute()
		{
			return new SimpleElementGroupCollectors.IndexMethod(CSharpBaseResolveContext.this);
		}
	};

	private final NotNullLazyValue<SimpleElementGroupCollectors.Constructor> myConstructorCollectorValue = new NotNullLazyValue<SimpleElementGroupCollectors.Constructor>()
	{
		@NotNull
		@Override
		protected SimpleElementGroupCollectors.Constructor compute()
		{
			return new SimpleElementGroupCollectors.Constructor(CSharpBaseResolveContext.this);
		}
	};

	private final NotNullLazyValue<SimpleElementGroupCollectors.DeConstructor> myDeConstructorCollectorValue = new NotNullLazyValue<SimpleElementGroupCollectors.DeConstructor>()
	{
		@NotNull
		@Override
		protected SimpleElementGroupCollectors.DeConstructor compute()
		{
			return new SimpleElementGroupCollectors.DeConstructor(CSharpBaseResolveContext.this);
		}
	};

	private final NotNullLazyValue<MapElementGroupCollectors.ConversionMethod> myConversionMethodCollectorValue = new NotNullLazyValue<MapElementGroupCollectors.ConversionMethod>()
	{
		@NotNull
		@Override
		protected MapElementGroupCollectors.ConversionMethod compute()
		{
			return new MapElementGroupCollectors.ConversionMethod(CSharpBaseResolveContext.this);
		}
	};

	private final NotNullLazyValue<MapElementGroupCollectors.OperatorMethod> myOperatorMethodCollectorValue = new NotNullLazyValue<MapElementGroupCollectors.OperatorMethod>()
	{
		@NotNull
		@Override
		protected MapElementGroupCollectors.OperatorMethod compute()
		{
			return new MapElementGroupCollectors.OperatorMethod(CSharpBaseResolveContext.this);
		}
	};

	private final NotNullLazyValue<MapElementGroupCollectors.Other> myOtherCollectorValue = new NotNullLazyValue<MapElementGroupCollectors.Other>()
	{
		@NotNull
		@Override
		protected MapElementGroupCollectors.Other compute()
		{
			return new MapElementGroupCollectors.Other(CSharpBaseResolveContext.this);
		}
	};

	@NotNull
	protected final T myElement;
	@NotNull
	protected final DotNetGenericExtractor myExtractor;
	@Nullable
	private Set<PsiElement> myRecursiveGuardSet;

	@RequiredReadAction
	public CSharpBaseResolveContext(@NotNull T element, @NotNull DotNetGenericExtractor extractor, @Nullable Set<PsiElement> recursiveGuardSet)
	{
		myElement = element;
		myExtractor = extractor;
		myRecursiveGuardSet = recursiveGuardSet;
	}

	public abstract void acceptChildren(CSharpElementVisitor visitor);

	@NotNull
	protected abstract List<DotNetTypeRef> getExtendTypeRefs();

	@NotNull
	@RequiredReadAction
	private CSharpResolveContext getSuperContext()
	{
		THashSet<PsiElement> alreadyProcessedItem = new THashSet<PsiElement>();
		if(myRecursiveGuardSet != null)
		{
			alreadyProcessedItem.addAll(myRecursiveGuardSet);
		}
		return getSuperContextImpl(alreadyProcessedItem);
	}

	@NotNull
	@RequiredReadAction
	private CSharpResolveContext getSuperContextImpl(Set<PsiElement> alreadyProcessedItem)
	{
		List<DotNetTypeRef> superTypes = getExtendTypeRefs();

		if(superTypes.isEmpty())
		{
			return EMPTY;
		}

		List<CSharpResolveContext> contexts = new ArrayList<CSharpResolveContext>(superTypes.size());
		for(DotNetTypeRef dotNetTypeRef : superTypes)
		{
			DotNetTypeResolveResult typeResolveResult = dotNetTypeRef.resolve(myElement);
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
			return new CSharpCompositeElementGroupImpl<CSharpIndexMethodDeclaration>(myElement.getProject(), Arrays.asList(elementGroup, deepGroup));
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
	public CSharpElementGroup<CSharpMethodDeclaration> findOperatorGroupByTokenType(@NotNull IElementType type, boolean deep)
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
			return new CSharpCompositeElementGroupImpl<CSharpMethodDeclaration>(myElement.getProject(), Arrays.asList(thisGroup, deepGroup));
		}
	}

	@RequiredReadAction
	@Nullable
	@Override
	public CSharpElementGroup<CSharpConversionMethodDeclaration> findConversionMethodGroup(@NotNull DotNetTypeRef typeRef, boolean deep)
	{
		Map<DotNetTypeRef, CSharpElementGroup<CSharpConversionMethodDeclaration>> map = myConversionMethodCollectorValue.getValue().toMap();
		if(map == null)
		{
			return deep ? getSuperContext().findConversionMethodGroup(typeRef, true) : null;
		}
		else
		{
			CSharpElementGroup<CSharpConversionMethodDeclaration> deepGroup = deep ? getSuperContext().findConversionMethodGroup(typeRef, true) : null;

			if(deepGroup == null)
			{
				return map.get(typeRef);
			}

			CSharpElementGroup<CSharpConversionMethodDeclaration> thisGroup = map.get(typeRef);
			if(thisGroup == null)
			{
				return deepGroup;
			}
			return new CSharpCompositeElementGroupImpl<CSharpConversionMethodDeclaration>(myElement.getProject(), Arrays.asList(thisGroup, deepGroup));
		}
	}

	@RequiredReadAction
	@Nullable
	@Override
	public CSharpElementGroup<CSharpMethodDeclaration> findExtensionMethodGroupByName(@NotNull String name)
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
		final List<CSharpMethodDeclaration> extensions = new SmartList<CSharpMethodDeclaration>();
		elementGroup.process(new Processor<PsiElement>()
		{
			@Override
			public boolean process(PsiElement element)
			{
				if(element instanceof CSharpMethodDeclaration && ((CSharpMethodDeclaration) element).isExtension())
				{
					extensions.add((CSharpMethodDeclaration) element);
				}
				return true;
			}
		});
		if(extensions.isEmpty())
		{
			return null;
		}
		return new CSharpElementGroupImpl<CSharpMethodDeclaration>(elementGroup.getProject(), elementGroup.getKey(), extensions);
	}

	@RequiredReadAction
	@Override
	public boolean processExtensionMethodGroups(@NotNull Processor<CSharpElementGroup<CSharpMethodDeclaration>> processor)
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
	@NotNull
	public PsiElement[] findByName(@NotNull String name, boolean deep, @NotNull UserDataHolder holder)
	{
		Map<String, CSharpElementGroup<PsiElement>> map = myOtherCollectorValue.getValue().toMap();

		PsiElement[] selectedElements;
		if(map == null)
		{
			selectedElements = PsiElement.EMPTY_ARRAY;
		}
		else
		{
			CSharpElementGroup<PsiElement> group = map.get(name);
			if(group == null)
			{
				selectedElements = PsiElement.EMPTY_ARRAY;
			}
			else
			{
				selectedElements = new PsiElement[]{group};
			}
		}

		if(deep)
		{
			selectedElements = ArrayUtil.mergeArrays(selectedElements, getSuperContext().findByName(name, true, holder));
		}
		return selectedElements;
	}

	@RequiredReadAction
	@Override
	public boolean processElements(@NotNull final Processor<PsiElement> processor, boolean deep)
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
	public boolean processElementsImpl(@NotNull Processor<PsiElement> processor)
	{
		Map<String, CSharpElementGroup<PsiElement>> map = myOtherCollectorValue.getValue().toMap();

		return map == null || ContainerUtil.process(map.values(), processor);
	}

	@NotNull
	public DotNetGenericExtractor getExtractor()
	{
		return myExtractor;
	}

	@Override
	@NotNull
	public T getElement()
	{
		return myElement;
	}
}
