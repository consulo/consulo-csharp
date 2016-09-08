package org.mustbe.consulo.csharp.ide.debugger;

import java.util.List;
import java.util.Set;

import consulo.lombok.annotations.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.debugger.expressionEvaluator.Evaluator;
import org.mustbe.consulo.csharp.ide.debugger.expressionEvaluator.ThisObjectEvaluator;
import org.mustbe.consulo.csharp.lang.CSharpFileType;
import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import org.mustbe.consulo.csharp.lang.psi.impl.fragment.CSharpFragmentFactory;
import org.mustbe.consulo.csharp.lang.psi.impl.fragment.CSharpFragmentFileImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpMethodCallExpressionImpl;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetReferenceExpression;
import com.intellij.lang.Language;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.Consumer;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.XNamedValue;
import consulo.dotnet.debugger.DotNetDebugContext;
import consulo.dotnet.debugger.DotNetDebuggerProvider;
import consulo.dotnet.debugger.nodes.DotNetFieldOrPropertyValueNode;
import consulo.dotnet.debugger.nodes.DotNetStructValueInfo;
import consulo.dotnet.debugger.proxy.DotNetFieldOrPropertyProxy;
import consulo.dotnet.debugger.proxy.DotNetInvalidObjectException;
import consulo.dotnet.debugger.proxy.DotNetStackFrameProxy;
import consulo.dotnet.debugger.proxy.DotNetTypeProxy;
import consulo.dotnet.debugger.proxy.value.DotNetObjectValueProxy;
import consulo.dotnet.debugger.proxy.value.DotNetStructValueProxy;
import consulo.dotnet.debugger.proxy.value.DotNetValueProxy;

/**
 * @author VISTALL
 * @since 10.04.14
 */
@Logger
public class CSharpDebuggerProvider extends DotNetDebuggerProvider
{
	@NotNull
	@Override
	public PsiFile createExpressionCodeFragment(@NotNull Project project, @NotNull PsiElement sourcePosition, @NotNull String text, boolean isPhysical)
	{
		return CSharpFragmentFactory.createExpressionFragment(project, text, sourcePosition);
	}

	@Override
	@RequiredReadAction
	public void evaluate(@NotNull DotNetStackFrameProxy frame,
			@NotNull DotNetDebugContext debuggerContext,
			@NotNull String expression,
			@Nullable PsiElement elementAt,
			@NotNull XDebuggerEvaluator.XEvaluationCallback callback,
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
				LOGGER.error("Exception have null message", e);
			}
			callback.errorOccurred(message);
		}
	}

	@Override
	@RequiredReadAction
	public void evaluate(@NotNull DotNetStackFrameProxy frame,
			@NotNull DotNetDebugContext debuggerContext,
			@NotNull DotNetReferenceExpression referenceExpression,
			@NotNull Set<Object> visitedVariables,
			@NotNull Consumer<XNamedValue> consumer)
	{
		PsiElement resolvedElement = referenceExpression.resolve();
		if(referenceExpression.getParent() instanceof CSharpMethodCallExpressionImpl || resolvedElement instanceof DotNetLikeMethodDeclaration)
		{
			return;
		}
		CSharpExpressionEvaluator expressionEvaluator = new CSharpExpressionEvaluator();
		try
		{
			referenceExpression.accept(expressionEvaluator);

			CSharpEvaluateContext evaluateContext = new CSharpEvaluateContext(debuggerContext, frame, referenceExpression);

			List<Evaluator> evaluators = expressionEvaluator.getEvaluators();
			if(evaluators.isEmpty())
			{
				return;
			}

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
					consumer.consume(new CSharpWatcherNode(debuggerContext, referenceExpression.getText(), frame, objectPair.getFirst()));
				}
			}
		}
		catch(Exception e)
		{
			// ignored
		}
	}

	@Override
	public boolean isSupported(@NotNull PsiFile psiFile)
	{
		return psiFile.getFileType() == CSharpFileType.INSTANCE;
	}

	@Override
	public Language getEditorLanguage()
	{
		return CSharpLanguage.INSTANCE;
	}
}
