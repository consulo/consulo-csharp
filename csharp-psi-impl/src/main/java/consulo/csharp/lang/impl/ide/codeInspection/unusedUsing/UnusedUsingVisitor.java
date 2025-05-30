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
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpUsingListChild;
import consulo.csharp.lang.psi.CSharpUsingNamespaceStatement;
import consulo.csharp.lang.psi.CSharpUsingTypeStatement;
import consulo.csharp.lang.impl.psi.DotNetTypes2;
import consulo.csharp.lang.impl.psi.source.CSharpLinqExpressionImpl;
import consulo.dotnet.psi.resolve.DotNetNamespaceAsElement;
import consulo.dotnet.psi.resolve.DotNetTypeRefUtil;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiRecursiveElementVisitor;
import consulo.util.lang.StringUtil;

import jakarta.annotation.Nonnull;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author VISTALL
 * @since 05.03.2016
 */
public class UnusedUsingVisitor extends BaseUnusedUsingVisitor
{
	@Nonnull
	public static UnusedUsingVisitor accept(@Nonnull PsiFile file)
	{
		final UnusedUsingVisitor unusedUsingVisitor = new UnusedUsingVisitor();
		PsiRecursiveElementVisitor visitor = new PsiRecursiveElementVisitor()
		{
			@Override
			public void visitElement(PsiElement element)
			{
				element.accept(unusedUsingVisitor);
				super.visitElement(element);
			}
		};
		file.accept(visitor);
		return unusedUsingVisitor;
	}

	private Map<CSharpUsingListChild, Boolean> myUsingContext = new HashMap<CSharpUsingListChild, Boolean>();

	@Override
	@RequiredReadAction
	public void visitUsingChild(@Nonnull CSharpUsingListChild child)
	{
		if(myUsingContext.containsKey(child))
		{
			return;
		}

		Boolean defaultState = Boolean.FALSE;
		PsiElement referenceElement = child.getReferenceElement();
		if(referenceElement == null)
		{
			defaultState = Boolean.TRUE;
		}
		else if(referenceElement instanceof CSharpReferenceExpression)
		{
			defaultState = ((CSharpReferenceExpression) referenceElement).resolve() == null ? Boolean.TRUE : Boolean.FALSE;
		}
		myUsingContext.put(child, defaultState);
	}

	@Nonnull
	public Map<CSharpUsingListChild, Boolean> getUsingContext()
	{
		return myUsingContext;
	}

	@Override
	@RequiredReadAction
	public void visitLinqExpression(CSharpLinqExpressionImpl expression)
	{
		super.visitLinqExpression(expression);

		String packageOfEnumerable = StringUtil.getPackageName(DotNetTypes2.System.Linq.Enumerable);
		String className = StringUtil.getShortName(DotNetTypes2.System.Linq.Enumerable);

		for(Map.Entry<CSharpUsingListChild, Boolean> entry : myUsingContext.entrySet())
		{
			if(entry.getValue())
			{
				continue;
			}

			CSharpUsingListChild key = entry.getKey();
			if(key instanceof CSharpUsingTypeStatement)
			{
				if(DotNetTypeRefUtil.isVmQNameEqual(((CSharpUsingTypeStatement) key).getTypeRef(), DotNetTypes2.System.Linq.Enumerable))
				{
					myUsingContext.put(key, Boolean.TRUE);
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
						myUsingContext.put(key, Boolean.TRUE);
						break;
					}
				}
			}
		}
	}

	@Nonnull
	@Override
	protected Collection<? extends CSharpUsingListChild> getStatements()
	{
		return myUsingContext.keySet();
	}

	@Override
	protected boolean isProcessed(@Nonnull CSharpUsingListChild element)
	{
		return myUsingContext.get(element) == Boolean.TRUE;
	}

	@Override
	protected void putElement(CSharpUsingListChild child, PsiElement targetElement)
	{
		myUsingContext.put(child, Boolean.TRUE);
	}
}
