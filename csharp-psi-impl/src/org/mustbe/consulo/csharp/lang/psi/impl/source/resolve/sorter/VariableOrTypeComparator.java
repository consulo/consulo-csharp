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
import org.mustbe.consulo.csharp.lang.psi.CSharpContextUtil;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpressionEx;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.ResolveResult;

/**
 * @author VISTALL
 * @since 28.02.2015
 */
public class VariableOrTypeComparator implements Comparator<ResolveResult>
{
	@NotNull
	public static VariableOrTypeComparator create(@NotNull PsiElement element)
	{
		CSharpReferenceExpressionEx parent = null;
		if(element instanceof CSharpReferenceExpression)
		{
			if(((CSharpReferenceExpression) element).getQualifier() == null && element.getParent() instanceof CSharpReferenceExpressionEx)
			{
				parent = (CSharpReferenceExpressionEx) element.getParent();
			}
		}
		return new VariableOrTypeComparator(element, parent);
	}

	private final TypeLikeComparator myComparator;
	private final CSharpReferenceExpressionEx myParent;

	public VariableOrTypeComparator(PsiElement element, CSharpReferenceExpressionEx parent)
	{
		myComparator = TypeLikeComparator.create(element);
		myParent = parent;
	}

	@Override
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

			ResolveResult[] resolveResults = myParent.tryResolveFromQualifier(element);
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
		}
		return myComparator.getWeight(resolveResult);
	}
}
