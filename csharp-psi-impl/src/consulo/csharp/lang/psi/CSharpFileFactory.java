/*
 * Copyright 2013-2017 consulo.io
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

package consulo.csharp.lang.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilderFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.SingleRootFileViewProvider;
import com.intellij.psi.StubBasedPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightVirtualFile;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.CSharpFileType;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.parser.CSharpBuilderWrapper;
import consulo.csharp.lang.parser.ModifierSet;
import consulo.csharp.lang.parser.exp.ExpressionParsing;
import consulo.csharp.lang.parser.stmt.StatementParsing;
import consulo.csharp.lang.psi.impl.source.CSharpFileImpl;
import consulo.csharp.lang.psi.impl.source.CSharpFileWithScopeImpl;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetStatement;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.lang.LanguageVersion;

/**
 * @author VISTALL
 * @since 30.12.13.
 */
public class CSharpFileFactory
{
	@NotNull
	@RequiredReadAction
	public static CSharpFile createFile(@NotNull Project project, @NotNull CharSequence text)
	{
		return (CSharpFile) PsiFileFactory.getInstance(project).createFileFromText("dummy.cs", CSharpFileType.INSTANCE, text);
	}

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
	public static DotNetType createMaybeStubType(@NotNull Project project, @NotNull String typeText, @Nullable DotNetType oldType)
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
		DotNetNamedElement member = createMember(project, text);
		if(!(member instanceof DotNetLikeMethodDeclaration))
		{
			throw new IllegalArgumentException("member is not method, text: " + StringUtil.SINGLE_QUOTER.fun(text.toString()));
		}
		return (DotNetLikeMethodDeclaration) member;
	}

	@NotNull
	public static DotNetNamedElement createMember(@NotNull Project project, @NotNull CharSequence text)
	{
		String clazz = "class _Dummy { " + text + "; }";

		CSharpFileImpl psiFile = createTypeDeclarationWithScope(project, clazz);

		DotNetTypeDeclaration typeDeclaration = (DotNetTypeDeclaration) psiFile.getMembers()[0];
		return typeDeclaration.getMembers()[0];
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

	private static final IElementType ourExpressionElementType = new IFileElementType("expression-element-type", CSharpLanguage.INSTANCE)
	{
		@Override
		protected ASTNode doParseContents(@NotNull ASTNode chameleon, @NotNull PsiElement psi)
		{
			final Project project = psi.getProject();
			final Language languageForParser = getLanguageForParser(psi);
			final LanguageVersion tempLanguageVersion = chameleon.getUserData(LanguageVersion.KEY);
			final LanguageVersion languageVersion = tempLanguageVersion == null ? psi.getLanguageVersion() : tempLanguageVersion;
			final PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, null, languageForParser, languageVersion, chameleon.getChars());
			ExpressionParsing.parse(new CSharpBuilderWrapper(builder, languageVersion), ModifierSet.EMPTY);
			while(!builder.eof())
			{
				builder.advanceLexer();
			}
			return builder.getTreeBuilt();
		}
	};

	@RequiredReadAction
	@NotNull
	public static DotNetExpression createExpression(@NotNull Project project, @NotNull String text)
	{
		return parseFile(text, project, DotNetExpression.class, ourExpressionElementType);
	}

	private static final IElementType ourStatementElementType = new IFileElementType("statement-element-type", CSharpLanguage.INSTANCE)
	{
		@Override
		protected ASTNode doParseContents(@NotNull ASTNode chameleon, @NotNull PsiElement psi)
		{
			final Project project = psi.getProject();
			final Language languageForParser = getLanguageForParser(psi);
			final LanguageVersion tempLanguageVersion = chameleon.getUserData(LanguageVersion.KEY);
			final LanguageVersion languageVersion = tempLanguageVersion == null ? psi.getLanguageVersion() : tempLanguageVersion;
			final PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, null, languageForParser, languageVersion, chameleon.getChars());
			StatementParsing.parse(new CSharpBuilderWrapper(builder, languageVersion), ModifierSet.EMPTY);
			while(!builder.eof())
			{
				builder.advanceLexer();
			}
			return builder.getTreeBuilt();
		}
	};

	@RequiredReadAction
	@NotNull
	public static DotNetStatement createStatement(@NotNull Project project, @NotNull CharSequence text)
	{
		return parseFile(text, project, DotNetStatement.class, ourStatementElementType);
	}

	@RequiredReadAction
	private static <T extends PsiElement> T parseFile(CharSequence text, Project project, Class<T> clazz, IElementType elementType)
	{
		LightVirtualFile virtualFile = new LightVirtualFile("dummy.cs", CSharpFileType.INSTANCE, text, System.currentTimeMillis());
		SingleRootFileViewProvider viewProvider = new SingleRootFileViewProvider(PsiManager.getInstance(project), virtualFile, false);

		PsiFileBase file = new PsiFileBase(viewProvider, CSharpLanguage.INSTANCE)
		{
			{
				init(elementType, elementType);
			}
		};

		return PsiTreeUtil.findChildOfType(file, clazz);
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
		return new CSharpFileWithScopeImpl(viewProvider);
	}
}
