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

package consulo.csharp.ide.resolve;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.impl.psi.source.CSharpIndexAccessExpressionImpl;
import consulo.csharp.lang.psi.CSharpCallArgumentList;
import consulo.document.util.TextRange;
import consulo.language.Language;
import consulo.language.pattern.StandardPatterns;
import consulo.language.psi.*;
import consulo.language.util.ProcessingContext;
import consulo.util.collection.ContainerUtil;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 04.09.14
 */
@ExtensionImpl
public class CSharpPsiReferenceContributor extends PsiReferenceContributor
{
	@Override
	public void registerReferenceProviders(PsiReferenceRegistrar psiReferenceRegistrar)
	{
		psiReferenceRegistrar.registerReferenceProvider(StandardPatterns.psiElement(CSharpCallArgumentList.class).withParent(CSharpIndexAccessExpressionImpl.class), new PsiReferenceProvider()
		{
			@Nonnull
			@Override
			@RequiredReadAction
			public PsiReference[] getReferencesByElement(@Nonnull PsiElement element, @Nonnull ProcessingContext processingContext)
			{
				CSharpIndexAccessExpressionImpl parent = (CSharpIndexAccessExpressionImpl) element.getParent();
				PsiElement callable = parent.resolveToCallable();
				if(callable == null)
				{
					return PsiReference.EMPTY_ARRAY;
				}
				CSharpCallArgumentList parameterList = parent.getParameterList();
				if(parameterList == null)
				{
					return PsiReference.EMPTY_ARRAY;
				}

				PsiElement openElement = parameterList.getOpenElement();

				List<PsiReference> list = new ArrayList<PsiReference>(2);
				if(openElement != null)
				{
					int startOffsetInParent = openElement.getStartOffsetInParent();
					list.add(new PsiReferenceBase.Immediate<PsiElement>(element, new TextRange(startOffsetInParent, startOffsetInParent + openElement.getTextLength()), callable));
				}

				PsiElement closeElement = parameterList.getCloseElement();
				if(closeElement != null)
				{
					int startOffsetInParent = closeElement.getStartOffsetInParent();
					list.add(new PsiReferenceBase.Immediate<PsiElement>(element, new TextRange(startOffsetInParent, startOffsetInParent + closeElement.getTextLength()), callable));
				}
				return ContainerUtil.toArray(list, PsiReference.ARRAY_FACTORY);
			}
		});
	}

	@Nonnull
	@Override
	public Language getLanguage()
	{
		return CSharpLanguage.INSTANCE;
	}
}
