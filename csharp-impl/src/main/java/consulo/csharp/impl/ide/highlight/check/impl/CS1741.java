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

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.impl.ide.codeInsight.actions.RemoveModifierFix;
import consulo.csharp.impl.ide.codeInsight.actions.RemoveVariableInitializer;
import consulo.csharp.impl.ide.highlight.CSharpHighlightContext;
import consulo.csharp.impl.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.module.extension.CSharpLanguageVersion;
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
	public HighlightInfoFactory checkImpl(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull DotNetParameter element)
	{
		DotNetExpression initializer = element.getInitializer();
		if(initializer != null)
		{
			if(element.hasModifier(CSharpModifier.REF) || element.hasModifier(CSharpModifier.OUT))
			{
				boolean isRef = element.hasModifier(CSharpModifier.REF);
				CompilerCheckBuilder builder = newBuilder(initializer, isRef ? "ref" : "out");
				builder.withQuickFix(new RemoveVariableInitializer(element));
				builder.withQuickFix(new RemoveModifierFix(isRef ? CSharpModifier.REF : CSharpModifier.OUT, element));
				return builder;
			}
		} return super.checkImpl(languageVersion, highlightContext, element);
	}
}
