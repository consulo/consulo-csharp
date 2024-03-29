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

import consulo.csharp.lang.CSharpLanguage;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;

/**
 * @author VISTALL
 * @since 28.11.13.
 */
public interface CSharpSoftTokens extends CSharpTokens
{
	IElementType PARTIAL_KEYWORD = new IElementType("PARTIAL_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType WHERE_KEYWORD = new IElementType("WHERE_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType WHEN_KEYWORD = new IElementType("WHEN_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType JOIN_KEYWORD = new IElementType("JOIN_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType ON_KEYWORD = new IElementType("ON_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType EQUALS_KEYWORD = new IElementType("EQUALS_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType ASCENDING_KEYWORD = new IElementType("ASCENDING_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType DESCENDING_KEYWORD = new IElementType("DESCENDING_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType GLOBAL_KEYWORD = new IElementType("GLOBAL_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType ADD_KEYWORD = new IElementType("ADD_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType REMOVE_KEYWORD = new IElementType("REMOVE_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType SET_KEYWORD = new IElementType("SET_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType GET_KEYWORD = new IElementType("GET_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType ASYNC_KEYWORD = new IElementType("ASYNC_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType VAR_KEYWORD = new IElementType("VAR_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType YIELD_KEYWORD = new IElementType("YIELD_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType AWAIT_KEYWORD = new IElementType("AWAIT_KEYWORD", CSharpLanguage.INSTANCE);

	// linq
	IElementType FROM_KEYWORD = new IElementType("FROM_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType LET_KEYWORD = new IElementType("LET_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType SELECT_KEYWORD = new IElementType("SELECT_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType GROUP_KEYWORD = new IElementType("GROUP_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType BY_KEYWORD = new IElementType("BY_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType INTO_KEYWORD = new IElementType("INTO_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType ORDERBY_KEYWORD = new IElementType("ORDERBY_KEYWORD", CSharpLanguage.INSTANCE);

	// attributes target
	IElementType ASSEMBLY_KEYWORD = new IElementType("ASSEMBLY_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType MODULE_KEYWORD = new IElementType("MODULE_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType FIELD_KEYWORD = new IElementType("FIELD_KEYWORD", CSharpLanguage.INSTANCE);

	// event is normal keyword IElementType EVENT_KEYWORD = new IElementType("EVENT_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType METHOD_KEYWORD = new IElementType("METHOD_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType PARAM_KEYWORD = new IElementType("PARAM_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType PROPERTY_KEYWORD = new IElementType("PROPERTY_KEYWORD", CSharpLanguage.INSTANCE);

	// return is normal keyword IElementType RETURN_KEYWORD = new IElementType("RETURN_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType TYPE_KEYWORD = new IElementType("TYPE_KEYWORD", CSharpLanguage.INSTANCE);

	IElementType NAMEOF_KEYWORD = new IElementType("NAMEOF_KEYWORD", CSharpLanguage.INSTANCE);

	TokenSet ALL = TokenSet.create(PARTIAL_KEYWORD, WHERE_KEYWORD, GLOBAL_KEYWORD, ADD_KEYWORD, REMOVE_KEYWORD, SET_KEYWORD, GET_KEYWORD,
			ASYNC_KEYWORD, VAR_KEYWORD, ASSEMBLY_KEYWORD, MODULE_KEYWORD, FIELD_KEYWORD, METHOD_KEYWORD, PARAM_KEYWORD, PROPERTY_KEYWORD,
			TYPE_KEYWORD, YIELD_KEYWORD, AWAIT_KEYWORD, FROM_KEYWORD, SELECT_KEYWORD, GROUP_KEYWORD, BY_KEYWORD, INTO_KEYWORD, ORDERBY_KEYWORD,
			LET_KEYWORD, ASCENDING_KEYWORD, DESCENDING_KEYWORD, JOIN_KEYWORD, ON_KEYWORD, EQUALS_KEYWORD, WHEN_KEYWORD, NAMEOF_KEYWORD);
}
