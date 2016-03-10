/*
 * Copyright 2013-2014 must-be.org
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

package org.mustbe.consulo.csharp.lang.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.CSharpFileType;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpBlockStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpExpressionStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpFileImpl;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetStatement;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.SingleRootFileViewProvider;
import com.intellij.psi.StubBasedPsiElement;
import com.intellij.testFramework.LightVirtualFile;

/**
 * @author VISTALL
 * @since 30.12.13.
 */
public class CSharpFileFactory
{
	@NotNull
	@RequiredReadAction
	public static CSharpUsingListChild createUsingStatementFromText(@NotNull Project project, @NotNull String text)
	{
		CSharpFileImpl fileFromText = (CSharpFileImpl) PsiFileFactory.getInstance(project).createFileFromText("dummy.cs", CSharpFileType.INSTANCE, text);

		return fileFromText.getUsingStatements()[0];
	}

	@NotNull
	@RequiredReadAction
	public static CSharpUsingNamespaceStatement createUsingNamespaceStatement(@NotNull Project project, @NotNull String qName)
	{
		return (CSharpUsingNamespaceStatement) createUsingStatementFromText(project, "using " + qName + ";");
	}

	@NotNull
	public static DotNetType createStubType(@NotNull Project project, @NotNull String typeText, @Nullable DotNetType oldType)
	{
		if(oldType instanceof StubBasedPsiElement)
		{
			CSharpFieldDeclaration field = createField(project, typeText + " _dummy");
			return field.getType();
		}
		else
		{
			CSharpLocalVariableDeclarationStatement statement = (CSharpLocalVariableDeclarationStatement) createStatement(project, typeText + " i;");
			CSharpLocalVariable localVariable = statement.getVariables()[0];
			return localVariable.getType();
		}
	}

	@NotNull
	public static CSharpFieldDeclaration createField(@NotNull Project project, @NotNull String text)
	{
		String clazz = "class _Dummy { " + text + "; }";

		CSharpFileImpl psiFile = createTypeDeclarationWithScope(project, clazz);

		DotNetTypeDeclaration typeDeclaration = (DotNetTypeDeclaration) psiFile.getMembers()[0];
		return (CSharpFieldDeclaration) typeDeclaration.getMembers()[0];
	}

	@Nullable
	public static CSharpPropertyDeclaration createProperty(@NotNull Project project, @NotNull String text)
	{
		String clazz = "class _Dummy { " + text + "; }";

		CSharpFileImpl psiFile = createTypeDeclarationWithScope(project, clazz);

		DotNetTypeDeclaration typeDeclaration = (DotNetTypeDeclaration) psiFile.getMembers()[0];
		DotNetNamedElement namedElement = typeDeclaration.getMembers()[0];
		if(namedElement instanceof CSharpPropertyDeclaration)
		{
			return (CSharpPropertyDeclaration) namedElement;
		}
		return null;
	}

	@NotNull
	public static DotNetLikeMethodDeclaration createMethod(@NotNull Project project, @NotNull CharSequence text)
	{
		String clazz = "class _Dummy { " + text + "; }";

		CSharpFileImpl psiFile = createTypeDeclarationWithScope(project, clazz);

		DotNetTypeDeclaration typeDeclaration = (DotNetTypeDeclaration) psiFile.getMembers()[0];
		return (DotNetLikeMethodDeclaration) typeDeclaration.getMembers()[0];
	}

	@NotNull
	@RequiredReadAction
	public static CSharpIdentifier createIdentifier(@NotNull Project project, @NotNull String name)
	{
		CSharpFieldDeclaration field = createField(project, "int " + name);
		return (CSharpIdentifier) field.getNameIdentifier();
	}

	@NotNull
	@RequiredReadAction
	public static PsiElement createReferenceToken(@NotNull Project project, @NotNull String name)
	{
		CSharpFieldDeclaration field = createField(project, "int dummy = " + name + ";");
		CSharpReferenceExpression initializer = (CSharpReferenceExpression) field.getInitializer();
		assert initializer != null;
		PsiElement referenceElement = initializer.getReferenceElement();
		assert referenceElement != null;
		return referenceElement;
	}

	public static DotNetExpression createExpression(@NotNull Project project, @NotNull String text)
	{
		DotNetStatement statement = createStatement(project, text);
		assert statement instanceof CSharpExpressionStatementImpl;
		return ((CSharpExpressionStatementImpl) statement).getExpression();
	}

	public static DotNetStatement createStatement(@NotNull Project project, @NotNull CharSequence text)
	{
		String clazz = "class _Dummy { " +
				"void test() {" +
				text +
				"}" +
				" }";

		CSharpFileImpl psiFile = createTypeDeclarationWithScope(project, clazz);

		DotNetTypeDeclaration typeDeclaration = (DotNetTypeDeclaration) psiFile.getMembers()[0];
		CSharpMethodDeclaration dotNetNamedElement = (CSharpMethodDeclaration) typeDeclaration.getMembers()[0];
		return ((CSharpBlockStatementImpl) dotNetNamedElement.getCodeBlock()).getStatements()[0];
	}

	@NotNull
	@RequiredReadAction
	public static CSharpLocalVariable createLocalVariable(@NotNull Project project, @NotNull CharSequence text)
	{
		CSharpLocalVariableDeclarationStatement statement = (CSharpLocalVariableDeclarationStatement) createStatement(project, text);
		return statement.getVariables()[0];
	}

	@NotNull
	@RequiredReadAction
	public static DotNetType createType(@NotNull Project project, @NotNull CharSequence type)
	{
		CSharpLocalVariableDeclarationStatement statement = (CSharpLocalVariableDeclarationStatement) createStatement(project, type + " temp;");
		return statement.getVariables()[0].getType();
	}

	public static DotNetTypeDeclaration createTypeDeclaration(@NotNull Project project, @NotNull String text)
	{
		CSharpFileImpl psiFile = createTypeDeclarationWithScope(project, text);

		return (DotNetTypeDeclaration) psiFile.getMembers()[0];
	}

	private static CSharpFileImpl createTypeDeclarationWithScope(Project project, CharSequence text)
	{
		LightVirtualFile virtualFile = new LightVirtualFile("dummy.cs", CSharpFileType.INSTANCE, text, System.currentTimeMillis());
		SingleRootFileViewProvider viewProvider = new SingleRootFileViewProvider(PsiManager.getInstance(project), virtualFile, false);
		return new CSharpFileImpl(viewProvider);
	}
}
