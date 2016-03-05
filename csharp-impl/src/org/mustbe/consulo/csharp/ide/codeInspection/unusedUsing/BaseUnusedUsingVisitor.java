/*
 * Copyright 2013-2016 must-be.org
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

package org.mustbe.consulo.csharp.ide.codeInspection.unusedUsing;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpAttribute;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpressionEx;
import org.mustbe.consulo.csharp.lang.psi.CSharpUsingListChild;
import org.mustbe.consulo.csharp.lang.psi.CSharpUsingNamespaceStatement;
import org.mustbe.consulo.csharp.lang.psi.CSharpUsingTypeStatement;
import org.mustbe.consulo.csharp.lang.psi.impl.DotNetTypes2;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpLinqExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.CSharpResolveResult;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.dotnet.resolve.DotNetNamespaceAsElement;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRefUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;

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
				if(DotNetTypeRefUtil.isVmQNameEqual(((CSharpUsingTypeStatement) key).getTypeRef(), expression, DotNetTypes2.System.Linq.Enumerable))
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
					if(namespaceAsElement != null && namespaceAsElement.findChildren(className, expression.getResolveScope(), DotNetNamespaceAsElement.ChildrenFilter.ONLY_ELEMENTS).length > 0)
					{
						putElement(key, expression);
						break;
					}
				}
			}
		}
	}

	@Override
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

	private void putState(@NotNull ResolveResult firstValidResult, @Nullable PsiElement element)
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

	protected boolean isProcessed(@NotNull CSharpUsingListChild element)
	{
		return false;
	}

	@NotNull
	protected abstract Collection<? extends CSharpUsingListChild> getStatements();

	protected abstract void putElement(CSharpUsingListChild child, PsiElement targetElement);
}
