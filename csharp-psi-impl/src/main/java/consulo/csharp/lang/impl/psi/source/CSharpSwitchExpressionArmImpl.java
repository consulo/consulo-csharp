/*
 * Copyright 2013-2026 consulo.io
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

package consulo.csharp.lang.impl.psi.source;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpSoftTokens;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.dotnet.psi.DotNetExpression;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElement;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.resolve.ResolveState;
import consulo.language.psi.util.PsiTreeUtil;
import org.jspecify.annotations.Nullable;

/**
 * {@code pattern [when condition] => result} of a switch expression - the variables of the pattern are visible in the
 * condition and the result.
 *
 * @author VISTALL
 */
public class CSharpSwitchExpressionArmImpl extends CSharpElementImpl
{
	public CSharpSwitchExpressionArmImpl(IElementType elementType)
	{
		super(elementType);
	}

	/**
	 * @return a {@link CSharpPatternImpl} or, for a declaration pattern, a {@link CSharpCaseVariableImpl}
	 */
	@Nullable
	public PsiElement getPattern()
	{
		for(PsiElement child = getFirstChild(); child != null; child = child.getNextSibling())
		{
			if(child instanceof CSharpPatternImpl || child instanceof CSharpCaseVariableImpl)
			{
				return child;
			}
		}
		return null;
	}

	@Nullable
	@RequiredReadAction
	public DotNetExpression getWhenExpression()
	{
		return expressionAfter(CSharpSoftTokens.WHEN_KEYWORD);
	}

	@Nullable
	@RequiredReadAction
	public DotNetExpression getResultExpression()
	{
		return expressionAfter(CSharpTokens.DARROW);
	}

	@Nullable
	private DotNetExpression expressionAfter(IElementType token)
	{
		boolean after = false;
		for(PsiElement child = getFirstChild(); child != null; child = child.getNextSibling())
		{
			if(child.getNode().getElementType() == token)
			{
				after = true;
			}
			else if(after && child instanceof DotNetExpression expression)
			{
				return expression;
			}
		}
		return null;
	}

	@Override
	@RequiredReadAction
	public boolean processDeclarations(PsiScopeProcessor processor, ResolveState state, PsiElement lastParent, PsiElement place)
	{
		PsiElement pattern = getPattern();
		// the pattern declares them for the condition and the result, not for itself
		if(pattern != null && lastParent != pattern)
		{
			if(pattern instanceof CSharpCaseVariableImpl variable && !processor.execute(variable, state))
			{
				return false;
			}
			for(CSharpCaseVariableImpl variable : PsiTreeUtil.findChildrenOfType(pattern, CSharpCaseVariableImpl.class))
			{
				if(!processor.execute(variable, state))
				{
					return false;
				}
			}
		}
		return super.processDeclarations(processor, state, lastParent, place);
	}

	@Override
	public void accept(CSharpElementVisitor visitor)
	{
		visitor.visitElement(this);
	}
}
