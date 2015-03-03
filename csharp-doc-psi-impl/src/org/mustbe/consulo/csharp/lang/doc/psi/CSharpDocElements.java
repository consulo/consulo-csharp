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

package org.mustbe.consulo.csharp.lang.doc.psi;

import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.doc.CSharpDocLanguage;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LazyParseableElement;
import com.intellij.psi.tree.ElementTypeAsPsiFactory;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.ILazyParseableElementType;

/**
 * @author VISTALL
 * @since 03.03.2015
 */
public interface CSharpDocElements
{
	ElementTypeAsPsiFactory TAG = new ElementTypeAsPsiFactory("TAG", CSharpDocLanguage.INSTANCE, CSharpDocTag.class);
	ElementTypeAsPsiFactory ATTRIBUTE = new ElementTypeAsPsiFactory("ATTRIBUTE", CSharpDocLanguage.INSTANCE, CSharpDocAttribute.class);
	ElementTypeAsPsiFactory ATTRIBUTE_VALUE = new ElementTypeAsPsiFactory("ATTRIBUTE_VALUE", CSharpDocLanguage.INSTANCE,
			CSharpDocAttributeValue.class);
	ElementTypeAsPsiFactory TEXT = new ElementTypeAsPsiFactory("TEXT", CSharpDocLanguage.INSTANCE, CSharpDocText.class);

	IElementType LINE_DOC_COMMENT = new ILazyParseableElementType("LINE_DOC_COMMENT", CSharpDocLanguage.INSTANCE)
	{
		@Override
		protected Language getLanguageForParser(PsiElement psi)
		{
			return CSharpDocLanguage.INSTANCE;
		}

		@Nullable
		@Override
		public ASTNode createNode(CharSequence text)
		{
			return new LazyParseableElement(this, text);
		}
	};
}
