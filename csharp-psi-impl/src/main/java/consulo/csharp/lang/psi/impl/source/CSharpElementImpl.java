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

package consulo.csharp.lang.psi.impl.source;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.ItemPresentationProviders;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.impl.source.tree.CompositePsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import consulo.annotation.access.RequiredReadAction;
import consulo.application.ApplicationProperties;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author VISTALL
 * @since 28.11.13.
 */
public abstract class CSharpElementImpl extends CompositePsiElement
{
	private static final Logger LOG = Logger.getInstance(CSharpElementImpl.class);

	public CSharpElementImpl(@Nonnull IElementType elementType)
	{
		super(elementType);

		if(ApplicationProperties.isInSandbox())
		{
			String name = getClass().getName();
			if(name.contains("Expression") && !name.contains("Statement") && !(this instanceof CSharpExpressionImpl))
			{
				throw new IllegalArgumentException();
			}
		}
	}

	@Nullable
	@RequiredReadAction
	protected PsiElement findChildByFilter(TokenSet tokenSet)
	{
		ASTNode[] nodes = getNode().getChildren(tokenSet);
		return nodes == null || nodes.length == 0 ? null : nodes[0].getPsi();
	}

	@Nonnull
	@RequiredReadAction
	protected PsiElement findNotNullChildByFilter(TokenSet tokenSet)
	{
		return notNullChild(findChildByFilter(tokenSet));
	}

	@Nonnull
	@RequiredReadAction
	protected <T extends PsiElement> List<T> findChildrenByType(IElementType elementType)
	{
		List<T> result = Collections.EMPTY_LIST;
		ASTNode child = getNode().getFirstChildNode();
		while(child != null)
		{
			if(elementType == child.getElementType())
			{
				if(result == Collections.EMPTY_LIST)
				{
					result = new ArrayList<T>();
				}
				result.add((T) child.getPsi());
			}
			child = child.getTreeNext();
		}
		return result;
	}


	@Nonnull
	@RequiredReadAction
	protected PsiElement findNotNullChildByType(IElementType type)
	{
		return notNullChild(findPsiChildByType(type));
	}

	@Nonnull
	@RequiredReadAction
	protected <T> T notNullChild(T child)
	{
		if(child == null)
		{
			LOG.error(getText() + "\n parent=" + getParent().getText());
		}
		return child;
	}

	@Nonnull
	@RequiredReadAction
	protected <T> T[] findChildrenByClass(Class<T> aClass)
	{
		List<T> result = new ArrayList<>();
		for(PsiElement cur = getFirstChild(); cur != null; cur = cur.getNextSibling())
		{
			if(aClass.isInstance(cur))
			{
				result.add((T) cur);
			}
		}
		return result.toArray((T[]) Array.newInstance(aClass, result.size()));
	}

	@Nullable
	@RequiredReadAction
	protected <T> T findChildByClass(Class<T> aClass)
	{
		for(PsiElement cur = getFirstChild(); cur != null; cur = cur.getNextSibling())
		{
			if(aClass.isInstance(cur))
			{
				return (T) cur;
			}
		}
		return null;
	}

	@Nonnull
	@RequiredReadAction
	protected <T> T findNotNullChildByClass(Class<T> aClass)
	{
		return notNullChild(findChildByClass(aClass));
	}

	@Nonnull
	@Override
	public GlobalSearchScope getResolveScope()
	{
		return super.getResolveScope();
	}

	@Override
	public ItemPresentation getPresentation()
	{
		return ItemPresentationProviders.getItemPresentation(this);
	}

	@Override
	public void accept(@Nonnull PsiElementVisitor visitor)
	{
		if(visitor instanceof CSharpElementVisitor)
		{
			accept((CSharpElementVisitor) visitor);
		}
		else
		{
			super.accept(visitor);
		}
	}

	public abstract void accept(@Nonnull CSharpElementVisitor visitor);
}
