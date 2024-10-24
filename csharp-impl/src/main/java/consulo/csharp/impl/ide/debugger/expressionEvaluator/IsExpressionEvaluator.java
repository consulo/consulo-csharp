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

package consulo.csharp.impl.ide.debugger.expressionEvaluator;

import consulo.application.ReadAction;
import consulo.csharp.impl.ide.debugger.CSharpEvaluateContext;
import consulo.csharp.lang.impl.psi.source.CSharpIsExpressionImpl;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.debugger.proxy.DotNetTypeProxy;
import consulo.dotnet.debugger.proxy.DotNetVirtualMachineProxy;
import consulo.dotnet.debugger.proxy.value.DotNetValueProxy;
import consulo.language.psi.PsiElement;
import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 05.08.2015
 */
public class IsExpressionEvaluator extends Evaluator
{
	private CSharpIsExpressionImpl myExpression;

	public IsExpressionEvaluator(CSharpIsExpressionImpl expression)
	{
		myExpression = expression;
	}

	@Override
	public void evaluate(@Nonnull CSharpEvaluateContext context)
	{
		DotNetValueProxy pop = context.popValue();

		if(pop == null)
		{
			return;
		}

		DotNetTypeProxy type = pop.getType();
		if(type == null)
		{
			return;
		}

		DotNetTypeRef typeRef = ReadAction.compute(() -> myExpression.getIsTypeRef());
		PsiElement element = ReadAction.compute(() -> typeRef.resolve().getElement());

		DotNetTypeProxy typeMirror = ReadAction.compute(() -> findTypeMirror(context, element));
		if(typeMirror == null)
		{
			return;
		}
		DotNetVirtualMachineProxy virtualMachine = context.getDebuggerContext().getVirtualMachine();
		context.pull(virtualMachine.createBooleanValue(typeMirror.isAssignableFrom(type)), null);
	}
}
