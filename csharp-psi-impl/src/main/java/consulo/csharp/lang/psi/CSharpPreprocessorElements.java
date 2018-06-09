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

import javax.annotation.Nonnull;

import org.jetbrains.annotations.NonNls;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilderFactory;
import com.intellij.lang.PsiParser;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.ILazyParseableElementType;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.CSharpPreprocessorLanguage;
import consulo.csharp.lang.lexer._CSharpMacroLexer;
import consulo.csharp.lang.psi.impl.source.*;
import consulo.lang.LanguageVersion;
import consulo.psi.tree.ElementTypeAsPsiFactory;

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

	IElementType MACRO_DEFINE = new ElementTypeAsPsiFactory("MACRO_DEFINE", CSharpPreprocessorLanguage.INSTANCE, CSharpPreprocessorDefineImpl.class);

	IElementType MACRO_UNDEF = new ElementTypeAsPsiFactory("MACRO_UNDEF", CSharpPreprocessorLanguage.INSTANCE, CSharpPreprocessorDefineImpl.class);

	IElementType MACRO_IF = new ElementTypeAsPsiFactory("MACRO_IF", CSharpPreprocessorLanguage.INSTANCE, CSharpPreprocessorIfImpl.class);

	IElementType REGION_DIRECTIVE = new ElementTypeAsPsiFactory("REGION_DIRECTIVE", CSharpPreprocessorLanguage.INSTANCE, CSharpPreprocessorRegionImpl.class);

	IElementType ENDREGION_DIRECTIVE = new ElementTypeAsPsiFactory("ENDREGION_DIRECTIVE", CSharpPreprocessorLanguage.INSTANCE, CSharpPreprocessorEndRegionImpl.class);

	IElementType PRAGMA_DIRECTIVE = new ElementTypeAsPsiFactory("PRAGMA_DIRECTIVE", CSharpPreprocessorLanguage.INSTANCE, CSharpPreprocessorPragmaImpl.class);

	IElementType WARNING_DIRECTIVE = new ElementTypeAsPsiFactory("WARNING_DIRECTIVE", CSharpPreprocessorLanguage.INSTANCE, CSharpPreprocessorWarningImpl.class);

	IElementType ERROR_DIRECTIVE = new ElementTypeAsPsiFactory("ERROR_DIRECTIVE", CSharpPreprocessorLanguage.INSTANCE, CSharpPreprocessorErrorImpl.class);

	IElementType MACRO_STOP_DIRECTIVE = new ElementTypeAsPsiFactory("MACRO_STOP_DIRECTIVE", CSharpPreprocessorLanguage.INSTANCE, CSharpPreprocessorBlockStopImpl.class);

	IElementType PREFIX_EXPRESSION = new ElementTypeAsPsiFactory("PREFIX_EXPRESSION", CSharpPreprocessorLanguage.INSTANCE, CSharpPreprocessorPrefixExpressionImpl.class);

	IElementType POLYADIC_EXPRESSION = new ElementTypeAsPsiFactory("POLYADIC_EXPRESSION", CSharpPreprocessorLanguage.INSTANCE, CSharpPreprocessorPolyadicExpressionImpl.class);

	IElementType BINARY_EXPRESSION = new ElementTypeAsPsiFactory("BINARY_EXPRESSION", CSharpPreprocessorLanguage.INSTANCE, CSharpPreprocessorBinaryExpressionImpl.class);

	IElementType REFERENCE_EXPRESSION = new ElementTypeAsPsiFactory("REFERENCE_EXPRESSION", CSharpPreprocessorLanguage.INSTANCE, CSharpPreprocessorReferenceExpressionImpl.class);

	IElementType PARENTHESES_EXPRESSION = new ElementTypeAsPsiFactory("PARENTHESES_EXPRESSION", CSharpPreprocessorLanguage.INSTANCE, CSharpPreprocessorParenthesesExpressionImpl.class);
}
