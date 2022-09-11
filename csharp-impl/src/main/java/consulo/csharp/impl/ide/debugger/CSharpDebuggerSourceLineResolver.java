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

package consulo.csharp.impl.ide.debugger;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.impl.psi.CSharpRecursiveElementVisitor;
import consulo.csharp.lang.impl.psi.source.CSharpDelegateExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpLambdaExpressionImpl;
import consulo.dotnet.debugger.DotNetDebuggerSourceLineResolver;
import consulo.language.Language;
import consulo.language.psi.PsiElement;

import javax.annotation.Nonnull;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author VISTALL
 * @since 23.07.2015
 */
@ExtensionImpl
public class CSharpDebuggerSourceLineResolver implements DotNetDebuggerSourceLineResolver
{
	@RequiredReadAction
	@Nonnull
	@Override
	public Set<PsiElement> getAllExecutableChildren(@Nonnull PsiElement root)
	{
		final Set<PsiElement> lambdas = new LinkedHashSet<>();
		root.accept(new CSharpRecursiveElementVisitor()
		{
			@Override
			public void visitAnonymMethodExpression(CSharpDelegateExpressionImpl method)
			{
				lambdas.add(method);
			}

			@Override
			public void visitLambdaExpression(CSharpLambdaExpressionImpl expression)
			{
				lambdas.add(expression);
			}
		});
		return lambdas;
	}

	@Nonnull
	@Override
	public Language getLanguage()
	{
		return CSharpLanguage.INSTANCE;
	}
}
