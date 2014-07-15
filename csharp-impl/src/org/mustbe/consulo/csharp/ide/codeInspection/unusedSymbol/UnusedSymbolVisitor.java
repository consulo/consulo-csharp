package org.mustbe.consulo.csharp.ide.codeInspection.unusedSymbol;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpLocalVariable;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpReferenceExpressionImpl;
import org.mustbe.consulo.dotnet.DotNetRunUtil;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;

/**
 * @author VISTALL
 * @since 20.05.14
 */
public class UnusedSymbolVisitor extends CSharpElementVisitor
{
	private Map<PsiNameIdentifierOwner, Boolean> myVariableStates = new HashMap<PsiNameIdentifierOwner, Boolean>();

	@Override
	public void visitLocalVariable(CSharpLocalVariable variable)
	{
		myVariableStates.put(variable, Boolean.FALSE);
	}

	@Override
	public void visitParameter(DotNetParameter parameter)
	{
		DotNetLikeMethodDeclaration method = parameter.getMethod();
		if(method instanceof CSharpMethodDeclaration)
		{
			if(((CSharpMethodDeclaration) method).isDelegate())
			{
				return;
			}

			if(DotNetRunUtil.isEntryPoint((CSharpMethodDeclaration) method))
			{
				return;
			}
		}
		myVariableStates.put(parameter, Boolean.FALSE);
	}

	@Override
	public void visitReferenceExpression(CSharpReferenceExpressionImpl expression)
	{
		PsiElement resolve = expression.resolve();
		if(!(resolve instanceof PsiNameIdentifierOwner))
		{
			return;
		}
		Boolean aBoolean = myVariableStates.get(resolve);
		if(aBoolean != null)
		{
			myVariableStates.put((PsiNameIdentifierOwner) resolve, Boolean.TRUE);
		}
	}

	@NotNull
	public Collection<Map.Entry<PsiNameIdentifierOwner, Boolean>> getVariableStates()
	{
		return myVariableStates.entrySet();
	}
}
