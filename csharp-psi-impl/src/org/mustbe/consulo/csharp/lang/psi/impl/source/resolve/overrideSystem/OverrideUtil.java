package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.overrideSystem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpArrayMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementCompareUtil;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.impl.resolve.CSharpElementGroupImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.resolve.CSharpResolveContextUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.AbstractScopeProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.ExecuteTarget;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.ExecuteTargetUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.MemberResolveScopeProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.csharp.lang.psi.resolve.MemberByNameSelector;
import org.mustbe.consulo.csharp.lang.psi.resolve.StaticResolveSelectors;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.psi.DotNetVirtualImplementOwner;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.CommonProcessors;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 14.12.14
 */
public class OverrideUtil
{
	@NotNull
	public static PsiElement[] filterOverrideElements(@NotNull AbstractScopeProcessor processor,
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
			return new PsiElement[]{new CSharpElementGroupImpl<PsiElement>(scopeElement.getProject(), "override", groupElements)};
		}
		else if(groupElements.isEmpty())
		{
			return ContainerUtil.toArray(elseElements, PsiElement.ARRAY_FACTORY);
		}
		else
		{
			elseElements.add(new CSharpElementGroupImpl<PsiElement>(scopeElement.getProject(), "override", groupElements));
			return ContainerUtil.toArray(elseElements, PsiElement.ARRAY_FACTORY);
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

		MemberResolveScopeProcessor processor = new MemberResolveScopeProcessor(parent, new ExecuteTarget[]{
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

		return overrideProcessor.getResults();
	}

	@NotNull
	public static Collection<PsiElement> collectMembersWithModifier(@NotNull PsiElement element,
			@NotNull DotNetGenericExtractor extractor,
			@NotNull CSharpModifier modifier)
	{
		List<PsiElement> psiElements = new SmartList<PsiElement>();
		for(PsiElement psiElement : getAllMembers(element, element.getResolveScope(), extractor))
		{
			if(psiElement instanceof DotNetModifierListOwner && ((DotNetModifierListOwner) psiElement).hasModifier(modifier))
			{
				psiElements.add(psiElement);
			}
		}
		return psiElements;
	}

	@NotNull
	public static Collection<PsiElement> getAllMembers(@NotNull PsiElement element,
			@NotNull GlobalSearchScope scope,
			@NotNull DotNetGenericExtractor extractor)
	{
		CommonProcessors.CollectProcessor<PsiElement> collectProcessor = new CommonProcessors.CollectProcessor<PsiElement>();
		CSharpResolveContextUtil.createContext(extractor, scope, element).processElements(collectProcessor, true);

		Collection<PsiElement> results = collectProcessor.getResults();

		List<PsiElement> mergedElements = CSharpResolveUtil.mergeGroupsToIterable(results);
		PsiElement[] psiElements = OverrideUtil.filterOverrideElements(element, mergedElements, OverrideProcessor.ALWAYS_TRUE);

		return CSharpResolveUtil.mergeGroupsToIterable(psiElements);
	}
}
