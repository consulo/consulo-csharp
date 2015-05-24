/*
 * Copyright 2013-2014 must-be.org
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentList;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpArrayAccessExpressionImpl;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.util.SmartList;

/**
 * @author VISTALL
 * @since 19.11.14
 */
public class CC0003 extends CompilerCheck<CSharpArrayAccessExpressionImpl>
{
	@RequiredReadAction
	@NotNull
	@Override
	public List<HighlightInfoFactory> check(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpArrayAccessExpressionImpl expression)
	{
		PsiElement resolve = expression.resolveToCallable();
		if(resolve == null)
		{
			CSharpCallArgumentList parameterList = expression.getParameterList();
			if(parameterList == null)
			{
				return Collections.emptyList();
			}

			List<TextRange> list = new ArrayList<TextRange>();
			PsiElement temp = parameterList.getOpenElement();
			if(temp != null)
			{
				list.add(temp.getTextRange());
			}
			temp = parameterList.getCloseElement();
			if(temp != null)
			{
				list.add(temp.getTextRange());
			}

			List<HighlightInfoFactory> result = new SmartList<HighlightInfoFactory>();
			for(TextRange textRange : list)
			{
				result.add(newBuilder(textRange));
			}
			return result;
		}
		return Collections.emptyList();
	}
}
