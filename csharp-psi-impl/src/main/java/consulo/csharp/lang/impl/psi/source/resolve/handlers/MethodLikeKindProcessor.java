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

import consulo.annotation.access.RequiredReadAction;
import consulo.application.progress.ProgressManager;
import consulo.application.util.function.Processor;
import consulo.csharp.lang.impl.psi.source.CSharpReferenceExpressionImplUtil;
import consulo.csharp.lang.impl.psi.source.resolve.CSharpResolveOptions;
import consulo.csharp.lang.impl.psi.source.resolve.MethodResolveResult;
import consulo.csharp.lang.impl.psi.source.resolve.WeightUtil;
import consulo.csharp.lang.impl.psi.source.resolve.genericInference.GenericInferenceUtil;
import consulo.csharp.lang.impl.psi.source.resolve.methodResolving.MethodResolvePriorityInfo;
import consulo.csharp.lang.impl.psi.source.resolve.methodResolving.NCallArgumentBuilder;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpLambdaResolveResult;
import consulo.csharp.lang.impl.psi.source.resolve.type.wrapper.GenericUnwrapTool;
import consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeResolveResult;
import consulo.language.psi.PsiElement;
import consulo.language.psi.ResolveResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 27.07.2015
 */
public class MethodLikeKindProcessor implements KindProcessor
{
	@RequiredReadAction
	@Override
	public void process(@Nonnull CSharpResolveOptions options,
			@Nonnull DotNetGenericExtractor defaultExtractor,
			@Nullable PsiElement forceQualifierElement,
			@Nonnull Processor<ResolveResult> processor)
	{
		final CSharpCallArgumentListOwner callArgumentListOwner = options.getCallArgumentListOwner();
		final PsiElement element = options.getElement();
		final CSharpReferenceExpression.ResolveToKind kind = options.getKind();
		if(callArgumentListOwner == null)
		{
			return;
		}

		final List<MethodResolveResult> methodResolveResults = new ArrayList<>();

		CSharpReferenceExpressionImplUtil.processAnyMember(options, defaultExtractor, forceQualifierElement, result ->
		{
			ProgressManager.checkCanceled();

			PsiElement maybeElementGroup = result.getElement();
			if(maybeElementGroup instanceof CSharpElementGroup)
			{
				//noinspection unchecked
				((CSharpElementGroup<PsiElement>) maybeElementGroup).process(psiElement ->
				{
					ProgressManager.checkCanceled();

					if(psiElement instanceof DotNetLikeMethodDeclaration)
					{
						GenericInferenceUtil.GenericInferenceResult inferenceResult = psiElement.getUserData(GenericInferenceUtil.INFERENCE_RESULT);

						if(inferenceResult == null && !CSharpReferenceExpressionImplUtil.isConstructorKind(kind))
						{
							inferenceResult = GenericInferenceUtil.inferenceGenericExtractor(element, callArgumentListOwner, (DotNetLikeMethodDeclaration) psiElement);
							psiElement = GenericUnwrapTool.extract((DotNetNamedElement) psiElement, inferenceResult.getExtractor());
						}

						MethodResolvePriorityInfo calcResult = NCallArgumentBuilder.calc(callArgumentListOwner, (DotNetLikeMethodDeclaration) psiElement, element.getResolveScope());

						if(inferenceResult == null || inferenceResult.isSuccess())
						{
							methodResolveResults.add(MethodResolveResult.createResult(calcResult, psiElement, result));
						}
						else
						{
							methodResolveResults.add(MethodResolveResult.createResult(calcResult.dupNoResult(-4000000), psiElement, result));
						}
					}
					return true;
				});
			}
			else if(maybeElementGroup instanceof DotNetVariable)
			{
				DotNetTypeRef dotNetTypeRef = ((DotNetVariable) maybeElementGroup).toTypeRef(true);

				DotNetTypeResolveResult maybeLambdaResolveResult = dotNetTypeRef.resolve();

				if(maybeLambdaResolveResult instanceof CSharpLambdaResolveResult)
				{
					CSharpLambdaResolveResult lambdaTypeResolveResult = (CSharpLambdaResolveResult) maybeLambdaResolveResult;

					MethodResolvePriorityInfo calcResult = NCallArgumentBuilder.calc(callArgumentListOwner, lambdaTypeResolveResult.getParameterInfos(), element.getResolveScope());

					methodResolveResults.add(MethodResolveResult.createResult(calcResult, maybeElementGroup, result));
				}
			}
			return true;
		});

		WeightUtil.sortAndProcess(methodResolveResults, processor, options.getElement());
	}
}
