/*
 * Copyright 2013-2015 must-be.org
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

package consulo.csharp.lang.formatter.processors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpCallArgument;
import consulo.csharp.lang.psi.CSharpCallArgumentList;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.psi.CSharpXXXAccessorOwner;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetParameterList;
import consulo.dotnet.psi.DotNetXXXAccessor;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;

/**
 * @author VISTALL
 * @since 28.05.2015
 */
public class CSharpFormattingUtil
{
	@RequiredReadAction
	public static boolean wantContinuationIndent(@Nullable PsiElement element)
	{
		PsiElement parent = element == null ? null : element.getParent();

		if(element instanceof CSharpCallArgument)
		{
			return wantContinuationIndentByParent(parent);
		}
		else if(element instanceof DotNetParameter)
		{
			return wantContinuationIndentByParent(parent);
		}
		return false;
	}

	public static boolean wantContinuationIndentByParent(@Nullable PsiElement parent)
	{
		if(parent instanceof CSharpCallArgumentList)
		{
			//PsiElement openElement = ((CSharpCallArgumentList) parent).getOpenElement();
			//PsiElement closeElement = ((CSharpCallArgumentList) parent).getCloseElement();

			return true;
		}
		else if(parent instanceof DotNetParameterList)
		{
			//ASTNode openElement = parent.getNode().findChildByType(TokenSet.create(CSharpTokens.LPAR, CSharpTokens.LBRACKET));
			ASTNode closeElement = parent.getNode().findChildByType(TokenSet.create(CSharpTokens.RPAR, CSharpTokens.RBRACKET));
			if(closeElement == null)
			{
				return false;
			}

			return true;
		}
		return false;
	}

	public static boolean isAutoAccessorOwner(@NotNull PsiElement element)
	{
		if(element instanceof CSharpXXXAccessorOwner)
		{
			DotNetXXXAccessor[] accessors = ((CSharpXXXAccessorOwner) element).getAccessors();
			for(DotNetXXXAccessor accessor : accessors)
			{
				PsiElement codeBlock = accessor.getCodeBlock();
				if(codeBlock != null)
				{
					return false;
				}
			}
			return true;
		}
		return false;
	}
}
