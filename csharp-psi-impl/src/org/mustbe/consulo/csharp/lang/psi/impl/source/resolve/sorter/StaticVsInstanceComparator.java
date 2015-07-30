/*
 * Copyright 2013-2015 must-be.org
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

package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.sorter;

import java.util.Comparator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpContextUtil;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpressionEx;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import org.mustbe.consulo.dotnet.resolve.DotNetNamespaceAsElement;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.ResolveResult;

/**
 * @author VISTALL
 * @since 28.02.2015
 */
public class StaticVsInstanceComparator implements Comparator<ResolveResult>
{
	@NotNull
	public static StaticVsInstanceComparator create(@NotNull PsiElement element)
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
	private final CSharpReferenceExpressionEx myParent;

	public StaticVsInstanceComparator(PsiElement element, CSharpReferenceExpressionEx parent)
	{
		myComparator = TypeLikeComparator.create(element);
		myParent = parent;
	}

	@Override
	@RequiredReadAction
	public int compare(ResolveResult o1, ResolveResult o2)
	{
		if(isNameEqual(o1, o2))
		{
			return getWeight(o2) - getWeight(o1);
		}
		return myComparator.compare(o1, o2);
	}

	private boolean isNameEqual(ResolveResult o1, ResolveResult o2)
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
	private int getWeight(ResolveResult resolveResult)
	{
		final PsiElement element = resolveResult.getElement();
		if(element == null)
		{
			return -100;
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
				return 0;
			}

			// region Some code
			ResolveResult[] resolveResults = myParent.tryResolveFromQualifier(forceTarget);
			if(resolveResults.length == 0)
			{
				return 0;
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
			// endregion
		}
		else
		{
			// if expression is single - types and namespaces are in the end of queue
			if(element instanceof CSharpTypeDeclaration || element instanceof DotNetNamespaceAsElement)
			{
				return -1;
			}
		}
		return myComparator.getWeight(resolveResult);
	}

	@Nullable
	@RequiredReadAction
	private static DotNetTypeDeclaration resolveTargetElement(@NotNull PsiElement element, @NotNull PsiElement scope)
	{
		if(element instanceof CSharpTypeDeclaration)
		{
			return (DotNetTypeDeclaration) element;
		}
		else if(element instanceof DotNetVariable)
		{
			DotNetTypeRef typeRef = ((DotNetVariable) element).toTypeRef(true);
			PsiElement resolvedElement = typeRef.resolve(scope).getElement();
			if(resolvedElement == null)
			{
				return null;
			}
			return resolveTargetElement(resolvedElement, scope);
		}
		return null;
	}
}
