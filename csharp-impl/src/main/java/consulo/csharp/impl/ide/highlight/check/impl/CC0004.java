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

package consulo.csharp.impl.ide.highlight.check.impl;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.impl.ide.highlight.CSharpHighlightContext;
import consulo.csharp.impl.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.impl.psi.source.CSharpMethodCallExpressionImpl;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetExpression;
import consulo.language.psi.PsiElement;

import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author VISTALL
 * @since 31.10.2015
 */
public class CC0004 extends CompilerCheck<CSharpMethodCallExpressionImpl>
{
	@RequiredReadAction
	@Nonnull
	@Override
	public List<HighlightInfoFactory> check(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull CSharpMethodCallExpressionImpl element)
	{
		List<PsiElement> list = new ArrayList<>();
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

