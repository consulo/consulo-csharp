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

package org.mustbe.consulo.csharp.ide.highlight.check.impl;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpMethodCallExpressionImpl;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import com.intellij.psi.PsiElement;
import com.intellij.util.SmartList;

/**
 * @author VISTALL
 * @since 31.10.2015
 */
public class CC0004 extends CompilerCheck<CSharpMethodCallExpressionImpl>
{
	@RequiredReadAction
	@NotNull
	@Override
	public List<HighlightInfoFactory> check(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpMethodCallExpressionImpl element)
	{
		List<PsiElement> list = new SmartList<PsiElement>();
		if(CC0001.isCalleInsideCalle(element))
		{
			DotNetExpression callExpression = element.getCallExpression();
			list.add(callExpression);
		}
		else
		{
			DotNetExpression callExpression = element.getCallExpression();
			if(callExpression instanceof CSharpReferenceExpression)
			{
				if(((CSharpReferenceExpression) callExpression).isSoft())
				{
					return Collections.emptyList();
				}
				PsiElement referenceElement = ((CSharpReferenceExpression) callExpression).getReferenceElement();
				if(referenceElement != null)
				{
					list.add(referenceElement);
				}
			}
		}

		if(list.isEmpty())
		{
			list.add(element);
		}
		String text = element.getText();
		return CC0001.checkReference(element, list);
	}
}

