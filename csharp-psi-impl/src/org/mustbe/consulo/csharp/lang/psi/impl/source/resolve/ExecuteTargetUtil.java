package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.KeyWithDefaultValue;
import com.intellij.psi.PsiElement;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.util.ArrayUtil;

/**
 * @author VISTALL
 * @since 09.10.14
 */
public class ExecuteTargetUtil
{
	public static final Key<ExecuteTarget[]> EXECUTE_TARGETS = new KeyWithDefaultValue<ExecuteTarget[]>("execute.targets")
	{
		@Override
		public ExecuteTarget[] getDefaultValue()
		{
			return ExecuteTarget.EMPTY_ARRAY;
		}
	};

	public static boolean isMyElement(@NotNull PsiScopeProcessor psiScopeProcessor, @NotNull PsiElement element)
	{
		ExecuteTarget[] hint = psiScopeProcessor.getHint(EXECUTE_TARGETS);
		if(hint == null)
		{
			return false;
		}
		for(ExecuteTarget executeTarget : hint)
		{
			if(executeTarget.isMyElement(element))
			{
				return true;
			}
		}
		return false;
	}

	public static boolean canProcess(@NotNull PsiScopeProcessor psiScopeProcessor, @NotNull ExecuteTarget target)
	{
		ExecuteTarget[] hint = psiScopeProcessor.getHint(EXECUTE_TARGETS);
		return hint != null && ArrayUtil.contains(target, hint);
	}
}
