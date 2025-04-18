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

package consulo.csharp.lang.impl.psi.source.resolve.handlers;

import consulo.language.psi.ResolveResult;
import consulo.application.util.function.Processor;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.source.CSharpLambdaExpressionImplUtil;
import consulo.csharp.lang.impl.psi.source.CSharpReferenceExpressionImplUtil;
import consulo.csharp.lang.impl.psi.source.resolve.CSharpResolveOptions;
import consulo.csharp.lang.impl.psi.source.resolve.MethodResolveResult;
import consulo.csharp.lang.impl.psi.source.resolve.WeightUtil;
import consulo.csharp.lang.impl.psi.source.resolve.methodResolving.MethodResolvePriorityInfo;
import consulo.csharp.lang.impl.psi.source.resolve.methodResolving.NCallArgumentBuilder;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpLambdaResolveResult;
import consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.language.psi.PsiElement;
import consulo.language.psi.scope.GlobalSearchScope;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 27.07.2015
 */
public class AnyMemberKindProcessor implements KindProcessor
{
	@RequiredReadAction
	@Override
	public void process(@Nonnull CSharpResolveOptions options,
						@Nonnull DotNetGenericExtractor defaultExtractor,
						@Nullable PsiElement forceQualifierElement,
						@Nonnull Processor<ResolveResult> processor)
	{
		final PsiElement element = options.getElement();
		GlobalSearchScope resolveScope = element.getResolveScope();
		final boolean resolveFromParent = options.isResolveFromParent();
		final List<MethodResolveResult> methodResolveResults = new ArrayList<>();

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
								MethodResolvePriorityInfo calc = NCallArgumentBuilder.calc(lambdaResolveResult.getParameterTypeRefs(), ((DotNetLikeMethodDeclaration) psiElement).getParameterTypeRefs
										(), resolveScope);

								methodResolveResults.add(MethodResolveResult.createResult(calc, psiElement, result));
							}
						}
					}
				}
				else
				{
					methodResolveResults.add(MethodResolveResult.createResult(MethodResolvePriorityInfo.TOP, result.getElement(), result));
				}
				return true;
			}
		});

		WeightUtil.sortAndProcess(methodResolveResults, processor, options.getElement());
	}
}
