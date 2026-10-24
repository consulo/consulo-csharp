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
import consulo.csharp.lang.impl.psi.CSharpTypeUtil;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpNullTypeRef;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.IElementType;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code value switch { pattern => result, ... }} (C# 8).
 *
 * @author VISTALL
 */
public class CSharpSwitchExpressionImpl extends CSharpExpressionImpl
{
	public CSharpSwitchExpressionImpl(IElementType elementType)
	{
		super(elementType);
	}

	/**
	 * @return the value which is matched
	 */
	@Nullable
	@RequiredReadAction
	public DotNetExpression getExpression()
	{
		return findChildByClass(DotNetExpression.class);
	}

	public CSharpSwitchExpressionArmImpl[] getArms()
	{
		return findChildrenByClass(CSharpSwitchExpressionArmImpl.class);
	}

	/**
	 * The natural type - the best common type of the results (C# spec 12.11); a throw and null take any.
	 */
	@RequiredReadAction
	@Override
	public DotNetTypeRef toTypeRefImpl(boolean resolveFromParent)
	{
		List<DotNetTypeRef> typeRefs = new ArrayList<>();
		boolean hasNull = false;
		for(CSharpSwitchExpressionArmImpl arm : getArms())
		{
			DotNetExpression result = arm.getResultExpression();
			if(result == null || result instanceof CSharpThrowExpressionImpl)
			{
				continue;
			}
			DotNetTypeRef typeRef = result.toTypeRef(resolveFromParent);
			if(typeRef instanceof CSharpNullTypeRef)
			{
				hasNull = true;
				continue;
			}
			if(typeRef != DotNetTypeRef.ERROR_TYPE && typeRef != DotNetTypeRef.AUTO_TYPE)
			{
				typeRefs.add(typeRef);
			}
		}

		for(DotNetTypeRef candidate : typeRefs)
		{
			boolean all = true;
			for(DotNetTypeRef other : typeRefs)
			{
				if(other != candidate && !CSharpTypeUtil.isInheritableWithImplicit(candidate, other, getResolveScope()))
				{
					all = false;
					break;
				}
			}
			if(all)
			{
				return candidate;
			}
		}
		if(!typeRefs.isEmpty())
		{
			return typeRefs.get(0);
		}
		return hasNull ? new CSharpNullTypeRef(getProject(), getResolveScope()) : DotNetTypeRef.ERROR_TYPE;
	}

	@Override
	public void accept(CSharpElementVisitor visitor)
	{
		visitor.visitElement(this);
	}
}
