package org.mustbe.consulo.csharp.ide.findUsage.usageType;

import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.highlight.CSharpHighlightUtil;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import com.intellij.psi.PsiElement;
import com.intellij.usages.impl.rules.UsageType;
import com.intellij.usages.impl.rules.UsageTypeProvider;

/**
 * @author VISTALL
 * @since 01.11.14
 */
public class CSharpUsageTypeProvider implements UsageTypeProvider
{
	public static final UsageType AS_METHOD_REF = new UsageType("As method reference");
	public static final UsageType METHOD_CALL = new UsageType("Method call");

	@Nullable
	@Override
	public UsageType getUsageType(PsiElement element)
	{
		if(CSharpHighlightUtil.isMethodRef(element, null))
		{
			return AS_METHOD_REF;
		}

		if(element instanceof CSharpReferenceExpression && ((CSharpReferenceExpression) element).kind() == CSharpReferenceExpression.ResolveToKind
				.METHOD)
		{
			return METHOD_CALL;
		}
		return null;
	}
}
