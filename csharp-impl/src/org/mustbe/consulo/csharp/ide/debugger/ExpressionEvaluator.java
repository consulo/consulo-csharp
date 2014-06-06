/*
 * Copyright 2013-2014 must-be.org
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

package org.mustbe.consulo.csharp.ide.debugger;

import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpReferenceExpressionImpl;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.PsiElement;
import mono.debugger.LocalVariableMirror;
import mono.debugger.MethodMirror;
import mono.debugger.MethodParameterMirror;
import mono.debugger.StackFrameMirror;

/**
 * @author VISTALL
 * @since 06.06.14
 */
public class ExpressionEvaluator extends CSharpElementVisitor
{
	private final StackFrameMirror myMirror;

	private Object myTargetMirror;

	public ExpressionEvaluator(StackFrameMirror mirror)
	{
		myMirror = mirror;
	}

	@Override
	public void visitReferenceExpression(CSharpReferenceExpressionImpl expression)
	{
		PsiElement qualifier = expression.getQualifier();
		if(qualifier != null)
		{

		}
		else
		{
			String referenceName = expression.getReferenceName();

			MethodMirror methodMirror = myMirror.location().method();
			MethodParameterMirror[] parameters = methodMirror.parameters();
			for(MethodParameterMirror parameter : parameters)
			{
				if(Comparing.equal(parameter.name(), referenceName))
				{
					myTargetMirror = parameter;
					break;
				}
			}

			for(LocalVariableMirror localVariableMirror : methodMirror.locals())
			{
				if(Comparing.equal(localVariableMirror.name(), referenceName))
				{
					myTargetMirror = localVariableMirror;
					break;
				}
			}
		}
	}

	public Object getTargetMirror()
	{
		return myTargetMirror;
	}
}
