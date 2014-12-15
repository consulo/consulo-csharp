package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.overrideSystem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementCompareUtil;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.resolve.CSharpElementGroupImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.AbstractScopeProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.ExecuteTarget;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.ExecuteTargetUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.psi.DotNetVirtualImplementOwner;
import com.intellij.psi.PsiElement;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 14.12.14
 */
public class OverrideUtil
{
	@NotNull
	public static PsiElement[] fiterOverridedAndHiddedElements(@NotNull AbstractScopeProcessor processor, @NotNull PsiElement scopeElement,
			@NotNull PsiElement[] psiElements)
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

		return fiterOverridedAndHiddedElements(scopeElement, elements);
	}

	@NotNull
	public static PsiElement[] fiterOverridedAndHiddedElements(@NotNull PsiElement scopeElement, @NotNull Collection<PsiElement> elements)
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
				DotNetType typeForImplement = ((DotNetVirtualImplementOwner) element).getTypeForImplement();

				for(PsiElement tempIterateElement : elements)
				{
					// skip self
					if(tempIterateElement == element)
					{
						continue;
					}

					if(CSharpElementCompareUtil.isEqual(tempIterateElement, element, CSharpElementCompareUtil.CHECK_RETURN_TYPE, scopeElement))
					{
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
			return new PsiElement[] {new CSharpElementGroupImpl<PsiElement>(scopeElement.getProject(), "override", groupElements)};
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
}
