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

package consulo.csharp.lang.doc.ide.competion;

import java.util.Collection;

import javax.annotation.Nonnull;
import consulo.csharp.lang.doc.psi.CSharpDocAttribute;
import consulo.csharp.lang.doc.psi.CSharpDocTagImpl;
import consulo.csharp.lang.doc.psi.CSharpDocTokenType;
import consulo.csharp.lang.doc.validation.CSharpDocAttributeInfo;
import consulo.csharp.lang.doc.validation.CSharpDocTagInfo;
import consulo.csharp.lang.doc.validation.CSharpDocTagManager;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import consulo.annotation.access.RequiredReadAction;
import consulo.codeInsight.completion.CompletionProvider;

/**
 * @author VISTALL
 * @since 03.03.2015
 */
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
}
