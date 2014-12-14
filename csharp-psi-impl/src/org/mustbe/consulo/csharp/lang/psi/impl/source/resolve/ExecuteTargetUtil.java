package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve;

import java.util.EnumSet;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.KeyWithDefaultValue;
import com.intellij.psi.PsiElement;
import com.intellij.psi.scope.PsiScopeProcessor;

/**
 * @author VISTALL
 * @since 09.10.14
 */
public class ExecuteTargetUtil
{
	public static final Key<EnumSet<ExecuteTarget>> EXECUTE_TARGETS = new KeyWithDefaultValue<EnumSet<ExecuteTarget>>("execute.targets")
	{
		@Override
		public EnumSet<ExecuteTarget> getDefaultValue()
		{
			return EnumSet.noneOf(ExecuteTarget.class);
		}
	};

	public static boolean isMyElement(@NotNull PsiScopeProcessor psiScopeProcessor, @NotNull PsiElement element)
	{
		EnumSet<ExecuteTarget> hint = psiScopeProcessor.getHint(EXECUTE_TARGETS);
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

	public static <E extends Enum<E>> EnumSet<E> of(E[] c)
	{
		if(c.length == 0)
		{
			throw new IllegalArgumentException("Collection is empty");
		}

		EnumSet<E> result = EnumSet.of(c[0]);
		if(c.length > 1)
		{
			for(int i = 1; i < c.length; i++)
			{
				result.add(c[i]);
			}
		}
		return result;
	}

	public static boolean canProcess(@NotNull PsiScopeProcessor psiScopeProcessor, @NotNull ExecuteTarget... executeTargets)
	{
		EnumSet<ExecuteTarget> hint = psiScopeProcessor.getHint(EXECUTE_TARGETS);
		if(hint == null)
		{
			return false;
		}

		for(ExecuteTarget executeTarget : executeTargets)
		{
			if(hint.contains(executeTarget))
			{
				return true;
			}
		}
		return false;
	}
}
