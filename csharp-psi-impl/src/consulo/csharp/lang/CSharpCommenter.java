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

package consulo.csharp.lang;

import org.jetbrains.annotations.Nullable;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.psi.CSharpTokensImpl;
import com.intellij.lang.CodeDocumentationAwareCommenter;
import com.intellij.psi.PsiComment;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 13.01.14
 */
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
}
