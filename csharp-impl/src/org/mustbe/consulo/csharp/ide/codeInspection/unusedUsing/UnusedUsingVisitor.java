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

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpUsingListChild;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.CSharpResolveResult;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;

/**
 * @author VISTALL
 * @since 05.03.2016
 */
public class UnusedUsingVisitor extends CSharpElementVisitor
{
	private Map<CSharpUsingListChild, Boolean> myUsingContext = new HashMap<CSharpUsingListChild, Boolean>();

	@Override
	public void visitUsingChild(@NotNull CSharpUsingListChild child)
	{
		if(myUsingContext.containsKey(child))
		{
			return;
		}
		myUsingContext.put(child, Boolean.FALSE);
	}

	@NotNull
	public Map<CSharpUsingListChild, Boolean> getUsingContext()
	{
		return myUsingContext;
	}

	@Override
	public void visitReferenceExpression(CSharpReferenceExpression expression)
	{
		super.visitReferenceExpression(expression);

		ResolveResult[] resolveResults = expression.multiResolve(false);

		ResolveResult firstValidResult = CSharpResolveUtil.findFirstValidResult(resolveResults);
		if(firstValidResult != null)
		{
			putState(firstValidResult);
		}
		else
		{
			for(ResolveResult resolveResult : resolveResults)
			{
				putState(resolveResult);
			}
		}
	}

	private void putState(@NotNull ResolveResult firstValidResult)
	{
		if(firstValidResult instanceof CSharpResolveResult)
		{
			PsiElement providerElement = ((CSharpResolveResult) firstValidResult).getProviderElement();
			if(providerElement instanceof CSharpUsingListChild)
			{
				CSharpUsingListChild child = (CSharpUsingListChild) providerElement;
				myUsingContext.put(child, Boolean.TRUE);
			}
		}
	}
}
