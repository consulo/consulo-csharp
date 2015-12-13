package org.mustbe.consulo.csharp.lang.psi.impl.resolve;

import gnu.trove.THashMap;
import gnu.trove.THashSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.consulo.lombok.annotations.LazyInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.*;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.wrapper.GenericUnwrapTool;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpMethodImplUtil;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import org.mustbe.consulo.dotnet.lang.psi.impl.stub.MsilHelper;
import org.mustbe.consulo.dotnet.psi.DotNetElement;
import org.mustbe.consulo.dotnet.psi.DotNetModifierListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Consumer;
import com.intellij.util.Processor;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;

/**
 * @author VISTALL
 * @since 26.10.14
 */
public abstract class CSharpBaseResolveContext<T extends DotNetElement & DotNetModifierListOwner> implements CSharpResolveContext
{
	protected static class Collector extends CSharpElementVisitor
	{
		private final DotNetGenericExtractor myGenericExtractor;
		private List<CSharpConstructorDeclaration> myDeConstructors;
		private List<CSharpConstructorDeclaration> myConstructors;
		private MultiMap<IElementType, CSharpMethodDeclaration> myOperatorsMap;
		private MultiMap<String, CSharpMethodDeclaration> myExtensionMap;
		private MultiMap<DotNetTypeRef, CSharpConversionMethodDeclaration> myConversionMap;
		private List<CSharpIndexMethodDeclaration> myIndexMethods;
		private MultiMap<String, PsiElement> myOtherElements = new MultiMap<String, PsiElement>();

		public Collector(DotNetGenericExtractor genericExtractor)
		{
			myGenericExtractor = genericExtractor;
		}

		@Override
		public void visitConstructorDeclaration(CSharpConstructorDeclaration declaration)
		{
			if(declaration.isDeConstructor())
			{
				if(myDeConstructors == null)
				{
					myDeConstructors = new SmartList<CSharpConstructorDeclaration>();
				}
				myDeConstructors.add(declaration);
			}
			else
			{
				if(myConstructors == null)
				{
					myConstructors = new SmartList<CSharpConstructorDeclaration>();
				}
				myConstructors.add(GenericUnwrapTool.extract(declaration, myGenericExtractor));
			}
		}

		@Override
		public void visitConversionMethodDeclaration(CSharpConversionMethodDeclaration element)
		{
			if(myConversionMap == null)
			{
				myConversionMap = new MultiMap<DotNetTypeRef, CSharpConversionMethodDeclaration>();
			}

			myConversionMap.putValue(element.getConversionTypeRef(), element);
		}

		@Override
		public void visitMethodDeclaration(CSharpMethodDeclaration declaration)
		{
			if(declaration.isOperator())
			{
				IElementType operatorElementType = declaration.getOperatorElementType();
				if(operatorElementType == null)
				{
					return;
				}

				if(myOperatorsMap == null)
				{
					myOperatorsMap = new MultiMap<IElementType, CSharpMethodDeclaration>();
				}

				myOperatorsMap.putValue(operatorElementType, declaration);
			}
			else
			{
				if(CSharpMethodImplUtil.isExtensionMethod(declaration))
				{
					String name = declaration.getName();
					if(name == null)
					{
						return;
					}

					if(myExtensionMap == null)
					{
						myExtensionMap = new MultiMap<String, CSharpMethodDeclaration>();
					}

					myExtensionMap.putValue(name, declaration);
				}

				putIfNotNull(declaration.getName(), declaration, myOtherElements);
			}
		}

		@Override
		public void visitArrayMethodDeclaration(CSharpIndexMethodDeclaration declaration)
		{
			if(myIndexMethods == null)
			{
				myIndexMethods = new SmartList<CSharpIndexMethodDeclaration>();
			}
			myIndexMethods.add(GenericUnwrapTool.extract(declaration, myGenericExtractor));
		}

		@Override
		public void visitEnumConstantDeclaration(CSharpEnumConstantDeclaration declaration)
		{
			putIfNotNull(declaration.getName(), declaration, myOtherElements);
		}

		@Override
		public void visitFieldDeclaration(CSharpFieldDeclaration declaration)
		{
			putIfNotNull(declaration.getName(), declaration, myOtherElements);
		}

		@Override
		public void visitEventDeclaration(CSharpEventDeclaration declaration)
		{
			putIfNotNull(declaration.getName(), declaration, myOtherElements);
		}

		@Override
		public void visitPropertyDeclaration(CSharpPropertyDeclaration declaration)
		{
			putIfNotNull(declaration.getName(), declaration, myOtherElements);
		}

