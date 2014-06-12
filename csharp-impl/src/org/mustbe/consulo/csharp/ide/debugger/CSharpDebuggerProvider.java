package org.mustbe.consulo.csharp.ide.debugger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.CSharpFileType;
import org.mustbe.consulo.csharp.lang.psi.impl.fragment.CSharpFragmentFactory;
import org.mustbe.consulo.dotnet.debugger.DotNetDebugContext;
import org.mustbe.consulo.dotnet.debugger.DotNetDebuggerProvider;
import org.mustbe.consulo.dotnet.debugger.nodes.DotNetLocalVariableMirrorNode;
import org.mustbe.consulo.dotnet.debugger.nodes.DotNetMethodParameterMirrorNode;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import lombok.val;
import mono.debugger.LocalVariableMirror;
import mono.debugger.MethodParameterMirror;
import mono.debugger.StackFrameMirror;

/**
 * @author VISTALL
 * @since 10.04.14
 */
public class CSharpDebuggerProvider extends DotNetDebuggerProvider
{
	@NotNull
	@Override
	public FileType getSupportedFileType()
	{
		return CSharpFileType.INSTANCE;
	}

	@NotNull
	@Override
	public PsiFile createExpressionCodeFragment(
			@NotNull Project project, @NotNull PsiElement sourcePosition, @NotNull String text, boolean isPhysical)
	{
		return CSharpFragmentFactory.createExpressionFragment(project, text, sourcePosition);
	}

	@Override
	public void evaluate(
			@NotNull StackFrameMirror frame,
			@NotNull DotNetDebugContext debuggerContext,
			@NotNull String expression,
			@Nullable PsiElement elementAt,
			@NotNull XDebuggerEvaluator.XEvaluationCallback callback)
	{
		val expressionFragment = CSharpFragmentFactory.createExpressionFragment(debuggerContext.getProject(), expression, elementAt);

		DotNetExpression expressionPsi = PsiTreeUtil.getChildOfType(expressionFragment, DotNetExpression.class);

		if(expressionPsi == null)
		{
			callback.evaluated(new ErrorValue("no expression"));
			return;
		}

		ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator(frame);
		expressionPsi.accept(expressionEvaluator);

		Object targetMirror = expressionEvaluator.getTargetMirror();
		if(targetMirror instanceof LocalVariableMirror)
		{
			callback.evaluated(new DotNetLocalVariableMirrorNode(debuggerContext, (LocalVariableMirror) targetMirror, frame));
		}
		else if(targetMirror instanceof MethodParameterMirror)
		{
			callback.evaluated(new DotNetMethodParameterMirrorNode(debuggerContext, (MethodParameterMirror) targetMirror, frame));
		}
		else
		{
			callback.evaluated(new ErrorValue("no value"));
		}
	}
}
