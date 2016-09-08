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

package org.mustbe.consulo.csharp.ide.highlight.check.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.highlight.CSharpHighlightContext;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpGotoStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpSwitchStatementImpl;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * @author VISTALL
 * @since 18-Jul-16
 */
public class CS0153 extends CompilerCheck<CSharpGotoStatementImpl>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpHighlightContext highlightContext, @NotNull CSharpGotoStatementImpl element)
	{
		if(element.isCaseOrDefault())
		{
			final CSharpSwitchStatementImpl statement = PsiTreeUtil.getParentOfType(element, CSharpSwitchStatementImpl.class);
			if(statement == null)
			{
				return newBuilder(element);
			}
		}
		return super.checkImpl(languageVersion, highlightContext, element);
	}
}
