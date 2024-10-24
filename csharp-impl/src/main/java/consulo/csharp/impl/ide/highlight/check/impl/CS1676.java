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

import jakarta.annotation.Nullable;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.impl.ide.codeInsight.actions.AddModifierFix;
import consulo.csharp.impl.ide.highlight.CSharpHighlightContext;
import consulo.csharp.impl.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpLambdaParameter;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.impl.psi.source.CSharpLambdaExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpLambdaExpressionImplUtil;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpLambdaResolveResult;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.util.ArrayUtil2;
import consulo.language.psi.util.PsiTreeUtil;
import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 17-May-17
 */
public class CS1676 extends CompilerCheck<CSharpLambdaParameter>
{
	private static final CSharpModifier[] ourModifiers = {
			CSharpModifier.REF,
			CSharpModifier.OUT
	};

	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull CSharpLambdaParameter element)
	{
		CSharpLambdaExpressionImpl lambdaExpression = PsiTreeUtil.getParentOfType(element, CSharpLambdaExpressionImpl.class);
		if(lambdaExpression == null)
		{
			return null;
		}

		CSharpLambdaResolveResult lambdaResolveResult = CSharpLambdaExpressionImplUtil.resolveLeftLambdaTypeRef(lambdaExpression);
		if(lambdaResolveResult == null)
		{
			return null;
		}

		CSharpMethodDeclaration target = lambdaResolveResult.getTarget();
		if(target == null)
		{
			return null;
		}

		DotNetParameter[] parameters = target.getParameters();

		DotNetParameter realParameter = ArrayUtil2.safeGet(parameters, element.getIndex());
		if(realParameter == null)
		{
			return null;
		}

		for(CSharpModifier modifier : ourModifiers)
		{
			if(realParameter.hasModifier(modifier) && !element.hasModifier(modifier))
			{
				return newBuilder(element, String.valueOf(element.getIndex() + 1), modifier.getPresentableText()).withQuickFix(new AddModifierFix(modifier, element));
			}
		}


		return null;
	}
}
