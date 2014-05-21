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
	IElementType MACRO_IF_KEYWORD = new IElementType("MACRO_IF_KEYWORD", CSharpMacroLanguage.INSTANCE);

	IElementType MACRO_ELSE_KEYWORD = new IElementType("MACRO_IF_KEYWORD", CSharpMacroLanguage.INSTANCE);

	IElementType MACRO_ELIF_KEYWORD = new IElementType("MACRO_ELIF_KEYWORD", CSharpMacroLanguage.INSTANCE);

	IElementType MACRO_ENDIF_KEYWORD = new IElementType("MACRO_ENDIF_KEYWORD", CSharpMacroLanguage.INSTANCE);

	IElementType MACRO_REGION_KEYWORD = new IElementType("MACRO_REGION_KEYWORD", CSharpMacroLanguage.INSTANCE);

	IElementType MACRO_ENDREGION_KEYWORD = new IElementType("MACRO_ENDREGION_KEYWORD", CSharpMacroLanguage.INSTANCE);

	IElementType MACRO_DEFINE_KEYWORD = new IElementType("MACRO_DEFINE_KEYWORD", CSharpMacroLanguage.INSTANCE);

	IElementType MACRO_UNDEF_KEYWORD = new IElementType("MACRO_UNDEF_KEYWORD", CSharpMacroLanguage.INSTANCE);

	IElementType MACRO_VALUE = new IElementType("MACRO_VALUE", CSharpMacroLanguage.INSTANCE);

	IElementType MACRO_STOP = new IElementType("MACRO_STOP", CSharpMacroLanguage.INSTANCE);

	IElementType IDENTIFIER = new IElementType("IDENTIFIER", CSharpMacroLanguage.INSTANCE);

	IElementType OROR = new IElementType("OROR", CSharpMacroLanguage.INSTANCE);

	IElementType ANDAND = new IElementType("ANDAND", CSharpMacroLanguage.INSTANCE);

	IElementType LPAR = new IElementType("LPAR", CSharpMacroLanguage.INSTANCE);

	IElementType RPAR = new IElementType("RPAR =", CSharpMacroLanguage.INSTANCE);

	IElementType EXCL = new IElementType("EXCL", CSharpMacroLanguage.INSTANCE);

	IElementType CSHARP_FRAGMENT = new IElementType("CSHARP_FRAGMENT", CSharpMacroLanguage.INSTANCE);
}
