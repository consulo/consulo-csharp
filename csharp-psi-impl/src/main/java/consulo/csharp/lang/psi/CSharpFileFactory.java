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

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilderFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightVirtualFile;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.CSharpFileType;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.parser.CSharpBuilderWrapper;
import consulo.csharp.lang.parser.ModifierSet;
import consulo.csharp.lang.parser.SharedParsingHelpers;
import consulo.csharp.lang.parser.exp.ExpressionParsing;
import consulo.csharp.lang.parser.stmt.StatementParsing;
import consulo.csharp.lang.psi.impl.source.CSharpFileImpl;
import consulo.csharp.lang.psi.impl.source.CSharpFileWithScopeImpl;
import consulo.dotnet.psi.*;
import consulo.lang.LanguageVersion;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiConsumer;

/**
 * @author VISTALL
 * @since 30.12.13.
 */
public class CSharpFileFactory
{
	@Nonnull
	@RequiredReadAction
	public static CSharpFile createFile(@Nonnull Project project, @Nonnull CharSequence text)
	{
		return (CSharpFile) PsiFileFactory.getInstance(project).createFileFromText("dummy.cs", CSharpFileType.INSTANCE, text);
	}

	@Nonnull
	@RequiredReadAction
	public static CSharpUsingListChild createUsingStatementFromText(@Nonnull Project project, @Nonnull String text)
	{
		CSharpFileImpl fileFromText = (CSharpFileImpl) PsiFileFactory.getInstance(project).createFileFromText("dummy.cs", CSharpFileType.INSTANCE, text);

		return fileFromText.getUsingStatements()[0];
	}

	@Nonnull
	@RequiredReadAction
	public static CSharpUsingNamespaceStatement createUsingNamespaceStatement(@Nonnull Project project, @Nonnull String qName)
	{
		return (CSharpUsingNamespaceStatement) createUsingStatementFromText(project, "using " + qName + ";");
	}

	@Nonnull
	public static DotNetType createMaybeStubType(@Nonnull Project project, @Nonnull String typeText, @Nullable DotNetType oldType)
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

	@Nonnull
	public static CSharpFieldDeclaration createField(@Nonnull Project project, @Nonnull String text)
	{
		String clazz = "class _Dummy { " + text + "; }";

		CSharpFileImpl psiFile = createTypeDeclarationWithScope(project, clazz);

		DotNetTypeDeclaration typeDeclaration = (DotNetTypeDeclaration) psiFile.getMembers()[0];
		return (CSharpFieldDeclaration) typeDeclaration.getMembers()[0];
	}

	@Nullable
	public static CSharpPropertyDeclaration createProperty(@Nonnull Project project, @Nonnull String text)
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

	@Nonnull
	public static DotNetLikeMethodDeclaration createMethod(@Nonnull Project project, @Nonnull CharSequence text)
	{
		DotNetNamedElement member = createMember(project, text);
		if(!(member instanceof DotNetLikeMethodDeclaration))
		{
			throw new IllegalArgumentException("member is not method, text: " + StringUtil.SINGLE_QUOTER.fun(text.toString()));
		}
		return (DotNetLikeMethodDeclaration) member;
	}

	@Nonnull
	public static DotNetNamedElement createMember(@Nonnull Project project, @Nonnull CharSequence text)
	{
		String clazz = "class _Dummy { " + text + "; }";

		CSharpFileImpl psiFile = createTypeDeclarationWithScope(project, clazz);

		DotNetTypeDeclaration typeDeclaration = (DotNetTypeDeclaration) psiFile.getMembers()[0];
		return typeDeclaration.getMembers()[0];
	}

	@Nonnull
	@RequiredReadAction
	public static CSharpIdentifier createIdentifier(@Nonnull Project project, @Nonnull String name)
	{
		CSharpFieldDeclaration field = createField(project, "int " + name);
		return (CSharpIdentifier) field.getNameIdentifier();
	}

	@Nonnull
	@RequiredReadAction
	public static PsiElement createReferenceToken(@Nonnull Project project, @Nonnull String name)
	{
		CSharpFieldDeclaration field = createField(project, "int dummy = " + name + ";");
		CSharpReferenceExpression initializer = (CSharpReferenceExpression) field.getInitializer();
		assert initializer != null;
		PsiElement referenceElement = initializer.getReferenceElement();
		assert referenceElement != null;
		return referenceElement;
	}

	private static final IElementType ourExpressionElementType = createElementType("expression-element-type", (builder, languageVersion) -> ExpressionParsing.parse(new CSharpBuilderWrapper(builder,
			languageVersion), ModifierSet.EMPTY));

	private static final IElementType ourStubExpressionElementType = createElementType("expression-element-type", (builder, languageVersion) -> ExpressionParsing.parse(new CSharpBuilderWrapper
			(builder, languageVersion), ModifierSet.EMPTY, SharedParsingHelpers.STUB_SUPPORT));

	private static final IElementType ourStatementElementType = createElementType("statement-element-type", (builder, languageVersion) -> StatementParsing.parse(new CSharpBuilderWrapper(builder,
			languageVersion), ModifierSet.EMPTY));

	@RequiredReadAction
	@Nonnull
	public static DotNetExpression createExpression(@Nonnull Project project, @Nonnull String text)
	{
		return parseFile(text, project, DotNetExpression.class, ourExpressionElementType);
	}

	@RequiredReadAction
	@Nonnull
	public static DotNetExpression createExpression(@Nonnull Project project, @Nonnull PsiElement stubMarker, @Nonnull String text)
	{
		IElementType refExpression = stubMarker instanceof StubBasedPsiElement ? ourStubExpressionElementType : ourExpressionElementType;
		return parseFile(text, project, DotNetExpression.class, refExpression);
	}

	@RequiredReadAction
	@Nonnull
	public static DotNetStatement createStatement(@Nonnull Project project, @Nonnull CharSequence text)
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

	@Nonnull
	private static IElementType createElementType(String id, BiConsumer<PsiBuilder, LanguageVersion> consumer)
	{
		return new IFileElementType(id, CSharpLanguage.INSTANCE)
		{
			@Override
			protected ASTNode doParseContents(@Nonnull ASTNode chameleon, @Nonnull PsiElement psi)
			{
				final Project project = psi.getProject();
				final Language languageForParser = getLanguageForParser(psi);
				final LanguageVersion tempLanguageVersion = chameleon.getUserData(LanguageVersion.KEY);
				final LanguageVersion languageVersion = tempLanguageVersion == null ? psi.getLanguageVersion() : tempLanguageVersion;
				final PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, null, languageForParser, languageVersion, chameleon.getChars());
				consumer.accept(builder, languageVersion);
				while(!builder.eof())
				{
					builder.advanceLexer();
				}
				return builder.getTreeBuilt();
			}
		};
	}

	@Nonnull
	@RequiredReadAction
	public static CSharpLocalVariable createLocalVariable(@Nonnull Project project, @Nonnull CharSequence text)
	{
		CSharpLocalVariableDeclarationStatement statement = (CSharpLocalVariableDeclarationStatement) createStatement(project, text);
		return statement.getVariables()[0];
	}

	@Nonnull
	@RequiredReadAction
	public static DotNetType createType(@Nonnull Project project, @Nonnull CharSequence type)
	{
		CSharpLocalVariableDeclarationStatement statement = (CSharpLocalVariableDeclarationStatement) createStatement(project, type + " temp;");
		return statement.getVariables()[0].getType();
	}

	public static DotNetTypeDeclaration createTypeDeclaration(@Nonnull Project project, @Nonnull String text)
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
