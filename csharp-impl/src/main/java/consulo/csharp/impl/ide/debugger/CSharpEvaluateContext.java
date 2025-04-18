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

package consulo.csharp.impl.ide.debugger;

import consulo.util.lang.Pair;
import consulo.language.psi.PsiElement;
import consulo.csharp.impl.ide.debugger.expressionEvaluator.Evaluator;
import consulo.dotnet.debugger.DotNetDebugContext;
import consulo.dotnet.debugger.proxy.*;
import consulo.dotnet.debugger.proxy.value.DotNetValueProxy;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * @author VISTALL
 * @since 05.08.2015
 */
public class CSharpEvaluateContext
{
	private Deque<Pair<DotNetValueProxy, Object>> myStack = new ArrayDeque<>();
	private DotNetDebugContext myDebuggerContext;
	private DotNetStackFrameProxy myFrame;
	private PsiElement myElementAt;

	public CSharpEvaluateContext(DotNetDebugContext debuggerContext, DotNetStackFrameProxy frame, PsiElement elementAt)
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

	@Nonnull
	public DotNetStackFrameProxy getFrame()
	{
		return myFrame;
	}

	@Nullable
	public DotNetValueProxy popValue()
	{
		Pair<DotNetValueProxy, Object> pair = myStack.pollFirst();
		return pair == null ? null : pair.getFirst();
	}

	@Nullable
	public Pair<DotNetValueProxy, Object> pop()
	{
		return myStack.pollFirst();
	}

	public void pull(@Nonnull DotNetValueProxy o, @Nullable Object provider)
	{
		myStack.addFirst(Pair.<DotNetValueProxy, Object>create(o, provider));
	}

	public void evaluate(List<Evaluator> evaluators) throws DotNetThrowValueException, DotNetInvalidObjectException, DotNetInvalidStackFrameException, DotNetAbsentInformationException, DotNetNotSuspendedException
	{
		for(Evaluator evaluator : evaluators)
		{
			evaluator.evaluate(this);
		}
	}
}
