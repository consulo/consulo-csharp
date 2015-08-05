package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.overrideSystem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.consulo.lombok.annotations.Logger;
import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpArrayMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementCompareUtil;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.CSharpTransform;
import org.mustbe.consulo.csharp.lang.psi.impl.resolve.CSharpElementGroupImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.resolve.CSharpResolveContextUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.ExecuteTarget;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.ExecuteTargetUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.MemberResolveScopeProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveSelector;
import org.mustbe.consulo.csharp.lang.psi.resolve.MemberByNameSelector;
import org.mustbe.consulo.csharp.lang.psi.resolve.StaticResolveSelectors;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierList;
import org.mustbe.consulo.dotnet.psi.DotNetModifierListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import org.mustbe.consulo.dotnet.psi.DotNetVirtualImplementOwner;
import org.mustbe.consulo.dotnet.psi.search.searches.TypeInheritorsSearch;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.CommonProcessors;
import com.intellij.util.Processor;
import com.intellij.util.Query;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 14.12.14
 */
@Logger
public class OverrideUtil
{
	@RequiredReadAction
	public static CSharpModifier getRequiredOverrideModifier(@NotNull DotNetModifierListOwner modifierListOwner)
	{
		DotNetModifierList modifierList = modifierListOwner.getModifierList();
		if(modifierList == null)
		{
			return null;
		}

		if(modifierList.hasModifier(CSharpModifier.INTERFACE_ABSTRACT))
		{
			return null;
		}

		if(modifierList.hasModifierInTree(CSharpModifier.ABSTRACT) || modifierList.hasModifierInTree(CSharpModifier.VIRTUAL) || modifierList
				.hasModifierInTree(CSharpModifier.OVERRIDE))
		{
			return CSharpModifier.OVERRIDE;
		}
		return CSharpModifier.NEW;
	}

	@NotNull
	public static PsiElement[] filterOverrideElements(@NotNull PsiScopeProcessor processor,
			@NotNull PsiElement scopeElement,
			@NotNull PsiElement[] psiElements,
			@NotNull OverrideProcessor overrideProcessor)
	{
		if(psiElements.length == 0)
		{
			return psiElements;
		}
		if(!ExecuteTargetUtil.canProcess(processor, ExecuteTarget.ELEMENT_GROUP, ExecuteTarget.EVENT, ExecuteTarget.PROPERTY))
		{
			List<PsiElement> elements = CSharpResolveUtil.mergeGroupsToIterable(psiElements);
			return ContainerUtil.toArray(elements, PsiElement.ARRAY_FACTORY);
		}

		List<PsiElement> elements = CSharpResolveUtil.mergeGroupsToIterable(psiElements);

		return filterOverrideElements(scopeElement, elements, overrideProcessor);
	}

	@NotNull
	@SuppressWarnings("unchecked")
	public static PsiElement[] filterOverrideElements(@NotNull PsiElement scopeElement,
			@NotNull Collection<PsiElement> elements,
			@NotNull OverrideProcessor overrideProcessor)
	{
		List<PsiElement> copyElements = new ArrayList<PsiElement>(elements);

		for(PsiElement element : elements)
		{
			if(!copyElements.contains(element))
			{
				continue;
			}

			if(element instanceof DotNetVirtualImplementOwner)
			{
				if(element instanceof CSharpMethodDeclaration && ((CSharpMethodDeclaration) element).isDelegate())
				{
					continue;
				}
				DotNetVirtualImplementOwner virtualImplementOwner = (DotNetVirtualImplementOwner) element;

				DotNetType typeForImplement = virtualImplementOwner.getTypeForImplement();

				for(PsiElement tempIterateElement : elements)
				{
					// skip self
					if(tempIterateElement == element)
					{
						continue;
					}

					if(CSharpElementCompareUtil.isEqual(tempIterateElement, element, CSharpElementCompareUtil.CHECK_RETURN_TYPE, scopeElement))
					{
						if(!overrideProcessor.elementOverride(virtualImplementOwner, (DotNetVirtualImplementOwner) tempIterateElement))
						{
							return PsiElement.EMPTY_ARRAY;
						}
						copyElements.remove(tempIterateElement);
					}
				}

				// if he have hide impl, remove it
				if(typeForImplement != null)
				{
					copyElements.remove(element);
				}
			}
		}

		List<PsiElement> groupElements = new SmartList<PsiElement>();
		List<PsiElement> elseElements = new SmartList<PsiElement>();

		for(PsiElement copyElement : copyElements)
		{
			if(copyElement instanceof DotNetLikeMethodDeclaration)
			{
				groupElements.add(copyElement);
			}
			else
			{
				elseElements.add(copyElement);
			}
		}

		if(elseElements.isEmpty() && groupElements.isEmpty())
		{
			return PsiElement.EMPTY_ARRAY;
		}
		else if(elseElements.isEmpty())
		{
			return new PsiElement[]{
					new CSharpElementGroupImpl<PsiElement>(scopeElement.getProject(), getNameForGroup(groupElements), groupElements)
			};
		}
		else if(groupElements.isEmpty())
		{
			return ContainerUtil.toArray(elseElements, PsiElement.ARRAY_FACTORY);
		}
		else
		{
			elseElements.add(new CSharpElementGroupImpl<PsiElement>(scopeElement.getProject(), getNameForGroup(groupElements), groupElements));
			return ContainerUtil.toArray(elseElements, PsiElement.ARRAY_FACTORY);
		}
	}

