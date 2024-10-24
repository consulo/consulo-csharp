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

package consulo.csharp.impl.lang.formatter;

import java.util.Collections;
import java.util.List;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import consulo.document.util.TextRange;
import consulo.language.ast.ASTNode;
import consulo.language.codeStyle.*;
import consulo.language.codeStyle.Wrap;
import consulo.language.codeStyle.template.BlockWithParent;
import consulo.annotation.access.RequiredReadAction;

/**
 * @author VISTALL
 * @since 17-May-17
 */
public class CSharpDisabledFormattingBlock implements Block, BlockWithParent
{
	private final ASTNode myNode;
	private BlockWithParent myParent;

	public CSharpDisabledFormattingBlock(ASTNode node)
	{
		myNode = node;
	}

	@Nonnull
	@Override
	@RequiredReadAction
	public TextRange getTextRange()
	{
		return myNode.getTextRange();
	}

	@Nonnull
	@Override
	public List<Block> getSubBlocks()
	{
		return Collections.emptyList();
	}

	@Nullable
	@Override
	public Wrap getWrap()
	{
		return null;
	}

	@Nullable
	@Override
	public Indent getIndent()
	{
		return Indent.getNormalIndent();
	}

	@Nullable
	@Override
	public Alignment getAlignment()
	{
		return null;
	}

	@Nullable
	@Override
	public Spacing getSpacing(@Nullable Block child1, @Nonnull Block child2)
	{
		return null;
	}

	@Nonnull
	@Override
	public ChildAttributes getChildAttributes(int newChildIndex)
	{
		return new ChildAttributes(null, null);
	}

	@Override
	public boolean isIncomplete()
	{
		return false;
	}

	@Override
	public boolean isLeaf()
	{
		return true;
	}

	@Override
	public BlockWithParent getParent()
	{
		return myParent;
	}

	@Override
	public void setParent(BlockWithParent newParent)
	{
		myParent = newParent;
	}
}
