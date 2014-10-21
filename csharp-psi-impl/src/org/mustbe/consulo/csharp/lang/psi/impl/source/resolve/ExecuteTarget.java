package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve;

import org.consulo.lombok.annotations.ArrayFactoryFields;
import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpFieldDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpLambdaParameter;
import org.mustbe.consulo.csharp.lang.psi.CSharpLocalVariable;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpLabeledStatementImpl;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import org.mustbe.consulo.dotnet.resolve.DotNetNamespaceAsElement;
import com.intellij.psi.PsiElement;

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
	LOCAL_VARIABLE_OR_PARAMETER
			{
				@Override
				public boolean isMyElement(@NotNull PsiElement element)
				{
					return element instanceof CSharpLocalVariable || element instanceof DotNetParameter || element instanceof CSharpLambdaParameter;
				}
			};

	public abstract boolean isMyElement(@NotNull PsiElement element);
}
