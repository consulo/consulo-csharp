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

package consulo.csharp.lang.impl;

import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.impl.psi.CSharpTokensImpl;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.language.CodeDocumentationAwareCommenter;
import consulo.language.Language;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiComment;
import jakarta.annotation.Nonnull;

import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 13.01.14
 */
@ExtensionImpl
public class CSharpCommenter implements CodeDocumentationAwareCommenter
{
	@Nullable
	@Override
	public String getLineCommentPrefix()
	{
		return "//";
	}

	@Nullable
	@Override
	public String getBlockCommentPrefix()
	{
		return "/*";
	}

	@Nullable
	@Override
	public String getBlockCommentSuffix()
	{
		return "*/";
	}

	@Nullable
	@Override
	public String getCommentedBlockCommentPrefix()
	{
		return null;
	}

	@Nullable
	@Override
	public String getCommentedBlockCommentSuffix()
	{
		return null;
	}

	@Nullable
	@Override
	public IElementType getLineCommentTokenType()
	{
		return CSharpTokens.LINE_COMMENT;
	}

	@Nullable
	@Override
	public IElementType getBlockCommentTokenType()
	{
		return CSharpTokens.BLOCK_COMMENT;
	}

	@Nullable
	@Override
	public IElementType getDocumentationCommentTokenType()
	{
		return CSharpTokensImpl.LINE_DOC_COMMENT;
	}

	@Nullable
	@Override
	public String getDocumentationCommentPrefix()
	{
		return null;
	}

	@Nullable
	@Override
	public String getDocumentationCommentLinePrefix()
	{
		return "///";
	}

	@Nullable
	@Override
	public String getDocumentationCommentSuffix()
	{
		return null;
	}

	@Override
	public boolean isDocumentationComment(PsiComment psiComment)
	{
		return false;
	}

	@Nonnull
	@Override
	public Language getLanguage()
	{
		return CSharpLanguage.INSTANCE;
	}
}
