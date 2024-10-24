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

package consulo.csharp.lang.doc.impl.ide.competion;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.lang.doc.impl.CSharpDocLanguage;
import consulo.csharp.lang.doc.impl.psi.CSharpDocAttribute;
import consulo.csharp.lang.doc.impl.psi.CSharpDocTagImpl;
import consulo.csharp.lang.doc.impl.psi.CSharpDocTokenType;
import consulo.csharp.lang.doc.impl.validation.CSharpDocAttributeInfo;
import consulo.csharp.lang.doc.impl.validation.CSharpDocTagInfo;
import consulo.csharp.lang.doc.impl.validation.CSharpDocTagManager;
import consulo.language.Language;
import consulo.language.editor.completion.*;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.language.pattern.StandardPatterns;
import consulo.language.psi.PsiElement;
import consulo.language.util.ProcessingContext;

import jakarta.annotation.Nonnull;
import java.util.Collection;

/**
 * @author VISTALL
 * @since 03.03.2015
 */
@ExtensionImpl
public class CSharpDocCompletionContributor extends CompletionContributor
{
	public CSharpDocCompletionContributor()
	{
		extend(CompletionType.BASIC, StandardPatterns.psiElement(CSharpDocTokenType.XML_NAME), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			public void addCompletions(@Nonnull CompletionParameters parameters, ProcessingContext context, @Nonnull CompletionResultSet result)
			{
				PsiElement parent = parameters.getPosition().getParent();
				if(parent instanceof CSharpDocTagImpl)
				{
					Collection<CSharpDocTagInfo> tags = CSharpDocTagManager.getInstance().getTags();
					for(CSharpDocTagInfo tag : tags)
					{
						result.addElement(LookupElementBuilder.create(tag.getName()));
					}
				}
				else if(parent instanceof CSharpDocAttribute)
				{
					CSharpDocTagInfo tagInfo = ((CSharpDocAttribute) parent).getTagInfo();
					if(tagInfo == null)
					{
						return;
					}
					for(CSharpDocAttributeInfo attributeInfo : tagInfo.getAttributes())
					{
						result.addElement(LookupElementBuilder.create(attributeInfo.getName()));
					}
				}
			}
		});
	}

	@Nonnull
	@Override
	public Language getLanguage()
	{
		return CSharpDocLanguage.INSTANCE;
	}
}
