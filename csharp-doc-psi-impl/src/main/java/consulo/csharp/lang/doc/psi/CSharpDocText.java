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

package consulo.csharp.lang.doc.psi;

import javax.annotation.Nonnull;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;

/**
 * @author VISTALL
 * @since 03.03.2015
 */
public class CSharpDocText extends ASTWrapperPsiElement
{
	public CSharpDocText(@Nonnull ASTNode node)
	{
		super(node);
	}

	@Nonnull
	public String getInnerText()
	{
		StringBuilder builder = new StringBuilder();
		ASTNode[] children = getNode().getChildren(null);
		for(ASTNode child : children)
		{
			if(child.getElementType() == CSharpDocTokenType.DOC_LINE_START)
			{
				continue;
			}
			builder.append(child.getText());
		}
		return builder.toString();
	}
}