	@NotNull
	private static String getNameForGroup(List<PsiElement> elements)
	{
		assert !elements.isEmpty();
		PsiElement element = elements.get(0);
		if(element instanceof DotNetVariable)
		{
			return ((DotNetVariable) element).getName();
		}
		else if(element instanceof CSharpArrayMethodDeclaration)
		{
			return "this[]";
		}
		else if(element instanceof DotNetLikeMethodDeclaration)
		{
			String name = ((DotNetLikeMethodDeclaration) element).getName();
			assert name != null : element.getClass().getName();
			return name;
		}
		else
		{
			LOGGER.error(element.getClass() + " is not handled");
			return "override";
		}
	}

	public static boolean isAllowForOverride(PsiElement parent)
	{
		if(parent instanceof CSharpMethodDeclaration &&
				!((CSharpMethodDeclaration) parent).isDelegate() && !((CSharpMethodDeclaration) parent).hasModifier(DotNetModifier.STATIC))
		{
			return true;
		}
		return parent instanceof DotNetVirtualImplementOwner;
	}

	@NotNull
	public static Collection<DotNetVirtualImplementOwner> collectOverridingMembers(final DotNetVirtualImplementOwner target)
	{
		PsiElement parent = target.getParent();
		if(parent == null)
		{
			return Collections.emptyList();
		}
		OverrideProcessor.Collector overrideProcessor = new OverrideProcessor.Collector();

		MemberResolveScopeProcessor processor = new MemberResolveScopeProcessor(parent, CommonProcessors.<ResolveResult>alwaysTrue(),
				new ExecuteTarget[]{
				ExecuteTarget.MEMBER,
				ExecuteTarget.ELEMENT_GROUP
		}, overrideProcessor);

		ResolveState state = ResolveState.initial();
		if(target instanceof CSharpArrayMethodDeclaration)
		{
			state = state.put(CSharpResolveUtil.SELECTOR, StaticResolveSelectors.INDEX_METHOD_GROUP);
		}
		else
		{
			String name = ((PsiNamedElement) target).getName();
			if(name == null)
			{
				return Collections.emptyList();
			}
			state = state.put(CSharpResolveUtil.SELECTOR, new MemberByNameSelector(name));
		}

		CSharpResolveUtil.walkChildren(processor, parent, false, true, state);

		List<DotNetVirtualImplementOwner> results = overrideProcessor.getResults();

		// need filter result due it ill return all elements with target selector
		ListIterator<DotNetVirtualImplementOwner> listIterator = results.listIterator();
		while(listIterator.hasNext())
		{
			DotNetVirtualImplementOwner next = listIterator.next();
			if(!CSharpElementCompareUtil.isEqual(next, target, CSharpElementCompareUtil.CHECK_RETURN_TYPE, target))
			{
				listIterator.remove();
			}
		}
		return results;
	}

