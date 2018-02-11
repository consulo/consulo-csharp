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

package consulo.csharp.ide.debugger;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.intellij.lang.Language;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.Consumer;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.XNamedValue;
import consulo.csharp.ide.debugger.expressionEvaluator.Evaluator;
import consulo.csharp.ide.debugger.expressionEvaluator.ThisObjectEvaluator;
import consulo.csharp.lang.CSharpFileType;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.psi.impl.fragment.CSharpFragmentFactory;
import consulo.csharp.lang.psi.impl.fragment.CSharpFragmentFileImpl;
import consulo.csharp.lang.psi.impl.source.CSharpMethodCallExpressionImpl;
import consulo.dotnet.debugger.DotNetDebugContext;
import consulo.dotnet.debugger.DotNetDebuggerProvider;
import consulo.dotnet.debugger.nodes.DotNetFieldOrPropertyValueNode;
import consulo.dotnet.debugger.nodes.DotNetStructValueInfo;
import consulo.dotnet.debugger.proxy.DotNetFieldOrPropertyProxy;
import consulo.dotnet.debugger.proxy.DotNetInvalidObjectException;
import consulo.dotnet.debugger.proxy.DotNetNotSuspendedException;
import consulo.dotnet.debugger.proxy.DotNetStackFrameProxy;
import consulo.dotnet.debugger.proxy.DotNetThrowValueException;
import consulo.dotnet.debugger.proxy.DotNetTypeProxy;
import consulo.dotnet.debugger.proxy.value.DotNetObjectValueProxy;
import consulo.dotnet.debugger.proxy.value.DotNetStructValueProxy;
import consulo.dotnet.debugger.proxy.value.DotNetValueProxy;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetReferenceExpression;

/**
 * @author VISTALL
 * @since 10.04.14
 */
public class CSharpDebuggerProvider extends DotNetDebuggerProvider
{
	private static final Logger LOGGER = Logger.getInstance(CSharpDebuggerProvider.class);

	@Nonnull
	@Override
	public PsiFile createExpressionCodeFragment(@Nonnull Project project, @Nonnull PsiElement sourcePosition, @Nonnull String text, boolean isPhysical)
	{
		return CSharpFragmentFactory.createExpressionFragment(project, text, sourcePosition);
	}

	@Override
	public void evaluate(@Nonnull DotNetStackFrameProxy frame,
			@Nonnull DotNetDebugContext debuggerContext,
			@Nonnull String expression,
			@Nullable PsiElement elementAt,
			@Nonnull XDebuggerEvaluator.XEvaluationCallback callback,
			@Nullable XSourcePosition sourcePosition)
	{
		if(elementAt == null)
		{
			XDebugSession session = debuggerContext.getSession();
			XSourcePosition currentPosition = session.getCurrentPosition();
			if(currentPosition == null)
			{
				callback.errorOccurred("cant evaluate");
				return;
			}

			VirtualFile file = currentPosition.getFile();
			PsiFile psiFile = PsiManager.getInstance(debuggerContext.getProject()).findFile(file);
			if(psiFile == null)
			{
				callback.errorOccurred("cant evaluate");
				return;
			}
			elementAt = psiFile.findElementAt(currentPosition.getOffset());
			if(elementAt == null)
			{
				callback.errorOccurred("cant evaluate");
				return;
			}
		}

		CSharpFragmentFileImpl expressionFragment = CSharpFragmentFactory.createExpressionFragment(debuggerContext.getProject(), expression, elementAt);

		PsiElement[] children = expressionFragment.getChildren();
		if(children.length == 0)
		{
			callback.errorOccurred("no expression");
			return;
		}

		PsiElement fragmentElement = children[0];

		DotNetExpression expressionPsi = fragmentElement instanceof DotNetExpression ? (DotNetExpression) fragmentElement : null;

		if(expressionPsi == null)
		{
			callback.errorOccurred("no expression");
			return;
		}

		CSharpExpressionEvaluator expressionEvaluator = new CSharpExpressionEvaluator();
		try
		{
			expressionPsi.accept(expressionEvaluator);

			CSharpEvaluateContext evaluateContext = new CSharpEvaluateContext(debuggerContext, frame, elementAt);

			List<Evaluator> evaluators = expressionEvaluator.getEvaluators();
			if(evaluators.isEmpty())
			{
				callback.errorOccurred("cant evaluate expression");
				return;
			}

			evaluateContext.evaluate(evaluators);
			DotNetValueProxy targetValue = evaluateContext.popValue();
			if(targetValue != null)
			{
				callback.evaluated(new CSharpWatcherNode(debuggerContext, expression, frame, targetValue));
			}
			else
			{
				callback.errorOccurred("no value");
			}
		}
		catch(DotNetThrowValueException e)
		{
			callback.errorOccurred(StringUtil.notNullize(e.getMessage(), "unknown exception"));
		}
		catch(DotNetNotSuspendedException e)
		{
			callback.errorOccurred("not suspended");
		}
		catch(DotNetInvalidObjectException e)
		{
			callback.errorOccurred("invalid object");
		}
		catch(Exception e)
		{
			String message = e.getMessage();
			if(message == null)
			{
				message = e.getClass().getSimpleName() + " was throw";
				CSharpDebuggerProvider.LOGGER.error("Exception have null message", e);
			}
			callback.errorOccurred(message);
		}
	}

