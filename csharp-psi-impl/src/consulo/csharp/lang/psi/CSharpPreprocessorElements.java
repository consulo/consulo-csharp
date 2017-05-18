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

import com.intellij.lang.Language;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.ILazyParseableElementType;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.CSharpPreprocessorLanguage;
import consulo.csharp.lang.psi.impl.source.*;
import consulo.psi.tree.ElementTypeAsPsiFactory;

/**
 * @author VISTALL
 * @since 24.01.14
 */
public interface CSharpPreprocessorElements
{
	IElementType PREPROCESSOR_DIRECTIVE = new ILazyParseableElementType("PREPROCESSOR_DIRECTIVE", CSharpLanguage.INSTANCE)
	{
		@Override
		protected Language getLanguageForParser(PsiElement psi)
		{
			return CSharpPreprocessorLanguage.INSTANCE;
		}
	};

	IElementType MACRO_DEFINE = new ElementTypeAsPsiFactory("MACRO_DEFINE", CSharpPreprocessorLanguage.INSTANCE, CSharpMacroDefineImpl.class);

	IElementType MACRO_UNDEF = new ElementTypeAsPsiFactory("MACRO_UNDEF", CSharpPreprocessorLanguage.INSTANCE, CSharpMacroDefineImpl.class);

	IElementType MACRO_IF = new ElementTypeAsPsiFactory("MACRO_IF", CSharpPreprocessorLanguage.INSTANCE, CSharpMacroIfImpl.class);

	IElementType MACRO_IF_CONDITION_BLOCK = new ElementTypeAsPsiFactory("MACRO_IF_CONDITION_BLOCK", CSharpPreprocessorLanguage.INSTANCE, CSharpMacroIfConditionBlockImpl.class);

	IElementType MACRO_BLOCK_START = new ElementTypeAsPsiFactory("MACRO_BLOCK_START", CSharpPreprocessorLanguage.INSTANCE, CSharpMacroBlockStartImpl.class);

	IElementType MACRO_BLOCK = new ElementTypeAsPsiFactory("MACRO_BLOCK", CSharpPreprocessorLanguage.INSTANCE, CSharpMacroBlockImpl.class);

	IElementType MACRO_BLOCK_STOP = new ElementTypeAsPsiFactory("MACRO_BLOCK_STOP", CSharpPreprocessorLanguage.INSTANCE, CSharpMacroBlockStopImpl.class);

	IElementType PREFIX_EXPRESSION = new ElementTypeAsPsiFactory("PREFIX_EXPRESSION", CSharpPreprocessorLanguage.INSTANCE, CSharpMacroPrefixExpressionImpl.class);

	IElementType POLYADIC_EXPRESSION = new ElementTypeAsPsiFactory("POLYADIC_EXPRESSION", CSharpPreprocessorLanguage.INSTANCE, CSharpMacroPolyadicExpressionImpl.class);

	IElementType BINARY_EXPRESSION = new ElementTypeAsPsiFactory("BINARY_EXPRESSION", CSharpPreprocessorLanguage.INSTANCE, CSharpMacroBinaryExpressionImpl.class);

	IElementType REFERENCE_EXPRESSION = new ElementTypeAsPsiFactory("REFERENCE_EXPRESSION", CSharpPreprocessorLanguage.INSTANCE, CSharpMacroReferenceExpressionImpl.class);

	IElementType PARENTHESES_EXPRESSION = new ElementTypeAsPsiFactory("PARENTHESES_EXPRESSION", CSharpPreprocessorLanguage.INSTANCE, CSharpMacroParenthesesExpressionImpl.class);
}
