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

import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 17.05.14
 */
public interface CSharpGenericConstraintKeywordValue extends CSharpGenericConstraintValue
{
	IElementType[] KEYWORDS_AS_ARRAY = new IElementType[]{
			CSharpTokens.NEW_KEYWORD,
			CSharpTokens.CLASS_KEYWORD,
			CSharpTokens.STRUCT_KEYWORD
	};

	TokenSet KEYWORDS = TokenSet.create(KEYWORDS_AS_ARRAY);

	@Nonnull
	IElementType getKeywordElementType();
}
