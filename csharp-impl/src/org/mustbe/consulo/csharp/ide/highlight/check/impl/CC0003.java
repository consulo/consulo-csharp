/*
 * Copyright 2013-2014 must-be.org
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

package org.mustbe.consulo.csharp.ide.highlight.check.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentList;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpArrayAccessExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.codeInsight.daemon.impl.quickfix.QuickFixActionRegistrarImpl;
import com.intellij.codeInsight.quickfix.UnresolvedReferenceQuickFixProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 19.11.14
 */
public class CC0003 extends CompilerCheck<CSharpArrayAccessExpressionImpl>
{
	@RequiredReadAction
	@NotNull
	@Override
	public List<HighlightInfoFactory> check(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpArrayAccessExpressionImpl expression)
	{
		return checkReference(expression);
	}

	@NotNull
	public static List<HighlightInfoFactory> checkReference(@NotNull final CSharpArrayAccessExpressionImpl callElement)
	{
		ResolveResult[] resolveResults = callElement.multiResolve(false);

		ResolveResult goodResult = CSharpResolveUtil.findFirstValidResult(resolveResults);

		List<PsiElement> ranges = new ArrayList<PsiElement>(2);
		CSharpCallArgumentList parameterList = callElement.getParameterList();
		if(parameterList != null)
		{
			ContainerUtil.addIfNotNull(ranges, parameterList.getOpenElement());
			ContainerUtil.addIfNotNull(ranges, parameterList.getCloseElement());
		}

		if(ranges.isEmpty())
		{
			return Collections.emptyList();
		}

		List<HighlightInfoFactory> list = new ArrayList<HighlightInfoFactory>(2);
		if(goodResult == null)
		{
			if(resolveResults.length == 0)
			{
				for(PsiElement range : ranges)
				{
					CompilerCheckBuilder result = new CompilerCheckBuilder()
					{
						@Nullable
						@Override
						public HighlightInfo create()
						{
							HighlightInfo highlightInfo = super.create();
							if(highlightInfo != null && callElement instanceof PsiReference)
							{
								UnresolvedReferenceQuickFixProvider.registerReferenceFixes((PsiReference) callElement,
										new QuickFixActionRegistrarImpl(highlightInfo));
							}
							return highlightInfo;
						}
					};
					result.setHighlightInfoType(HighlightInfoType.WRONG_REF);
					result.setText(message(CC0003.class));
					result.setTextRange(range.getTextRange());
					list.add(result);
				}
			}
			else
			{
				final HighlightInfo highlightInfo = CC0001.createHighlightInfo(callElement, resolveResults[0]);
				if(highlightInfo == null)
				{
					return list;
				}

				list.add(new HighlightInfoFactory()
				{
					@Nullable
					@Override
					public HighlightInfo create()
					{
						return highlightInfo;
					}
				});
			}
		}
		return list;
	}
}
