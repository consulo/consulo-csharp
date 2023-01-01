/*
 * Copyright 2013-2019 consulo.io
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

package consulo.csharp.lang.impl.psi.source;

import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiUtilCore;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.csharp.lang.psi.CSharpCodeBodyProxy;
import consulo.csharp.lang.impl.psi.CSharpElements;
import consulo.csharp.lang.psi.CSharpSimpleLikeMethodAsElement;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.dotnet.psi.DotNetElement;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetStatement;
import consulo.language.psi.PsiElement;

import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2019-10-28
 */
public class CSharpCodeBodyProxyImpl implements CSharpCodeBodyProxy
{
	private final CSharpSimpleLikeMethodAsElement myMethodElement;

	public CSharpCodeBodyProxyImpl(CSharpSimpleLikeMethodAsElement methodElement)
	{
		myMethodElement = methodElement;
	}

	@Override
	@RequiredReadAction
	public boolean isSemicolonOrEmpty()
	{
		PsiElement element = getElement();
		return element == null || PsiUtilCore.getElementType(element) == CSharpTokens.SEMICOLON;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public PsiElement getElement()
	{
		ASTNode node = myMethodElement.getNode().findChildByType(CSharpElements.METHOD_BODIES);
		if(node != null)
		{
			CSharpMethodBodyImpl psi = (CSharpMethodBodyImpl) node.getPsi();
			return psi.getInnerElement();
		}


		DotNetElement element = PsiTreeUtil.findChildOfAnyType(myMethodElement, DotNetStatement.class, DotNetExpression.class);
		if(element != null)
		{
			return element;
		}

		node = myMethodElement.getNode().findChildByType(CSharpTokens.SEMICOLON);
		if(node != null)
		{
			return node.getPsi();
		}
		return null;
	}

	@Override
	public void replace(@Nullable PsiElement newElement)
	{
		ASTNode lazyNode = myMethodElement.getNode().findChildByType(CSharpElements.METHOD_BODIES);
		if(lazyNode != null)
		{
			if(newElement == null)
			{
				lazyNode.getPsi().delete();
			}
			else
			{
				lazyNode.getPsi().replace(newElement);
			}
		}
		else
		{
			PsiElement element = getElement();

			if(newElement != null)
			{
				if(element == null)
				{
					myMethodElement.add(newElement);
				}
				else
				{
					element.replace(newElement);
				}
			}
			else if(element != null)
			{
				element.delete();
			}
		}
	}

	@Override
	@RequiredWriteAction
	public void replaceBySemicolon()
	{
		ASTNode lazyNode = myMethodElement.getNode().findChildByType(CSharpElements.METHOD_BODIES);
		if(lazyNode != null)
		{
			lazyNode.getPsi().delete();
		}
		else
		{
			PsiElement element = getElement();
			if(element != null)
			{
				element.delete();
			}
		}

		myMethodElement.getNode().addLeaf(CSharpTokens.SEMICOLON, ";", null);
	}
}
