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

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.csharp.lang.doc.validation.CSharpDocTagInfo;
import consulo.csharp.lang.doc.validation.CSharpDocTagManager;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 03.03.2015
 */
public class CSharpDocTag extends ASTWrapperPsiElement
{
	public CSharpDocTag(@NotNull ASTNode node)
	{
		super(node);
	}

	@Nullable
	public String getInnerText()
	{
		CSharpDocText textElement = getInnerTextElement();
		return textElement == null ? null : textElement.getInnerText();
	}

	@Nullable
	public CSharpDocText getInnerTextElement()
	{
		return findChildByClass(CSharpDocText.class);
	}

	@Override
	public String getName()
	{
		List<PsiElement> nameElements = getNameElements();
		return nameElements.isEmpty() ? null : ContainerUtil.getFirstItem(nameElements).getText();
	}

	@Nullable
	public CSharpDocTagInfo getTagInfo()
	{
		PsiElement tagName = ContainerUtil.getFirstItem(getNameElements());
		if(tagName == null)
		{
			return null;
		}
		return CSharpDocTagManager.getInstance().getTag(tagName.getText());
	}

	@NotNull
	public List<PsiElement> getNameElements()
	{
		return findChildrenByType(CSharpDocTokenType.XML_NAME);
	}

	@Override
	public void accept(@NotNull PsiElementVisitor visitor)
	{
		if(visitor instanceof CSharpDocElementVisitor)
		{
			((CSharpDocElementVisitor) visitor).visitDocTag(this);
		}
		else
		{
			super.accept(visitor);
		}
	}
}
