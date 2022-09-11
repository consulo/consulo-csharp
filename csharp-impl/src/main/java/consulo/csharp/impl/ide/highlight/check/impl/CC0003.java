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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.impl.ide.highlight.CSharpHighlightContext;
import consulo.csharp.impl.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpCallArgumentList;
import consulo.csharp.lang.impl.psi.source.CSharpIndexAccessExpressionImpl;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetExpression;
import consulo.util.collection.ContainerUtil;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpDynamicTypeRef;
import consulo.language.psi.PsiElement;

/**
 * @author VISTALL
 * @since 19.11.14
 */
public class CC0003 extends CompilerCheck<CSharpIndexAccessExpressionImpl>
{
	@RequiredReadAction
	@Nonnull
	@Override
	public List<HighlightInfoFactory> check(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull CSharpIndexAccessExpressionImpl expression)
	{
		DotNetExpression qualifier = expression.getQualifier();
		if(qualifier.toTypeRef(false) instanceof CSharpDynamicTypeRef)
		{
			return Collections.emptyList();
		}
		List<PsiElement> ranges = new ArrayList<>(2);
		CSharpCallArgumentList parameterList = expression.getParameterList();
		if(parameterList != null)
		{
			ContainerUtil.addIfNotNull(ranges, parameterList.getOpenElement());
			ContainerUtil.addIfNotNull(ranges, parameterList.getCloseElement());
		}

		if(ranges.isEmpty())
		{
			return Collections.emptyList();
		}
		return CC0001.checkReference(expression, ranges);
	}
}