	@NotNull
	public static Collection<DotNetVirtualImplementOwner> collectOverridenMembers(final DotNetVirtualImplementOwner target)
	{
		PsiElement parent = target.getParent();
		if(!(parent instanceof DotNetTypeDeclaration))
		{
			return Collections.emptyList();
		}
		final CSharpResolveSelector selector;
		if(target instanceof CSharpArrayMethodDeclaration)
		{
			selector = StaticResolveSelectors.INDEX_METHOD_GROUP;
		}
		else
		{
			String name = ((PsiNamedElement) target).getName();
			if(name != null)
			{
				selector = new MemberByNameSelector(name);
			}
			else
			{
				selector = null;
			}
		}

		if(selector == null)
		{
			return Collections.emptyList();
		}
		final GlobalSearchScope resolveScope = target.getResolveScope();

		final List<DotNetVirtualImplementOwner> list = new ArrayList<DotNetVirtualImplementOwner>();
		Query<DotNetTypeDeclaration> search = TypeInheritorsSearch.search((DotNetTypeDeclaration) parent, true, CSharpTransform.INSTANCE);
		search.forEach(new Processor<DotNetTypeDeclaration>()
		{
			@Override
			public boolean process(DotNetTypeDeclaration typeDeclaration)
			{
				CSharpResolveContext context = CSharpResolveContextUtil.createContext(DotNetGenericExtractor.EMPTY, resolveScope, typeDeclaration);

				PsiElement[] elements = selector.doSelectElement(context, false);
				for(PsiElement element : CSharpResolveUtil.mergeGroupsToIterable(elements))
				{
					if(element == target)
					{
						continue;
					}
					if(CSharpElementCompareUtil.isEqual(element, target, CSharpElementCompareUtil.CHECK_RETURN_TYPE, target))
					{
						list.add((DotNetVirtualImplementOwner) element);
					}
				}
				return true;
			}
		});

		return list;
	}

	@NotNull
	@RequiredReadAction
	public static Collection<DotNetModifierListOwner> collectMembersWithModifier(@NotNull PsiElement element,
			@NotNull DotNetGenericExtractor extractor,
			@NotNull CSharpModifier modifier)
	{
		List<DotNetModifierListOwner> psiElements = new SmartList<DotNetModifierListOwner>();
		for(PsiElement psiElement : getAllMembers(element, element.getResolveScope(), extractor, false))
		{
			if(psiElement instanceof DotNetModifierListOwner && ((DotNetModifierListOwner) psiElement).hasModifier(modifier))
			{
				psiElements.add((DotNetModifierListOwner) psiElement);
			}
		}
		return psiElements;
	}

	@NotNull
	@RequiredReadAction
	public static Collection<PsiElement> getAllMembers(@NotNull final PsiElement targetTypeDeclaration,
			@NotNull GlobalSearchScope scope,
			@NotNull DotNetGenericExtractor extractor,
			boolean completion)
	{
		CommonProcessors.CollectProcessor<PsiElement> collectProcessor = new CommonProcessors.CollectProcessor<PsiElement>();
		CSharpResolveContextUtil.createContext(extractor, scope, targetTypeDeclaration).processElements(collectProcessor, true);

		Collection<PsiElement> results = collectProcessor.getResults();

		List<PsiElement> mergedElements = CSharpResolveUtil.mergeGroupsToIterable(results);
		PsiElement[] psiElements = OverrideUtil.filterOverrideElements(targetTypeDeclaration, mergedElements, OverrideProcessor.ALWAYS_TRUE);

		List<PsiElement> elements = CSharpResolveUtil.mergeGroupsToIterable(psiElements);
		// filter self methods, we need it in circular extends
		elements = ContainerUtil.filter(elements, new Condition<PsiElement>()
		{
			@Override
			public boolean value(PsiElement element)
			{
				return  element.getParent() != targetTypeDeclaration;
			}
		});
		return completion ? elements : ContainerUtil.filter(elements, new Condition<PsiElement>()
		{
			@Override
			public boolean value(PsiElement element)
			{
				return !(element instanceof DotNetTypeDeclaration);
			}
		});
	}
}
