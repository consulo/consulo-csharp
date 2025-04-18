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

package consulo.csharp.lang.impl.psi.source.resolve.sorter;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.util.RecursionManager;
import consulo.csharp.lang.impl.psi.CSharpContextUtil;
import consulo.csharp.lang.psi.*;
import consulo.csharp.lang.impl.psi.source.CSharpReferenceExpressionImplUtil;
import consulo.dotnet.psi.DotNetGenericParameterListOwner;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.psi.resolve.DotNetNamespaceAsElement;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiNamedElement;
import consulo.language.psi.ResolveResult;
import consulo.util.lang.Comparing;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Comparator;

/**
 * @author VISTALL
 * @since 28.02.2015
 */
public class StaticVsInstanceComparator implements Comparator<ResolveResult>
{
	@Nonnull
	@RequiredReadAction
	public static StaticVsInstanceComparator create(@Nonnull PsiElement element)
	{
		CSharpReferenceExpressionEx parent = null;
		if(element instanceof CSharpReferenceExpression)
		{
			if(((CSharpReferenceExpression) element).getQualifier() == null && element.getParent() instanceof CSharpReferenceExpressionEx)
			{
				parent = (CSharpReferenceExpressionEx) element.getParent();
			}
		}
		return new StaticVsInstanceComparator(element, parent);
	}

	private final TypeLikeComparator myComparator;
	@Nullable
	private final CSharpReferenceExpressionEx myParent;

	private int myTypeArgumentsSize;

	@RequiredReadAction
	public StaticVsInstanceComparator(@Nonnull PsiElement element, @Nullable CSharpReferenceExpressionEx parent)
	{
		myComparator = TypeLikeComparator.create(element);
		myTypeArgumentsSize = CSharpReferenceExpressionImplUtil.getTypeArgumentListSize(element);
		myParent = parent;
	}

	@Override
	@RequiredReadAction
	public int compare(ResolveResult o1, ResolveResult o2)
	{
		if(isNameEqual(o1, o2))
		{
			int i = getWeightByContext(o2) - getWeightByContext(o1);
			if(i == 0)
			{
				return getWeightByTypeArguments(o2) - getWeightByTypeArguments(o1);
			}
			return i;
		}
		return myComparator.compare(o1, o2);
	}

	private static boolean isNameEqual(ResolveResult o1, ResolveResult o2)
	{
		PsiElement e1 = o1.getElement();
		PsiElement e2 = o2.getElement();
		if(e1 instanceof PsiNamedElement && e2 instanceof PsiNamedElement)
		{
			return Comparing.equal(((PsiNamedElement) e1).getName(), ((PsiNamedElement) e2).getName());
		}
		return false;
	}

	@RequiredReadAction
	private int getWeightByTypeArguments(ResolveResult o1)
	{
		PsiElement element = o1.getElement();
		if(element instanceof DotNetGenericParameterListOwner)
		{
			int genericParametersCount = ((DotNetGenericParameterListOwner) element).getGenericParametersCount();
			int weight = genericParametersCount * 10;
			if(genericParametersCount == myTypeArgumentsSize)
			{
				weight += 1000;
			}
			return weight;
		}
		return 0;
	}

	@RequiredReadAction
	private int getWeightByContext(ResolveResult resolveResult)
	{
		final PsiElement element = resolveResult.getElement();
		if(element == null)
		{
			return -100;
		}

		// type alias have max priority
		if(element instanceof CSharpTypeDefStatement)
		{
			return Integer.MAX_VALUE;
		}

		if(myParent != null)
		{
			CSharpContextUtil.ContextType parentContext = CSharpContextUtil.ContextType.ANY;
			if(element instanceof CSharpTypeDeclaration)
			{
				parentContext = CSharpContextUtil.ContextType.STATIC;
			}
			else if(element instanceof DotNetVariable)
			{
				parentContext = CSharpContextUtil.ContextType.INSTANCE;
			}

			DotNetTypeDeclaration forceTarget = resolveTargetElement(element, myParent);
			if(forceTarget == null)
			{
				return parentContext == CSharpContextUtil.ContextType.INSTANCE ? 10 : 5;
			}

			ResolveResult[] resolveResults = RecursionManager.doPreventingRecursion(myParent, false, () -> myParent.tryResolveFromQualifier(forceTarget));
			if(resolveResults == null || resolveResults.length == 0)
			{
				return parentContext == CSharpContextUtil.ContextType.INSTANCE ? 10 : 5;
			}

			for(ResolveResult result : resolveResults)
			{
				PsiElement element1 = result.getElement();
				if(element1 == null)
				{
					continue;
				}

				CSharpContextUtil.ContextType contextForResolved = CSharpContextUtil.getContextForResolved(element1);

				if(parentContext != CSharpContextUtil.ContextType.ANY)
				{
					switch(parentContext)
					{
						case INSTANCE:
							if(contextForResolved.isAllowInstance())
							{
								return 5000;
							}
							break;
						case STATIC:
							if(contextForResolved == CSharpContextUtil.ContextType.STATIC)
							{
								return 5000;
							}
							break;
					}
				}
			}
		}
		else
		{
			// if expression is single - types and namespaces are in the end of queue
			if(element instanceof CSharpTypeDeclaration || element instanceof DotNetNamespaceAsElement)
			{
				return -1;
			}
		}
		return 0;
	}

	@Nullable
	@RequiredReadAction
	private static DotNetTypeDeclaration resolveTargetElement(@Nonnull PsiElement element, @Nonnull PsiElement scope)
	{
		if(element instanceof CSharpTypeDeclaration)
		{
			return (DotNetTypeDeclaration) element;
		}
		else if(element instanceof DotNetVariable)
		{
			DotNetTypeRef typeRef = ((DotNetVariable) element).toTypeRef(true);
			PsiElement resolvedElement = typeRef.resolve().getElement();
			if(resolvedElement == null)
			{
				return null;
			}
			return resolveTargetElement(resolvedElement, scope);
		}
		return null;
	}
}
