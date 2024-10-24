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

package consulo.csharp.lang.impl.ide.codeInspection.unusedUsing;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.*;
import consulo.csharp.lang.impl.psi.DotNetTypes2;
import consulo.csharp.lang.impl.psi.source.CSharpLinqExpressionImpl;
import consulo.csharp.lang.impl.psi.source.resolve.CSharpResolveResult;
import consulo.csharp.lang.impl.psi.source.resolve.util.CSharpResolveUtil;
import consulo.dotnet.psi.resolve.DotNetNamespaceAsElement;
import consulo.dotnet.psi.resolve.DotNetTypeRefUtil;
import consulo.language.psi.PsiElement;
import consulo.language.psi.ResolveResult;
import consulo.util.lang.StringUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Collection;

/**
 * @author VISTALL
 * @since 06.03.2016
 */
public abstract class BaseUnusedUsingVisitor extends CSharpElementVisitor
{
	@Override
	@RequiredReadAction
	public void visitLinqExpression(CSharpLinqExpressionImpl expression)
	{
		super.visitLinqExpression(expression);

		String packageOfEnumerable = StringUtil.getPackageName(DotNetTypes2.System.Linq.Enumerable);
		String className = StringUtil.getShortName(DotNetTypes2.System.Linq.Enumerable);

		Collection<? extends CSharpUsingListChild> statements = getStatements();
		for(CSharpUsingListChild key : statements)
		{
			if(isProcessed(key))
			{
				continue;
			}

			if(key instanceof CSharpUsingTypeStatement)
			{
				if(DotNetTypeRefUtil.isVmQNameEqual(((CSharpUsingTypeStatement) key).getTypeRef(), DotNetTypes2.System.Linq.Enumerable))
				{
					putElement(key, expression);
					break;
				}
			}
			else if(key instanceof CSharpUsingNamespaceStatement)
			{
				String referenceText = ((CSharpUsingNamespaceStatement) key).getReferenceText();

				// our namespace, try find class
				if(packageOfEnumerable.equals(referenceText))
				{
					DotNetNamespaceAsElement namespaceAsElement = ((CSharpUsingNamespaceStatement) key).resolve();
					if(namespaceAsElement != null && namespaceAsElement.findChildren(className, expression.getResolveScope(), DotNetNamespaceAsElement.ChildrenFilter.ONLY_ELEMENTS).size() > 0)
					{
						putElement(key, expression);
						break;
					}
				}
			}
		}
	}

	@Override
	@RequiredReadAction
	public void visitReferenceExpression(CSharpReferenceExpression expression)
	{
		super.visitReferenceExpression(expression);

		ResolveResult[] resolveResults;
		if(expression.kind() == CSharpReferenceExpression.ResolveToKind.CONSTRUCTOR)
		{
			CSharpReferenceExpression.ResolveToKind forceKind = CSharpReferenceExpression.ResolveToKind.TYPE_LIKE;
			if(expression.getParent() instanceof CSharpAttribute)
			{
				forceKind = CSharpReferenceExpression.ResolveToKind.ATTRIBUTE;
			}
			resolveResults = ((CSharpReferenceExpressionEx) expression).multiResolveImpl(forceKind, true);
		}
		else
		{
			resolveResults = expression.multiResolve(false);
		}

		ResolveResult firstValidResult = CSharpResolveUtil.findFirstValidResult(resolveResults);
		if(firstValidResult != null)
		{
			putState(firstValidResult, expression.getReferenceElement());
		}
		else
		{
			for(ResolveResult resolveResult : resolveResults)
			{
				putState(resolveResult, expression.getReferenceElement());
			}
		}
	}

	private void putState(@Nonnull ResolveResult firstValidResult, @Nullable PsiElement element)
	{
		if(element == null)
		{
			return;

		}
		if(firstValidResult instanceof CSharpResolveResult)
		{
			PsiElement providerElement = ((CSharpResolveResult) firstValidResult).getProviderElement();
			if(providerElement instanceof CSharpUsingListChild)
			{
				CSharpUsingListChild child = (CSharpUsingListChild) providerElement;

				putElement(child, element);
			}
		}
	}

	protected boolean isProcessed(@Nonnull CSharpUsingListChild element)
	{
		return false;
	}

	@Nonnull
	protected abstract Collection<? extends CSharpUsingListChild> getStatements();

	protected abstract void putElement(CSharpUsingListChild child, PsiElement targetElement);
}
