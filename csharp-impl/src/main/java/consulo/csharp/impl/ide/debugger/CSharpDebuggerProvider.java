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

import consulo.annotation.component.ExtensionImpl;
import consulo.application.ApplicationManager;
import consulo.csharp.impl.ide.debugger.expressionEvaluator.Evaluator;
import consulo.csharp.impl.ide.debugger.expressionEvaluator.ThisObjectEvaluator;
import consulo.csharp.lang.CSharpFileType;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.impl.psi.fragment.CSharpFragmentFactory;
import consulo.csharp.lang.impl.psi.fragment.CSharpFragmentFileImpl;
import consulo.csharp.lang.impl.psi.source.CSharpMethodCallExpressionImpl;
import consulo.dotnet.debugger.DotNetDebugContext;
import consulo.dotnet.debugger.DotNetDebuggerProvider;
import consulo.dotnet.debugger.impl.nodes.DotNetFieldOrPropertyValueNode;
import consulo.dotnet.debugger.impl.nodes.DotNetStructValueInfo;
import consulo.dotnet.debugger.proxy.*;
import consulo.dotnet.debugger.proxy.value.DotNetObjectValueProxy;
import consulo.dotnet.debugger.proxy.value.DotNetStructValueProxy;
import consulo.dotnet.debugger.proxy.value.DotNetValueProxy;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetReferenceExpression;
import consulo.execution.debug.XDebugSession;
import consulo.execution.debug.XSourcePosition;
import consulo.execution.debug.evaluation.XDebuggerEvaluator;
import consulo.execution.debug.frame.XNamedValue;
import consulo.language.Language;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.logging.Logger;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.util.lang.Pair;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author VISTALL
 * @since 10.04.14
 */
@ExtensionImpl
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
	@RequiredUIAccess
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
			return;
		}

		final PsiElement finalElementAt = elementAt;
		debuggerContext.invoke(() ->
		{
			try
			{
				CSharpEvaluateContext evaluateContext = new CSharpEvaluateContext(debuggerContext, frame, finalElementAt);

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
		});
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
			List<Evaluator> evaluators = ApplicationManager.getApplication().runReadAction((Supplier<List<Evaluator>>) () ->
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
					consumer.accept(new DotNetFieldOrPropertyValueNode(debuggerContext, fieldOrPropertyMirror, frame, (DotNetObjectValueProxy) thisObjectValue));
				}
				else if(thisObjectValue instanceof DotNetStructValueProxy && parent.equals(type))
				{
					DotNetStructValueProxy structValueMirror = (DotNetStructValueProxy) thisObjectValue;

					DotNetStructValueInfo valueInfo = new DotNetStructValueInfo(structValueMirror, null, fieldOrPropertyMirror, objectPair.getFirst());

					consumer.accept(new DotNetFieldOrPropertyValueNode(debuggerContext, fieldOrPropertyMirror, frame, null, valueInfo));
				}
				else
				{
					consumer.accept(new CSharpWatcherNode(debuggerContext, ApplicationManager.getApplication().runReadAction((Supplier<String>) referenceExpression::getText), frame, objectPair
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
