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

import com.intellij.lang.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.ILazyParseableElementType;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.CSharpPreprocessorLanguage;
import consulo.csharp.lang.lexer._CSharpMacroLexer;
import consulo.csharp.lang.psi.impl.CompositeElementTypeAsPsiFactory;
import consulo.csharp.lang.psi.impl.source.*;
import consulo.lang.LanguageVersion;
import org.jetbrains.annotations.NonNls;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 24.01.14
 */
public interface CSharpPreprocessorElements
{
	class DirectiveElementType extends ILazyParseableElementType
	{
		public DirectiveElementType(@Nonnull @NonNls String debugName)
		{
			super(debugName, CSharpLanguage.INSTANCE);
		}

		@Override
		protected ASTNode doParseContents(@Nonnull ASTNode chameleon, @Nonnull PsiElement psi)
		{
			final Project project = psi.getProject();
			final Language languageForParser = getLanguageForParser(psi);
			final LanguageVersion tempLanguageVersion = chameleon.getUserData(LanguageVersion.KEY);
			final LanguageVersion languageVersion = tempLanguageVersion == null ? psi.getLanguageVersion() : tempLanguageVersion;
			final PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, new _CSharpMacroLexer(), languageForParser, languageVersion, chameleon.getChars());
			final PsiParser parser = LanguageParserDefinitions.INSTANCE.forLanguage(languageForParser).createParser(languageVersion);
			return parser.parse(this, builder, languageVersion).getFirstChildNode();
		}

		@Override
		protected Language getLanguageForParser(PsiElement psi)
		{
			return CSharpPreprocessorLanguage.INSTANCE;
		}
	}

	IElementType PREPROCESSOR_DIRECTIVE = new DirectiveElementType("PREPROCESSOR_DIRECTIVE");

	IElementType DISABLED_PREPROCESSOR_DIRECTIVE = new DirectiveElementType("DISABLED_PREPROCESSOR_DIRECTIVE");

	IElementType MACRO_DEFINE = new CompositeElementTypeAsPsiFactory("MACRO_DEFINE", CSharpPreprocessorLanguage.INSTANCE, CSharpPreprocessorDefineImpl.class);

	IElementType MACRO_UNDEF = new CompositeElementTypeAsPsiFactory("MACRO_UNDEF", CSharpPreprocessorLanguage.INSTANCE, CSharpPreprocessorDefineImpl.class);

	IElementType MACRO_IF = new CompositeElementTypeAsPsiFactory("MACRO_IF", CSharpPreprocessorLanguage.INSTANCE, CSharpPreprocessorIfImpl.class);

	IElementType REGION_DIRECTIVE = new CompositeElementTypeAsPsiFactory("REGION_DIRECTIVE", CSharpPreprocessorLanguage.INSTANCE, CSharpPreprocessorRegionImpl.class);

	IElementType ENDREGION_DIRECTIVE = new CompositeElementTypeAsPsiFactory("ENDREGION_DIRECTIVE", CSharpPreprocessorLanguage.INSTANCE, CSharpPreprocessorEndRegionImpl.class);

	IElementType PRAGMA_DIRECTIVE = new CompositeElementTypeAsPsiFactory("PRAGMA_DIRECTIVE", CSharpPreprocessorLanguage.INSTANCE, CSharpPreprocessorPragmaImpl.class);

	IElementType NULLABLE_DIRECTIVE = new CompositeElementTypeAsPsiFactory("NULLABLE_DIRECTIVE", CSharpPreprocessorLanguage.INSTANCE, CSharpPreprocessorNullableImpl.class);

	IElementType WARNING_DIRECTIVE = new CompositeElementTypeAsPsiFactory("WARNING_DIRECTIVE", CSharpPreprocessorLanguage.INSTANCE, CSharpPreprocessorWarningImpl.class);

	IElementType ERROR_DIRECTIVE = new CompositeElementTypeAsPsiFactory("ERROR_DIRECTIVE", CSharpPreprocessorLanguage.INSTANCE, CSharpPreprocessorErrorImpl.class);

	IElementType MACRO_STOP_DIRECTIVE = new CompositeElementTypeAsPsiFactory("MACRO_STOP_DIRECTIVE", CSharpPreprocessorLanguage.INSTANCE, CSharpPreprocessorBlockStopImpl.class);

	IElementType PREFIX_EXPRESSION = new CompositeElementTypeAsPsiFactory("PREFIX_EXPRESSION", CSharpPreprocessorLanguage.INSTANCE, CSharpPreprocessorPrefixExpressionImpl.class);

	IElementType POLYADIC_EXPRESSION = new CompositeElementTypeAsPsiFactory("POLYADIC_EXPRESSION", CSharpPreprocessorLanguage.INSTANCE, CSharpPreprocessorPolyadicExpressionImpl.class);

	IElementType BINARY_EXPRESSION = new CompositeElementTypeAsPsiFactory("BINARY_EXPRESSION", CSharpPreprocessorLanguage.INSTANCE, CSharpPreprocessorBinaryExpressionImpl.class);

	IElementType REFERENCE_EXPRESSION = new CompositeElementTypeAsPsiFactory("REFERENCE_EXPRESSION", CSharpPreprocessorLanguage.INSTANCE, CSharpPreprocessorReferenceExpressionImpl.class);

	IElementType PARENTHESES_EXPRESSION = new CompositeElementTypeAsPsiFactory("PARENTHESES_EXPRESSION", CSharpPreprocessorLanguage.INSTANCE, CSharpPreprocessorParenthesesExpressionImpl.class);
}
