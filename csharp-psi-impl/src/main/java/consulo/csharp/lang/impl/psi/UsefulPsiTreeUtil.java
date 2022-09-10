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

package consulo.csharp.lang.impl.psi;

import consulo.csharp.lang.psi.CSharpTokens;
import consulo.language.ast.ASTNode;
import consulo.language.ast.TokenType;
import consulo.language.psi.PsiComment;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiWhiteSpace;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.util.collection.ArrayUtil;
import consulo.util.collection.SmartList;
import consulo.util.lang.function.Condition;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * @author: Fedor.Korotkov
 */
public class UsefulPsiTreeUtil
{
	public static ASTNode[] findChildrenRange(ASTNode[] elements, int startOffset, int endOffset)
	{
		int i = findChildIndex(elements, startOffset);
		int j = findChildIndex(elements, endOffset);
		i = i == -1 ? 0 : i;
		j = j == -1 ? elements.length : j;
		// trim
		while(0 < j && j < elements.length && elements[j].getElementType() == TokenType.WHITE_SPACE)
		{
			--j;
		}
		int to = j;
		if(j < elements.length && elements[j].getElementType() != CSharpTokens.SEMICOLON)
		{
			// try eat until ';'
			while(j + 1 < elements.length && (elements[j + 1].getElementType() == CSharpTokens.SEMICOLON || elements[j + 1].getElementType() ==
					TokenType.WHITE_SPACE))
			{
				++j;
				if(elements[j].getElementType() == CSharpTokens.SEMICOLON)
				{
					to = j;
				}
			}
		}
		to = Math.min(elements.length, to + 1);
		if(to < i)
		{
			return ASTNode.EMPTY_ARRAY;
		}
		return Arrays.copyOfRange(elements, i, to);
	}

	private static int findChildIndex(ASTNode[] children, int offset)
	{
		for(int i = 0, length = children.length; i < length; i++)
		{
			ASTNode child = children[i];
			if(child.getTextRange().contains(offset))
			{
				return i;
			}
		}

		return -1;
	}

	public static boolean isWhitespaceOrComment(PsiElement element)
	{
		return element instanceof PsiWhiteSpace || element instanceof PsiComment;
	}

	@Nullable
	public static List<PsiElement> getPathToParentOfType(@Nullable PsiElement element, @Nonnull Class<? extends PsiElement> aClass)
	{
		if(element == null)
		{
			return null;
		}
		final List<PsiElement> result = new ArrayList<PsiElement>();
		while(element != null)
		{
			result.add(element);
			if(aClass.isInstance(element))
			{
				return result;
			}
			if(element instanceof PsiFile)
			{
				return null;
			}
			element = element.getParent();
		}

		return null;
	}

	@Nullable
	public static PsiElement getNextSiblingSkippingWhiteSpacesAndComments(@Nonnull PsiElement sibling)
	{
		return getSiblingSkippingCondition(sibling, PsiElement::getNextSibling, UsefulPsiTreeUtil::isWhitespaceOrComment, true
		);
	}

	@Nullable
	public static PsiElement getPrevSiblingSkipWhiteSpacesAndComments(@Nullable PsiElement sibling, boolean strictly)
	{
		return getPrevSiblingSkippingCondition(sibling, element -> isWhitespaceOrComment(element), strictly);
	}

	@Nullable
	public static ASTNode getPrevSiblingSkipWhiteSpacesAndComments(@Nullable ASTNode sibling)
	{
		if(sibling == null)
		{
			return null;
		}
		ASTNode result = sibling.getTreePrev();
		while(result != null && isWhitespaceOrComment(result.getPsi()))
		{
			result = result.getTreePrev();
		}
		return result;
	}

	@Nullable
	public static PsiElement getPrevSiblingSkipWhiteSpaces(@Nullable PsiElement sibling, boolean strictly)
	{
		return getPrevSiblingSkippingCondition(sibling, element -> element instanceof PsiWhiteSpace, strictly);
	}

	@Nullable
	public static PsiElement getPrevSiblingSkippingCondition(@Nullable PsiElement sibling, Condition<PsiElement> condition, boolean strictly)
	{
		return getSiblingSkippingCondition(sibling, element -> element.getPrevSibling(), condition, strictly);
	}

	@Nullable
	public static PsiElement getSiblingSkippingCondition(@Nullable PsiElement sibling,
			Function<PsiElement, PsiElement> nextSibling,
			Condition<PsiElement> condition,
			boolean strictly)
	{
		if(sibling == null)
		{
			return null;
		}
		if(sibling instanceof PsiFile)
		{
			return sibling;
		}
		PsiElement result = strictly ? nextSibling.apply(sibling) : sibling;
		while(result != null && !(result instanceof PsiFile) && condition.value(result))
		{
			result = nextSibling.apply(result);
		}
		return result;
	}

	@Nullable
	public static <T extends PsiElement> T[] getChildrenOfType(@Nullable PsiElement element,
																					@Nonnull Class<T> aClass,
																					@Nullable PsiElement lastParent)
	{
		if(element == null)
		{
			return null;
		}

		List<T> result = null;
		for(PsiElement child = element.getFirstChild(); child != null; child = child.getNextSibling())
		{
			if(lastParent == child)
			{
				break;
			}
			if(aClass.isInstance(child))
			{
				if(result == null)
				{
					result = new SmartList<T>();
				}
				//noinspection unchecked
				result.add((T) child);
			}
		}
		return result == null ? null : ArrayUtil.toObjectArray(result, aClass);
	}

	public static boolean isAncestor(@Nonnull PsiElement element, List<PsiElement> children, boolean strict)
	{
		for(PsiElement child : children)
		{
			if(child != null && !PsiTreeUtil.isAncestor(element, child, strict))
			{
				return false;
			}
		}
		return true;
	}
}
