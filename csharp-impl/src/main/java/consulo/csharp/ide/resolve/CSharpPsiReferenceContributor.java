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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpCallArgumentList;
import consulo.csharp.lang.psi.impl.source.CSharpIndexAccessExpressionImpl;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 04.09.14
 */
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
}
