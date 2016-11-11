package consulo.csharp.lang.psi.impl.source.resolve;

import org.jetbrains.annotations.NotNull;
import com.intellij.psi.PsiElement;
import consulo.csharp.lang.psi.*;
import consulo.csharp.lang.psi.impl.source.CSharpLabeledStatementImpl;
import consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetQualifiedElement;
import consulo.dotnet.resolve.DotNetNamespaceAsElement;
import consulo.lombok.annotations.ArrayFactoryFields;

/**
 * @author VISTALL
 * @since 09.10.14
 */
@ArrayFactoryFields
public enum ExecuteTarget
{
	LABEL
			{
				@Override
				public boolean isMyElement(@NotNull PsiElement element)
				{
					return element instanceof CSharpLabeledStatementImpl;
				}
			},
	TYPE
			{
				@Override
				public boolean isMyElement(@NotNull PsiElement element)
				{
					return element instanceof CSharpTypeDeclaration;
				}
			},
	MEMBER
			{
				@Override
				public boolean isMyElement(@NotNull PsiElement element)
				{
					return element instanceof DotNetQualifiedElement;
				}
			},
	DELEGATE_METHOD
			{
				@Override
				public boolean isMyElement(@NotNull PsiElement element)
				{
					return element instanceof CSharpMethodDeclaration && ((CSharpMethodDeclaration) element).isDelegate();
				}
			},
	TYPE_DEF
			{
				@Override
				public boolean isMyElement(@NotNull PsiElement element)
				{
					return element instanceof CSharpTypeDefStatement;
				}
			},
	NAMESPACE
			{
				@Override
				public boolean isMyElement(@NotNull PsiElement element)
				{
					return element instanceof DotNetNamespaceAsElement;
				}
			},
	GENERIC_PARAMETER
			{
				@Override
				public boolean isMyElement(@NotNull PsiElement element)
				{
					return element instanceof DotNetGenericParameter;
				}
			},
	FIELD
			{
				@Override
				public boolean isMyElement(@NotNull PsiElement element)
				{
					return element instanceof CSharpFieldDeclaration;
				}
			},
	PROPERTY
			{
				@Override
				public boolean isMyElement(@NotNull PsiElement element)
				{
					return element instanceof CSharpPropertyDeclaration;
				}
			},
	CONSTRUCTOR
			{
				@Override
				public boolean isMyElement(@NotNull PsiElement element)
				{
					return element instanceof CSharpConstructorDeclaration;
				}
			},
	EVENT
			{
				@Override
				public boolean isMyElement(@NotNull PsiElement element)
				{
					return element instanceof CSharpEventDeclaration;
				}
			},
	ELEMENT_GROUP
			{
				@Override
				public boolean isMyElement(@NotNull PsiElement element)
				{
					return element instanceof CSharpElementGroup;
				}
			},
	LOCAL_VARIABLE_OR_PARAMETER
			{
				@Override
				public boolean isMyElement(@NotNull PsiElement element)
				{
					return element instanceof CSharpLocalVariable ||
							element instanceof DotNetParameter ||
							element instanceof CSharpLinqVariable ||
							element instanceof CSharpLambdaParameter;
				}
			};

	public abstract boolean isMyElement(@NotNull PsiElement element);
}