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

package consulo.csharp.ide.debugger;

import java.util.LinkedHashSet;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpRecursiveElementVisitor;
import consulo.csharp.lang.psi.impl.source.CSharpDelegateExpressionImpl;
import consulo.csharp.lang.psi.impl.source.CSharpLambdaExpressionImpl;
import com.intellij.psi.PsiElement;
import consulo.dotnet.debugger.DotNetDefaultDebuggerSourceLineResolver;

/**
 * @author VISTALL
 * @since 23.07.2015
 */
public class CSharpDebuggerSourceLineResolver extends DotNetDefaultDebuggerSourceLineResolver
{
	@RequiredReadAction
	@NotNull
	@Override
	public Set<PsiElement> getAllExecutableChildren(@NotNull PsiElement root)
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
}
