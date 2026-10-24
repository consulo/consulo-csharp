/*
 * Copyright 2013-2019 consulo.io
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

import consulo.language.ast.IElementType;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.dotnet.psi.DotNetType;
import consulo.language.psi.PsiElement;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.DotNetExpression;
import consulo.csharp.lang.impl.psi.CSharpElements;

import org.jspecify.annotations.Nullable;

/**
 * @author VISTALL
 * @since 2019-10-05
 */
public class CSharpCaseVariableImpl extends CSharpVariableImpl
{
	public CSharpCaseVariableImpl(IElementType elementType)
	{
		super(elementType);
	}

	@Override
	public void accept(CSharpElementVisitor visitor)
	{
		visitor.visitCaseVariable(this);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public DotNetType getType()
	{
		return findNotNullChildByClass(DotNetType.class);
	}

	/**
	 * {@code var v} of a switch expression arm is of the type of the value matched
	 */
	@RequiredReadAction
	@Override
	public DotNetTypeRef toTypeRefImpl(boolean resolveFromInitializer)
	{
		DotNetType type = getType();
		DotNetTypeRef typeRef = type == null ? DotNetTypeRef.ERROR_TYPE : type.toTypeRef();
		if(typeRef != DotNetTypeRef.AUTO_TYPE)
		{
			return typeRef;
		}

		PsiElement current = this;
		while(current.getParent() instanceof CSharpPatternImpl pattern && pattern.getNode().getElementType() != CSharpElements.PROPERTY_PATTERN)
		{
			current = pattern;
		}
		if(current.getParent() instanceof CSharpSwitchExpressionArmImpl arm && arm.getParent() instanceof CSharpSwitchExpressionImpl switchExpression)
		{
			DotNetExpression value = switchExpression.getExpression();
			if(value != null)
			{
				return value.toTypeRef(resolveFromInitializer);
			}
		}
		return DotNetTypeRef.ERROR_TYPE;
	}
}
