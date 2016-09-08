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
import org.mustbe.consulo.csharp.ide.codeInsight.actions.RemoveModifierFix;
import org.mustbe.consulo.csharp.ide.codeInsight.actions.RemoveVariableInitializer;
import org.mustbe.consulo.csharp.ide.highlight.CSharpHighlightContext;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetParameter;

/**
 * @author VISTALL
 * @since 24.05.2015
 */
public class CS1741 extends CompilerCheck<DotNetParameter>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpHighlightContext highlightContext, @NotNull DotNetParameter element)
	{
		DotNetExpression initializer = element.getInitializer();
		if(initializer != null)
		{
			if(element.hasModifier(CSharpModifier.REF) || element.hasModifier(CSharpModifier.OUT))
			{
				boolean isRef = element.hasModifier(CSharpModifier.REF);
				CompilerCheckBuilder builder = newBuilder(initializer, isRef ? "ref" : "out");
				builder.addQuickFix(new RemoveVariableInitializer(element));
				builder.addQuickFix(new RemoveModifierFix(isRef ? CSharpModifier.REF : CSharpModifier.OUT, element));
				return builder;
			}
		} return super.checkImpl(languageVersion, highlightContext, element);
	}
}
