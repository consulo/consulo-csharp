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

package consulo.csharp.lang.formatter;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpTokens;

/**
 * @author VISTALL
 * @since 17-May-17
 */
public class CSharpDisabledBlock extends UserDataHolderBase implements ASTNode, Cloneable
{
	private final List<ASTNode> myNodes;

	public CSharpDisabledBlock(List<ASTNode> nodes)
	{
		myNodes = nodes;
	}

	@Nullable
	@Override
	public IElementType getElementType()
	{
		return CSharpTokens.NON_ACTIVE_SYMBOL;
	}

	@Override
	public Object clone()
	{
		return super.clone();
	}

	@RequiredReadAction
	@Override
	public String getText()
	{
		throw new UnsupportedOperationException();
	}

	@RequiredReadAction
	@NotNull
	@Override
	public CharSequence getChars()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean textContains(char c)
	{
		throw new UnsupportedOperationException();
	}

	@RequiredReadAction
	@Override
	public int getStartOffset()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public int getTextLength()
	{
		throw new UnsupportedOperationException();
	}

	@RequiredReadAction
	@NotNull
	@Override
	public TextRange getTextRange()
	{
		ASTNode first = myNodes.get(0);
		ASTNode last = myNodes.get(myNodes.size() - 1);
		return new TextRange(first.getStartOffset(), last.getStartOffset() + last.getTextLength());
	}

	@Override
	public ASTNode getTreeParent()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public ASTNode getFirstChildNode()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public ASTNode getLastChildNode()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public ASTNode getTreeNext()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public ASTNode getTreePrev()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public ASTNode[] getChildren(@Nullable TokenSet filter)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void addChild(@NotNull ASTNode child)
	{
		throw new UnsupportedOperationException();

	}

	@Override
	public void addChild(@NotNull ASTNode child, @Nullable ASTNode anchorBefore)
	{
		throw new UnsupportedOperationException();

	}

	@Override
	public void addLeaf(@NotNull IElementType leafType, CharSequence leafText, @Nullable ASTNode anchorBefore)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeChild(@NotNull ASTNode child)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeRange(@NotNull ASTNode firstNodeToRemove, ASTNode firstNodeToKeep)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void replaceChild(@NotNull ASTNode oldChild, @NotNull ASTNode newChild)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void replaceAllChildrenToChildrenOf(ASTNode anotherParent)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void addChildren(ASTNode firstChild, ASTNode firstChildToNotAdd, ASTNode anchorBefore)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public ASTNode copyElement()
	{
		throw new UnsupportedOperationException();
	}

	@Nullable
	@Override
	public ASTNode findLeafElementAt(int offset)
	{
		throw new UnsupportedOperationException();
	}

	@Nullable
	@Override
	public ASTNode findChildByType(IElementType type)
	{
		throw new UnsupportedOperationException();
	}

	@Nullable
	@Override
	public ASTNode findChildByType(IElementType type, @Nullable ASTNode anchor)
	{
		throw new UnsupportedOperationException();
	}

	@Nullable
	@Override
	public ASTNode findChildByType(@NotNull TokenSet typesSet)
	{
		throw new UnsupportedOperationException();
	}

	@Nullable
	@Override
	public ASTNode findChildByType(@NotNull TokenSet typesSet, @Nullable ASTNode anchor)
	{
		throw new UnsupportedOperationException();
	}

	@Nullable
	@Override
	public PsiElement getPsi()
	{
		throw new UnsupportedOperationException();
	}

	@Nullable
	@Override
	public <T extends PsiElement> T getPsi(Class<T> clazz)
	{
		throw new UnsupportedOperationException();
	}
}
