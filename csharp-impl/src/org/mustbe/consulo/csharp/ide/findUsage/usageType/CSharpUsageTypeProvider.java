package org.mustbe.consulo.csharp.ide.findUsage.usageType;

import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpFieldDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpLocalVariable;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
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
		if(element instanceof CSharpReferenceExpression)
		{
			PsiElement resolve = ((CSharpReferenceExpression) element).resolve();
			if(resolve == null)
			{
				return null;
			}
			CSharpReferenceExpression.ResolveToKind kind = ((CSharpReferenceExpression) element).kind();
			switch(kind)
			{
				case METHOD:
					return METHOD_CALL;
				case CONSTRUCTOR:
					return UsageType.CLASS_NEW_OPERATOR;
				case TYPE_LIKE:
					DotNetType type = PsiTreeUtil.getParentOfType(element, DotNetType.class);
					if(type == null)
					{
						return null;
					}
					PsiElement parent = type.getParent();
					if(parent instanceof CSharpLocalVariable)
					{
						return UsageType.CLASS_LOCAL_VAR_DECLARATION;
					}
					else if(parent instanceof CSharpFieldDeclaration)
					{
						return UsageType.CLASS_FIELD_DECLARATION;
					}
					break;
				case ANY_MEMBER:
					if(element instanceof CSharpMethodDeclaration && !((CSharpMethodDeclaration) element).isDelegate())
					{
						return AS_METHOD_REF;
					}
					break;
			}
		}
		return null;
	}
}
