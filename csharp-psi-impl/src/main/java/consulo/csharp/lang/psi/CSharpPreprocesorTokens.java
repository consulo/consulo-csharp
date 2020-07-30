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

import consulo.csharp.lang.CSharpPreprocessorLanguage;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 23.01.14
 */
public interface CSharpPreprocesorTokens extends TokenType
{
	IElementType MACRO_IF_KEYWORD = new IElementType("MACRO_IF_KEYWORD", CSharpPreprocessorLanguage.INSTANCE);

	IElementType PRAGMA_KEYWORD = new IElementType("PRAGMA_KEYWORD", CSharpPreprocessorLanguage.INSTANCE);

	IElementType MACRO_ELSE_KEYWORD = new IElementType("MACRO_IF_KEYWORD", CSharpPreprocessorLanguage.INSTANCE);

	IElementType MACRO_ELIF_KEYWORD = new IElementType("MACRO_ELIF_KEYWORD", CSharpPreprocessorLanguage.INSTANCE);

	IElementType MACRO_ENDIF_KEYWORD = new IElementType("MACRO_ENDIF_KEYWORD", CSharpPreprocessorLanguage.INSTANCE);

	IElementType MACRO_REGION_KEYWORD = new IElementType("MACRO_REGION_KEYWORD", CSharpPreprocessorLanguage.INSTANCE);

	IElementType MACRO_ENDREGION_KEYWORD = new IElementType("MACRO_ENDREGION_KEYWORD", CSharpPreprocessorLanguage.INSTANCE);

	IElementType MACRO_DEFINE_KEYWORD = new IElementType("MACRO_DEFINE_KEYWORD", CSharpPreprocessorLanguage.INSTANCE);

	IElementType MACRO_UNDEF_KEYWORD = new IElementType("MACRO_UNDEF_KEYWORD", CSharpPreprocessorLanguage.INSTANCE);

	IElementType WARNING_KEYWORD = new IElementType("WARNING_KEYWORD", CSharpPreprocessorLanguage.INSTANCE);

	IElementType NULLABLE_KEYWORD = new IElementType("NULLABLE_KEYWORD", CSharpPreprocessorLanguage.INSTANCE);

	IElementType ERROR_KEYWORD = new IElementType("ERROR_KEYWORD", CSharpPreprocessorLanguage.INSTANCE);

	IElementType ILLEGAL_KEYWORD = new IElementType("ILLEGAL_KEYWORD", CSharpPreprocessorLanguage.INSTANCE);

	IElementType MACRO_STOP = new IElementType("MACRO_STOP", CSharpPreprocessorLanguage.INSTANCE);

	IElementType IDENTIFIER = new IElementType("IDENTIFIER", CSharpPreprocessorLanguage.INSTANCE);

	IElementType OROR = new IElementType("OROR", CSharpPreprocessorLanguage.INSTANCE);

	IElementType ANDAND = new IElementType("ANDAND", CSharpPreprocessorLanguage.INSTANCE);

	IElementType LPAR = new IElementType("LPAR", CSharpPreprocessorLanguage.INSTANCE);

	IElementType RPAR = new IElementType("RPAR =", CSharpPreprocessorLanguage.INSTANCE);

	IElementType EXCL = new IElementType("EXCL", CSharpPreprocessorLanguage.INSTANCE);

	IElementType LINE_COMMENT = new IElementType("LINE_COMMENT", CSharpPreprocessorLanguage.INSTANCE);
}
