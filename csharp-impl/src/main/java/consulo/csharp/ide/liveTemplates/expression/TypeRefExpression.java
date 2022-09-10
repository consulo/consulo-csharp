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

package consulo.csharp.ide.liveTemplates.expression;

import consulo.csharp.ide.completion.expected.ExpectedTypeInfo;
import consulo.csharp.lang.impl.psi.CSharpTypeRefPresentationUtil;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.editor.template.Expression;
import consulo.language.editor.template.ExpressionContext;
import consulo.language.editor.template.Result;
import consulo.language.editor.template.TextResult;
import consulo.language.psi.PsiElement;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 06.01.15
 */
public class TypeRefExpression extends Expression
{
	private List<String> myTypeRefText;

	public TypeRefExpression(DotNetTypeRef typeRef, PsiElement scope)
	{
		myTypeRefText = List.of(CSharpTypeRefPresentationUtil.buildShortText(typeRef));
	}

	public TypeRefExpression(List<ExpectedTypeInfo> expectedTypeInfos, PsiElement scope)
	{
		assert !expectedTypeInfos.isEmpty();
		myTypeRefText = new ArrayList<String>(expectedTypeInfos.size());
		for(ExpectedTypeInfo expectedTypeInfo : expectedTypeInfos)
		{
			myTypeRefText.add(CSharpTypeRefPresentationUtil.buildShortText(expectedTypeInfo.getTypeRef()));
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
