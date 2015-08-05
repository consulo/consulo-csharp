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

package org.mustbe.consulo.csharp.ide.debugger;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.debugger.expressionEvaluator.Evaluator;
import org.mustbe.consulo.dotnet.debugger.DotNetDebugContext;
import com.intellij.psi.PsiElement;
import mono.debugger.StackFrameMirror;
import mono.debugger.Value;

/**
 * @author VISTALL
 * @since 05.08.2015
 */
public class CSharpEvaluateContext
{
	private Deque<Value<?>> myStack = new ArrayDeque<Value<?>>();
	private DotNetDebugContext myDebuggerContext;
	private StackFrameMirror myFrame;
	private PsiElement myElementAt;

	public CSharpEvaluateContext(DotNetDebugContext debuggerContext, StackFrameMirror frame, PsiElement elementAt)
	{
		myDebuggerContext = debuggerContext;
		myFrame = frame;
		myElementAt = elementAt;
	}

	public PsiElement getElementAt()
	{
		return myElementAt;
	}

	public DotNetDebugContext getDebuggerContext()
	{
		return myDebuggerContext;
	}

	@NotNull
	public StackFrameMirror getFrame()
	{
		return myFrame;
	}

	@Nullable
	public Value<?> pop()
	{
		return myStack.poll();
	}

	public void pull(@NotNull Value<?> o)
	{
		myStack.addFirst(o);
	}

	public void evaluate(List<Evaluator> evaluators)
	{
		for(Evaluator evaluator : evaluators)
		{
			evaluator.evaluate(this);
		}
	}
}
