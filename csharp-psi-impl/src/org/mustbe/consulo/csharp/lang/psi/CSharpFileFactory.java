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
import org.mustbe.consulo.csharp.lang.CSharpFileType;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpBlockStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpExpressionStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpFileImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpUsingListImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpUsingNamespaceStatementImpl;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetStatement;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.SingleRootFileViewProvider;
import com.intellij.testFramework.LightVirtualFile;
import lombok.val;

/**
 * @author VISTALL
 * @since 30.12.13.
 */
public class CSharpFileFactory
{
	public static CSharpUsingListImpl createUsingList(@NotNull Project project, @NotNull String qName)
	{
		val fileFromText = (CSharpFileImpl) PsiFileFactory.getInstance(project).createFileFromText("dummy.cs", CSharpFileType.INSTANCE,
				"using " + qName + ";");

		return (CSharpUsingListImpl) fileFromText.getFirstChild();
	}

	public static CSharpUsingListImpl createUsingListFromText(@NotNull Project project, @NotNull String text)
	{
		val fileFromText = (CSharpFileImpl) PsiFileFactory.getInstance(project).createFileFromText("dummy.cs", CSharpFileType.INSTANCE, text);

		return (CSharpUsingListImpl) fileFromText.getFirstChild();
	}

	public static CSharpUsingNamespaceStatementImpl createUsingStatement(@NotNull Project project, @NotNull String qName)
	{
		val fileFromText = (CSharpFileImpl) PsiFileFactory.getInstance(project).createFileFromText("dummy.cs", CSharpFileType.INSTANCE,
				"using " + qName + ";");

		CSharpUsingListImpl firstChild = (CSharpUsingListImpl) fileFromText.getFirstChild();
		return (CSharpUsingNamespaceStatementImpl) firstChild.getStatements()[0];
	}

	@NotNull
	public static DotNetType createType(@NotNull Project project, @NotNull String typeText)
	{
		CSharpFieldDeclaration field = createField(project, typeText + " _dummy");
		return field.getType();
	}

	@NotNull
	public static CSharpFieldDeclaration createField(@NotNull Project project, @NotNull String text)
	{
		val clazz = "class _Dummy { " + text + "; }";

		CSharpFileImpl psiFile = createTypeDeclarationWithScope(project, clazz);

		DotNetTypeDeclaration typeDeclaration = (DotNetTypeDeclaration) psiFile.getMembers()[0];
		return (CSharpFieldDeclaration) typeDeclaration.getMembers()[0];
	}

	@NotNull
	public static PsiElement createIdentifier(@NotNull Project project, @NotNull String name)
	{
		CSharpFieldDeclaration field = createField(project, "int " + name);
		return field.getNameIdentifier();
	}

	public static DotNetExpression createExpression(@NotNull Project project, @NotNull String text)
	{
		DotNetStatement statement = createStatement(project, text);
		assert statement instanceof CSharpExpressionStatementImpl;
		return ((CSharpExpressionStatementImpl) statement).getExpression();
	}

	public static DotNetStatement createStatement(@NotNull Project project, @NotNull String text)
	{
		val clazz = "class _Dummy { " +
				"void test() {" +
				text +
				"}" +
				" }";

		CSharpFileImpl psiFile = createTypeDeclarationWithScope(project, clazz);

		DotNetTypeDeclaration typeDeclaration = (DotNetTypeDeclaration) psiFile.getMembers()[0];
		CSharpMethodDeclaration dotNetNamedElement = (CSharpMethodDeclaration) typeDeclaration.getMembers()[0];
		return ((CSharpBlockStatementImpl) dotNetNamedElement.getCodeBlock()).getStatements()[0];
	}

	public static DotNetTypeDeclaration createTypeDeclaration(@NotNull Project project, @NotNull String text)
	{
		CSharpFileImpl psiFile = createTypeDeclarationWithScope(project, text);

		return (DotNetTypeDeclaration) psiFile.getMembers()[0];
	}

	private static CSharpFileImpl createTypeDeclarationWithScope(Project project, String text)
	{
		val virtualFile = new LightVirtualFile("dummy.cs", CSharpFileType.INSTANCE, text, System.currentTimeMillis());
		val viewProvider = new SingleRootFileViewProvider(PsiManager.getInstance(project), virtualFile, false);
		return new CSharpFileImpl(viewProvider);
	}
}
