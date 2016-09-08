package org.mustbe.consulo.csharp.ide.findUsage.usageType;

import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpFieldDeclaration;
import consulo.csharp.lang.psi.CSharpLocalVariable;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpSimpleLikeMethodAsElement;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpAsExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpIsExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpTypeCastExpressionImpl;
import consulo.dotnet.psi.DotNetAttribute;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetType;
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
	public static final UsageType ATTRIBUTE = new UsageType("Attribute");
	public static final UsageType CLASS_IN_AS = new UsageType("Usage in 'as'");
	public static final UsageType CLASS_IN_IS = new UsageType("Usage in 'is'");

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
					if(element.getParent() instanceof DotNetAttribute)
					{
						return ATTRIBUTE;
					}
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
					else if(parent instanceof DotNetParameter)
					{
						return UsageType.CLASS_METHOD_PARAMETER_DECLARATION;
					}
					else if(parent instanceof CSharpSimpleLikeMethodAsElement)
					{
						return UsageType.CLASS_METHOD_RETURN_TYPE;
					}
					else if(parent instanceof CSharpTypeCastExpressionImpl)
					{
						return UsageType.CLASS_CAST_TO;
					}
					else if(parent instanceof CSharpAsExpressionImpl)
					{
						return CLASS_IN_AS;
					}
					else if(parent instanceof CSharpIsExpressionImpl)
					{
						return CLASS_IN_IS;
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
