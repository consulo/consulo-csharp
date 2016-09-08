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

package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.handlers;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpLambdaExpressionImplUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpReferenceExpressionImplUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.CSharpResolveOptions;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.MethodResolveResult;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.WeightUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.MethodCalcResult;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.MethodResolver;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaResolveResult;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.util.Processor;

/**
 * @author VISTALL
 * @since 27.07.2015
 */
public class AnyMemberKindProcessor implements KindProcessor
{
	@RequiredReadAction
	@Override
	public void process(@NotNull CSharpResolveOptions options,
			@NotNull DotNetGenericExtractor defaultExtractor,
			@Nullable PsiElement forceQualifierElement,
			@NotNull Processor<ResolveResult> processor)
	{
		final PsiElement element = options.getElement();
		final boolean resolveFromParent = options.isResolveFromParent();
		final List<MethodResolveResult> methodResolveResults = new ArrayList<MethodResolveResult>();

		CSharpReferenceExpressionImplUtil.processAnyMember(options, defaultExtractor, forceQualifierElement, new Processor<ResolveResult>()
		{
			@Override
			@RequiredReadAction
			public boolean process(ResolveResult result)
			{
				PsiElement resolvedElement = result.getElement();
				if(resolvedElement instanceof CSharpElementGroup && resolveFromParent)
				{
					CSharpLambdaResolveResult lambdaResolveResult = CSharpLambdaExpressionImplUtil.resolveLeftLambdaTypeRef(element);
					if(lambdaResolveResult != null)
					{
						for(PsiElement psiElement : ((CSharpElementGroup<?>) resolvedElement).getElements())
						{
							if(psiElement instanceof DotNetLikeMethodDeclaration)
							{
								MethodCalcResult calc = MethodResolver.calc(lambdaResolveResult.getParameterTypeRefs(),
										((DotNetLikeMethodDeclaration) psiElement).getParameterTypeRefs(), element);

								methodResolveResults.add(MethodResolveResult.createResult(calc, psiElement, result));
							}
						}
					}
				}
				else
				{
					methodResolveResults.add(MethodResolveResult.createResult(MethodCalcResult.VALID, result.getElement(), result));
				}
				return true;
			}
		});

		WeightUtil.sortAndProcess(methodResolveResults, processor, options.getElement());
	}
}
