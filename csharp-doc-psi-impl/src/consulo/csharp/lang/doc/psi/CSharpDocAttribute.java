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

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.csharp.lang.doc.validation.CSharpDocAttributeInfo;
import consulo.csharp.lang.doc.validation.CSharpDocTagInfo;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 03.03.2015
 */
public class CSharpDocAttribute extends ASTWrapperPsiElement implements PsiNameIdentifierOwner
{
	public CSharpDocAttribute(@NotNull ASTNode node)
	{
		super(node);
	}

	@Override
	public void accept(@NotNull PsiElementVisitor visitor)
	{
		if(visitor instanceof CSharpDocElementVisitor)
		{
			((CSharpDocElementVisitor) visitor).visitDocAttribute(this);
		}
		else
		{
			super.accept(visitor);
		}
	}

	@Nullable
	public CSharpDocTagInfo getTagInfo()
	{
		PsiElement parent = getParent();
		if(parent instanceof CSharpDocTag)
		{
			return ((CSharpDocTag) parent).getTagInfo();
		}
		return null;
	}

	@Nullable
	public CSharpDocAttributeInfo getAttributeInfo()
	{
		PsiElement nameIdentifier = getNameIdentifier();
		if(nameIdentifier == null)
		{
			return null;
		}
		CSharpDocTagInfo tagInfo = getTagInfo();
		if(tagInfo != null)
		{
			return tagInfo.getAttribute(nameIdentifier.getText());
		}
		return null;
	}

	@Nullable
	@Override
	public PsiElement getNameIdentifier()
	{
		return findChildByType(CSharpDocTokenType.XML_NAME);
	}

	@Override
	public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException
	{
		return null;
	}
}
