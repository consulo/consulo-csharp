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

package consulo.csharp.lang.doc.impl.parser;

import javax.annotation.Nonnull;

import consulo.language.ast.ASTNode;
import consulo.language.ast.TokenSet;
import consulo.language.parser.PsiBuilder;
import consulo.language.ast.IElementType;
import consulo.language.version.LanguageVersion;
import consulo.language.parser.PsiParser;

/**
 * @author VISTALL
 * @since 15.09.14
 */
public class CSharpDocParser implements PsiParser
{
	@Nonnull
	@Override
	public ASTNode parse(@Nonnull IElementType root, @Nonnull PsiBuilder builder, @Nonnull LanguageVersion languageVersion)
	{
		builder.enforceCommentTokens(TokenSet.EMPTY);

		final PsiBuilder.Marker file = builder.mark();
		new CSharpDocParsing(builder).parse();
		file.done(root);
		return builder.getTreeBuilt();
	}
}
