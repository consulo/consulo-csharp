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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.highlight.CSharpHighlightContext;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpAwaitExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpCatchStatementImpl;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * @author VISTALL
 * @since 02.01.15
 */
public class CS1985 extends CompilerCheck<CSharpAwaitExpressionImpl>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpHighlightContext highlightContext, @NotNull CSharpAwaitExpressionImpl element)
	{
		if(languageVersion.isAtLeast(CSharpLanguageVersion._6_0))
		{
			return null;
		}
		CSharpCatchStatementImpl catchStatement = PsiTreeUtil.getParentOfType(element, CSharpCatchStatementImpl.class);
		if(catchStatement != null)
		{
			return newBuilder(element.getAwaitKeywordElement(), "await");
		}
		return null;
	}
}
