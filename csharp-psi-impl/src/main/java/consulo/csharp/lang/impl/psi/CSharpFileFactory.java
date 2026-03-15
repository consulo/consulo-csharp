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

package consulo.csharp.lang.impl.psi;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.CSharpFileType;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.impl.parser.CSharpBuilderWrapper;
import consulo.csharp.lang.impl.parser.ModifierSet;
import consulo.csharp.lang.impl.parser.SharedParsingHelpers;
import consulo.csharp.lang.impl.parser.exp.ExpressionParsing;
import consulo.csharp.lang.impl.parser.stmt.StatementParsing;
import consulo.csharp.lang.impl.psi.source.CSharpFileImpl;
import consulo.csharp.lang.impl.psi.source.CSharpFileWithScopeImpl;
import consulo.csharp.lang.psi.*;
import consulo.dotnet.psi.*;
import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.ast.IFileElementType;
import consulo.language.file.light.LightVirtualFile;
import consulo.language.impl.file.SingleRootFileViewProvider;
import consulo.language.impl.psi.PsiFileBase;
import consulo.language.parser.PsiBuilder;
import consulo.language.parser.PsiBuilderFactory;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFileFactory;
import consulo.language.psi.PsiManager;
import consulo.language.psi.StubBasedPsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.version.LanguageVersion;
import consulo.project.Project;
import consulo.util.lang.StringUtil;

import org.jspecify.annotations.Nullable;
import java.util.function.BiConsumer;

/**
 * @author VISTALL
 * @since 30.12.13.
 */
public class CSharpFileFactory
{
	@RequiredReadAction
	public static CSharpFile createFile(Project project, CharSequence text)
	{
		return (CSharpFile) PsiFileFactory.getInstance(project).createFileFromText("dummy.cs", CSharpFileType.INSTANCE, text);
	}

	@RequiredReadAction
	public static CSharpUsingListChild createUsingStatementFromText(Project project, String text)
	{
		CSharpFileImpl fileFromText = (CSharpFileImpl) PsiFileFactory.getInstance(project).createFileFromText("dummy.cs", CSharpFileType.INSTANCE, text);

		return fileFromText.getUsingStatements()[0];
	}

	@RequiredReadAction
	public static CSharpUsingNamespaceStatement createUsingNamespaceStatement(Project project, String qName)
	{
		return (CSharpUsingNamespaceStatement) createUsingStatementFromText(project, "using " + qName + ";");
	}

	public static DotNetType createMaybeStubType(Project project, String typeText, @Nullable DotNetType oldType)
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

	public static CSharpFieldDeclaration createField(Project project, String text)
	{
		String clazz = "class _Dummy { " + text + "; }";

		CSharpFileImpl psiFile = createTypeDeclarationWithScope(project, clazz);

		DotNetTypeDeclaration typeDeclaration = (DotNetTypeDeclaration) psiFile.getMembers()[0];
		return (CSharpFieldDeclaration) typeDeclaration.getMembers()[0];
	}

	@Nullable
	public static CSharpPropertyDeclaration createProperty(Project project, String text)
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

	public static DotNetLikeMethodDeclaration createMethod(Project project, CharSequence text)
	{
		DotNetNamedElement member = createMember(project, text);
		if(!(member instanceof DotNetLikeMethodDeclaration))
		{
			throw new IllegalArgumentException("member is not method, text: " + StringUtil.SINGLE_QUOTER.apply(text.toString()));
		}
		return (DotNetLikeMethodDeclaration) member;
	}

	public static DotNetNamedElement createMember(Project project, CharSequence text)
	{
		String clazz = "class _Dummy { " + text + "; }";

		CSharpFileImpl psiFile = createTypeDeclarationWithScope(project, clazz);

		DotNetTypeDeclaration typeDeclaration = (DotNetTypeDeclaration) psiFile.getMembers()[0];
		return typeDeclaration.getMembers()[0];
	}

	@RequiredReadAction
	public static CSharpIdentifier createIdentifier(Project project, String name)
	{
		CSharpFieldDeclaration field = createField(project, "int " + name);
		return (CSharpIdentifier) field.getNameIdentifier();
	}

	@RequiredReadAction
	public static PsiElement createReferenceToken(Project project, String name)
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
	public static DotNetExpression createExpression(Project project, String text)
	{
		return parseFile(text, project, DotNetExpression.class, ourExpressionElementType);
	}

	@RequiredReadAction
	public static DotNetExpression createExpression(Project project, PsiElement stubMarker, String text)
	{
		IElementType refExpression = stubMarker instanceof StubBasedPsiElement ? ourStubExpressionElementType : ourExpressionElementType;
		return parseFile(text, project, DotNetExpression.class, refExpression);
	}

	@RequiredReadAction
	public static DotNetStatement createStatement(Project project, CharSequence text)
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

	private static IElementType createElementType(String id, BiConsumer<PsiBuilder, LanguageVersion> consumer)
	{
		return new IFileElementType(id, CSharpLanguage.INSTANCE)
		{
			@Override
			protected ASTNode doParseContents(ASTNode chameleon, PsiElement psi)
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

	@RequiredReadAction
	public static CSharpLocalVariable createLocalVariable(Project project, CharSequence text)
	{
		CSharpLocalVariableDeclarationStatement statement = (CSharpLocalVariableDeclarationStatement) createStatement(project, text);
		return statement.getVariables()[0];
	}

	@RequiredReadAction
	public static DotNetType createType(Project project, CharSequence type)
	{
		CSharpLocalVariableDeclarationStatement statement = (CSharpLocalVariableDeclarationStatement) createStatement(project, type + " temp;");
		return statement.getVariables()[0].getType();
	}

	public static DotNetTypeDeclaration createTypeDeclaration(Project project, String text)
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
