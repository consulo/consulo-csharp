/*
 * Copyright 2013-2022 consulo.io
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

import consulo.language.ast.TokenSet;

/**
 * @author VISTALL
 * @since 11-Sep-22
 */
public interface CSharpStubElementSets extends CSharpStubElements
{
	TokenSet USING_CHILDREN = TokenSet.create(USING_NAMESPACE_STATEMENT, USING_TYPE_STATEMENT, TYPE_DEF_STATEMENT);

	TokenSet GENERIC_CONSTRAINT_VALUES = TokenSet.create(GENERIC_CONSTRAINT_KEYWORD_VALUE, GENERIC_CONSTRAINT_TYPE_VALUE);

	TokenSet TYPE_SET = TokenSet.create(NULLABLE_TYPE, POINTER_TYPE, NATIVE_TYPE, TUPLE_TYPE, ARRAY_TYPE, USER_TYPE);

	TokenSet QUALIFIED_MEMBERS = TokenSet.create(NAMESPACE_DECLARATION, TYPE_DECLARATION, METHOD_DECLARATION, CONSTRUCTOR_DECLARATION,
			PROPERTY_DECLARATION, EVENT_DECLARATION, FIELD_DECLARATION, ENUM_CONSTANT_DECLARATION, CONVERSION_METHOD_DECLARATION,
			INDEX_METHOD_DECLARATION);
}

