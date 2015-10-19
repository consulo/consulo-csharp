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

import org.mustbe.consulo.csharp.lang.CSharpMacroLanguage;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 23.01.14
 */
public interface CSharpMacroTokens extends TokenType
{
	IElementType IF_KEYWORD = new IElementType("IF_KEYWORD", CSharpMacroLanguage.INSTANCE);

	IElementType ELSE_KEYWORD = new IElementType("ELSE_KEYWORD", CSharpMacroLanguage.INSTANCE);

	IElementType ELIF_KEYWORD = new IElementType("ELIF_KEYWORD", CSharpMacroLanguage.INSTANCE);

	IElementType ENDIF_KEYWORD = new IElementType("ENDIF_KEYWORD", CSharpMacroLanguage.INSTANCE);

	IElementType REGION_KEYWORD = new IElementType("REGION_KEYWORD", CSharpMacroLanguage.INSTANCE);

	IElementType ENDREGION_KEYWORD = new IElementType("ENDREGION_KEYWORD", CSharpMacroLanguage.INSTANCE);

	IElementType DEFINE_KEYWORD = new IElementType("DEFINE_KEYWORD", CSharpMacroLanguage.INSTANCE);

	IElementType UNDEF_KEYWORD = new IElementType("UNDEF_KEYWORD", CSharpMacroLanguage.INSTANCE);

	IElementType SHARP = new IElementType("SHARP", CSharpMacroLanguage.INSTANCE);

	IElementType COMMENT = new IElementType("COMMENT", CSharpMacroLanguage.INSTANCE);

	IElementType SIMPLE_VALUE = new IElementType("SIMPLE_VALUE", CSharpMacroLanguage.INSTANCE);

	IElementType IDENTIFIER = new IElementType("IDENTIFIER", CSharpMacroLanguage.INSTANCE);

	IElementType OROR = new IElementType("OROR", CSharpMacroLanguage.INSTANCE);

	IElementType ANDAND = new IElementType("ANDAND", CSharpMacroLanguage.INSTANCE);

	IElementType LPAR = new IElementType("LPAR", CSharpMacroLanguage.INSTANCE);

	IElementType RPAR = new IElementType("RPAR =", CSharpMacroLanguage.INSTANCE);

	IElementType EXCL = new IElementType("EXCL", CSharpMacroLanguage.INSTANCE);

	IElementType CSHARP_FRAGMENT = new IElementType("CSHARP_FRAGMENT", CSharpMacroLanguage.INSTANCE);
}
