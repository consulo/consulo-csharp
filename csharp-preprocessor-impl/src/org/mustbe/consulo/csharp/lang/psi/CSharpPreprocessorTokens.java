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

import org.mustbe.consulo.csharp.lang.CSharpPreprocessorLanguage;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

/**
 * @author VISTALL
 * @since 23.01.14
 */
public interface CSharpPreprocessorTokens extends TokenType
{
	IElementType IF_KEYWORD = new IElementType("IF_KEYWORD", CSharpPreprocessorLanguage.INSTANCE);

	IElementType ELSE_KEYWORD = new IElementType("ELSE_KEYWORD", CSharpPreprocessorLanguage.INSTANCE);

	IElementType ELIF_KEYWORD = new IElementType("ELIF_KEYWORD", CSharpPreprocessorLanguage.INSTANCE);

	IElementType ENDIF_KEYWORD = new IElementType("ENDIF_KEYWORD", CSharpPreprocessorLanguage.INSTANCE);

	IElementType REGION_KEYWORD = new IElementType("REGION_KEYWORD", CSharpPreprocessorLanguage.INSTANCE);

	IElementType ENDREGION_KEYWORD = new IElementType("ENDREGION_KEYWORD", CSharpPreprocessorLanguage.INSTANCE);

	IElementType DEFINE_KEYWORD = new IElementType("DEFINE_KEYWORD", CSharpPreprocessorLanguage.INSTANCE);

	IElementType UNDEF_KEYWORD = new IElementType("UNDEF_KEYWORD", CSharpPreprocessorLanguage.INSTANCE);

	IElementType SHARP = new IElementType("SHARP", CSharpPreprocessorLanguage.INSTANCE);

	IElementType COMMENT = new IElementType("COMMENT", CSharpPreprocessorLanguage.INSTANCE);

	IElementType SIMPLE_VALUE = new IElementType("SIMPLE_VALUE", CSharpPreprocessorLanguage.INSTANCE);

	IElementType IDENTIFIER = new IElementType("IDENTIFIER", CSharpPreprocessorLanguage.INSTANCE);

	IElementType OROR = new IElementType("OROR", CSharpPreprocessorLanguage.INSTANCE);

	IElementType ANDAND = new IElementType("ANDAND", CSharpPreprocessorLanguage.INSTANCE);

	IElementType LPAR = new IElementType("LPAR", CSharpPreprocessorLanguage.INSTANCE);

	IElementType RPAR = new IElementType("RPAR =", CSharpPreprocessorLanguage.INSTANCE);

	IElementType EXCL = new IElementType("EXCL", CSharpPreprocessorLanguage.INSTANCE);

	IElementType CSHARP_FRAGMENT = new IElementType("CSHARP_FRAGMENT", CSharpPreprocessorLanguage.INSTANCE);

	TokenSet KEYWORDS = TokenSet.create(IF_KEYWORD, ELSE_KEYWORD, ELIF_KEYWORD, ENDIF_KEYWORD, REGION_KEYWORD, ENDREGION_KEYWORD, DEFINE_KEYWORD, UNDEF_KEYWORD);
}
