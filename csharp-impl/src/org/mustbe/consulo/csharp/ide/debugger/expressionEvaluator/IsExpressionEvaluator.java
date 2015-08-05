/*
 * Copyright 2013-2015 must-be.org
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

package org.mustbe.consulo.csharp.ide.debugger.expressionEvaluator;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.ide.debugger.CSharpEvaluateContext;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpIsExpressionImpl;
import org.mustbe.consulo.dotnet.debugger.DotNetDebugContext;
import org.mustbe.consulo.dotnet.debugger.TypeMirrorUnloadedException;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.psi.PsiElement;
import mono.debugger.BooleanValueMirror;
import mono.debugger.TypeMirror;
import mono.debugger.Value;

/**
 * @author VISTALL
 * @since 05.08.2015
 */
public class IsExpressionEvaluator implements Evaluator
{
	private CSharpIsExpressionImpl myExpression;

	public IsExpressionEvaluator(CSharpIsExpressionImpl expression)
	{
		myExpression = expression;
	}

	@Override
	public void evaluate(@NotNull CSharpEvaluateContext context)
	{
		Value<?> pop = context.pop();

		if(pop == null)
		{
			return;
		}

		TypeMirror type = pop.type();

		DotNetTypeRef typeRef = myExpression.getIsTypeRef();
		PsiElement element = typeRef.resolve(context.getElementAt()).getElement();
		if(element instanceof CSharpTypeDeclaration)
		{
			if(type == null)
			{
				return;
			}

			try
			{
				DotNetDebugContext debuggerContext = context.getDebuggerContext();
				TypeMirror typeMirror = debuggerContext.getVirtualMachine().findTypeMirror(null,
						((CSharpTypeDeclaration) element).getVmQName());

				if(typeMirror == null)
				{
					return;
				}
				context.pull(new BooleanValueMirror(debuggerContext.getVirtualMachine().getDelegate(),
						typeMirror.isAssignableFrom(type)));
			}
			catch(TypeMirrorUnloadedException e)
			{
				e.printStackTrace();
			}

		}
	}
}
