package org.mustbe.consulo.csharp.ide.debugger;

import java.util.LinkedHashSet;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import consulo.annotations.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpRecursiveElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpDelegateExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpLambdaExpressionImpl;
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
		final Set<PsiElement> lambdas = new LinkedHashSet<PsiElement>();
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