		@Override
		public void visitTypeDeclaration(CSharpTypeDeclaration declaration)
		{
			putIfNotNull(declaration.getName(), declaration, myOtherElements);
		}

		private <K, V extends DotNetNamedElement> void putIfNotNull(@Nullable K key, @NotNull V value, @NotNull MultiMap<K, PsiElement> map)
		{
			if(key == null)
			{
				return;
			}
			map.putValue(key, GenericUnwrapTool.extract(value, myGenericExtractor));
		}
	}

	@NotNull
	protected final T myElement;
	@NotNull
	protected final DotNetGenericExtractor myExtractor;
	private Set<PsiElement> myRecursiveGuardSet;
	@Nullable
	private CSharpElementGroup<CSharpIndexMethodDeclaration> myIndexMethodGroup;
	@Nullable
	private CSharpElementGroup<CSharpConstructorDeclaration> myConstructorGroup;
	@Nullable
	private CSharpElementGroup<CSharpConstructorDeclaration> myDeConstructorGroup;

	@Nullable
	private final Map<DotNetTypeRef, CSharpElementGroup<CSharpConversionMethodDeclaration>> myConversionMap;
	@Nullable
	private final Map<IElementType, CSharpElementGroup<CSharpMethodDeclaration>> myOperatorMap;
	@Nullable
	private final Map<String, CSharpElementGroup<CSharpMethodDeclaration>> myExtensionMap;
	@Nullable
	private final Map<String, CSharpElementGroup<PsiElement>> myOtherElements;

	@RequiredReadAction
	public CSharpBaseResolveContext(@NotNull T element, @NotNull DotNetGenericExtractor extractor, @Nullable Set<PsiElement> recursiveGuardSet)
	{
		myElement = element;
		myExtractor = extractor;
		myRecursiveGuardSet = recursiveGuardSet;
		final Project project = element.getProject();

		final Collector collector = new Collector(extractor);

		processMembers(element, collector);

		Consumer<DotNetElement> elementConsumer = new Consumer<DotNetElement>()
		{
			@Override
			public void consume(DotNetElement dotNetElement)
			{
				dotNetElement.accept(collector);
			}
		};

		for(CSharpAdditionalMemberProvider provider : getAdditionalTypeMemberProviders())
		{
			provider.processAdditionalMembers(element, extractor, elementConsumer);
		}

		myOtherElements = convertToGroup(project, collector.myOtherElements);
		myIndexMethodGroup = toGroup(project, "[]", collector.myIndexMethods);
		myConstructorGroup = toGroup(project, MsilHelper.CONSTRUCTOR_NAME, collector.myConstructors);
		myDeConstructorGroup = toGroup(project, "~" + MsilHelper.CONSTRUCTOR_NAME, collector.myDeConstructors);
		myOperatorMap = convertToGroup(project, collector.myOperatorsMap);
		myExtensionMap = convertToGroup(project, collector.myExtensionMap);
		myConversionMap = convertToGroup(project, collector.myConversionMap);
	}

	public abstract void processMembers(T element, Collector collector);

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