	@Override
	public void evaluate(@Nonnull DotNetStackFrameProxy frame,
			@Nonnull DotNetDebugContext debuggerContext,
			@Nonnull DotNetReferenceExpression referenceExpression,
			@Nonnull Set<Object> visitedVariables,
			@Nonnull Consumer<XNamedValue> consumer)
	{
		try
		{
			List<Evaluator> evaluators = ApplicationManager.getApplication().runReadAction((Computable<List<Evaluator>>) () ->
			{
				PsiElement resolvedElement = referenceExpression.resolve();
				if(referenceExpression.getParent() instanceof CSharpMethodCallExpressionImpl || resolvedElement instanceof DotNetLikeMethodDeclaration)
				{
					return Collections.emptyList();
				}
				CSharpExpressionEvaluator expressionEvaluator = new CSharpExpressionEvaluator();
				referenceExpression.accept(expressionEvaluator);
				return expressionEvaluator.getEvaluators();
			});

			if(evaluators.isEmpty())
			{
				return;
			}

			CSharpEvaluateContext evaluateContext = new CSharpEvaluateContext(debuggerContext, frame, referenceExpression);
			evaluateContext.evaluate(evaluators);
			Pair<DotNetValueProxy, Object> objectPair = evaluateContext.pop();
			if(objectPair != null && objectPair.getSecond() instanceof DotNetFieldOrPropertyProxy)
			{
				DotNetFieldOrPropertyProxy fieldOrPropertyMirror = (DotNetFieldOrPropertyProxy) objectPair.getSecond();
				if(visitedVariables.contains(fieldOrPropertyMirror))
				{
					return;
				}

				visitedVariables.add(fieldOrPropertyMirror);

				DotNetTypeProxy parent = fieldOrPropertyMirror.getParentType();

				DotNetValueProxy thisObjectValue = ThisObjectEvaluator.calcThisObject(frame, frame.getThisObject());
				DotNetTypeProxy type = thisObjectValue.getType();
				if(thisObjectValue instanceof DotNetObjectValueProxy && parent.equals(type))
				{
					consumer.consume(new DotNetFieldOrPropertyValueNode(debuggerContext, fieldOrPropertyMirror, frame, (DotNetObjectValueProxy) thisObjectValue));
				}
				else if(thisObjectValue instanceof DotNetStructValueProxy && parent.equals(type))
				{
					DotNetStructValueProxy structValueMirror = (DotNetStructValueProxy) thisObjectValue;

					DotNetStructValueInfo valueInfo = new DotNetStructValueInfo(structValueMirror, null, fieldOrPropertyMirror, objectPair.getFirst());

					consumer.consume(new DotNetFieldOrPropertyValueNode(debuggerContext, fieldOrPropertyMirror, frame, null, valueInfo));
				}
				else
				{
					consumer.consume(new CSharpWatcherNode(debuggerContext, ApplicationManager.getApplication().runReadAction((Computable<String>) referenceExpression::getText), frame, objectPair
							.getFirst()));
				}
			}
		}
		catch(Exception e)
		{
			// ignored
		}
	}

	@Override
	public boolean isSupported(@Nonnull PsiFile psiFile)
	{
		return psiFile.getFileType() == CSharpFileType.INSTANCE;
	}

	@Override
	public Language getEditorLanguage()
	{
		return CSharpLanguage.INSTANCE;
	}
}
