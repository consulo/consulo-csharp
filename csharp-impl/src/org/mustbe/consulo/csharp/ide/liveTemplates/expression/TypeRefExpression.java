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

package org.mustbe.consulo.csharp.ide.liveTemplates.expression;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.completion.expected.ExpectedTypeInfo;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeRefPresentationUtil;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.template.Expression;
import com.intellij.codeInsight.template.ExpressionContext;
import com.intellij.codeInsight.template.Result;
import com.intellij.codeInsight.template.TextResult;
import com.intellij.psi.PsiElement;
import com.intellij.util.SmartList;

/**
 * @author VISTALL
 * @since 06.01.15
 */
public class TypeRefExpression extends Expression
{
	private List<String> myTypeRefText;

	public TypeRefExpression(DotNetTypeRef typeRef, PsiElement scope)
	{
		myTypeRefText = new SmartList<String>(CSharpTypeRefPresentationUtil.buildShortText(typeRef, scope));
	}

	public TypeRefExpression(List<ExpectedTypeInfo> expectedTypeInfos, PsiElement scope)
	{
		assert !expectedTypeInfos.isEmpty();
		myTypeRefText = new ArrayList<String>(expectedTypeInfos.size());
		for(ExpectedTypeInfo expectedTypeInfo : expectedTypeInfos)
		{
			myTypeRefText.add(CSharpTypeRefPresentationUtil.buildShortText(expectedTypeInfo.getTypeRef(), scope));
		}
	}

	@Nullable
	@Override
	public Result calculateResult(ExpressionContext context)
	{
		assert !myTypeRefText.isEmpty();
		return new TextResult(myTypeRefText.get(0));
	}

	@Nullable
	@Override
	public Result calculateQuickResult(ExpressionContext context)
	{
		assert !myTypeRefText.isEmpty();
		return new TextResult(myTypeRefText.get(0));
	}

	@Nullable
	@Override
	public LookupElement[] calculateLookupItems(ExpressionContext context)
	{
		return LookupElement.EMPTY_ARRAY;
	}
}