				contexts.add(CSharpResolveContextUtil.createContext(genericExtractor, myElement.getResolveScope(), resolvedElement,
						alreadyProcessedItem));
			}
		}

		if(contexts.isEmpty())
		{
			return EMPTY;
		}
		return new CSharpCompositeResolveContext(myElement.getProject(), contexts.toArray(new CSharpResolveContext[contexts.size()]));
	}

	@NotNull
	@LazyInstance
	private static CSharpAdditionalMemberProvider[] getAdditionalTypeMemberProviders()
	{
		return CSharpAdditionalMemberProvider.EP_NAME.getExtensions();
	}

	@Nullable
	private static <T extends PsiElement> CSharpElementGroup<T> toGroup(@NotNull Project project, @NotNull String key, @Nullable List<T> elements)
	{
		if(ContainerUtil.isEmpty(elements))
		{
			return null;
		}
		return new CSharpElementGroupImpl<T>(project, key, elements);
	}

	@Nullable
	public static <K, V extends PsiElement> Map<K, CSharpElementGroup<V>> convertToGroup(@NotNull Project project, @Nullable MultiMap<K, V> multiMap)
	{
		if(multiMap == null || multiMap.isEmpty())
		{
			return null;
		}
		Map<K, CSharpElementGroup<V>> map = new THashMap<K, CSharpElementGroup<V>>(multiMap.size());
		for(Map.Entry<K, Collection<V>> entry : multiMap.entrySet())
		{
			map.put(entry.getKey(), new CSharpElementGroupImpl<V>(project, entry.getKey(), entry.getValue()));
		}
		return map;
	}

	@Nullable
	@Override
	public CSharpElementGroup<CSharpIndexMethodDeclaration> indexMethodGroup(boolean deep)
	{
		if(myIndexMethodGroup == null)
		{
			return deep ? getSuperContext().indexMethodGroup(deep) : null;
		}
		else
		{
			CSharpElementGroup<CSharpIndexMethodDeclaration> deepGroup = deep ? getSuperContext().indexMethodGroup(deep) : null;

			if(deepGroup == null)
			{
				return myIndexMethodGroup;
			}
			return new CSharpCompositeElementGroupImpl<CSharpIndexMethodDeclaration>(myElement.getProject(), Arrays.asList(myIndexMethodGroup,
					deepGroup));
		}
	}

	@Nullable
	@Override
	public CSharpElementGroup<CSharpConstructorDeclaration> constructorGroup()
	{
		return myConstructorGroup;
	}

	@Nullable
	@Override
	public CSharpElementGroup<CSharpConstructorDeclaration> deConstructorGroup()
	{
		return myDeConstructorGroup;
	}

	@Nullable
	@Override
	public CSharpElementGroup<CSharpMethodDeclaration> findOperatorGroupByTokenType(@NotNull IElementType type, boolean deep)
	{
		if(myOperatorMap == null)
		{
			return deep ? getSuperContext().findOperatorGroupByTokenType(type, deep) : null;
		}
		else
		{
			CSharpElementGroup<CSharpMethodDeclaration> deepGroup = deep ? getSuperContext().findOperatorGroupByTokenType(type, deep) : null;

			if(deepGroup == null)
			{
				return myOperatorMap.get(type);
			}

			CSharpElementGroup<CSharpMethodDeclaration> thisGroup = myOperatorMap.get(type);
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
		if(myConversionMap == null)
		{
			return deep ? getSuperContext().findConversionMethodGroup(typeRef, deep) : null;
		}
		else
		{
			CSharpElementGroup<CSharpConversionMethodDeclaration> deepGroup = deep ? getSuperContext().findConversionMethodGroup(typeRef,
					deep) : null;

			if(deepGroup == null)
			{
				return myConversionMap.get(typeRef);
			}

			CSharpElementGroup<CSharpConversionMethodDeclaration> thisGroup = myConversionMap.get(typeRef);
			if(thisGroup == null)
			{
				return deepGroup;
			}
			return new CSharpCompositeElementGroupImpl<CSharpConversionMethodDeclaration>(myElement.getProject(), Arrays.asList(thisGroup,
					deepGroup));
		}
	}

	@RequiredReadAction
	@Nullable
	@Override
	public CSharpElementGroup<CSharpMethodDeclaration> findExtensionMethodGroupByName(@NotNull String name)
	{
		if(myExtensionMap == null)
		{
			return null;
		}
		return myExtensionMap.get(name);
	}

	@RequiredReadAction
	@Override
	public boolean processExtensionMethodGroups(@NotNull Processor<CSharpElementGroup<CSharpMethodDeclaration>> processor)
	{
		return myExtensionMap == null || ContainerUtil.process(myExtensionMap.values(), processor);
	}

	@RequiredReadAction
	@Override
	@NotNull
	public PsiElement[] findByName(@NotNull String name, boolean deep, @NotNull UserDataHolder holder)
	{
		PsiElement[] selectedElements;
		if(myOtherElements == null)
		{
			selectedElements = PsiElement.EMPTY_ARRAY;
		}
		else
		{
			CSharpElementGroup<PsiElement> group = myOtherElements.get(name);
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
			selectedElements = ArrayUtil.mergeArrays(selectedElements, getSuperContext().findByName(name, deep, holder));
		}
		return selectedElements;
	}

	@RequiredReadAction
	@Override
	public boolean processElements(@NotNull final Processor<PsiElement> processor, boolean deep)
	{
		if(processElementsImpl(processor))
		{
			if(!deep)
			{
				return true;
			}

			return getSuperContext().processElements(processor, true);
		}
		else
		{
			return false;
		}
	}

	public boolean processElementsImpl(@NotNull Processor<PsiElement> processor)
	{
		return myOtherElements == null || ContainerUtil.process(myOtherElements.values(), processor);
	}

	@NotNull
	public T getElement()
	{
		return myElement;
	}
}
