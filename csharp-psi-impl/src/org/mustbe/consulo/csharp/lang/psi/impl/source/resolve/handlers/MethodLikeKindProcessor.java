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
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpReferenceExpressionImplUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.CSharpResolveOptions;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.MethodResolveResult;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.WeightUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.genericInference.GenericInferenceUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.MethodCalcResult;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.MethodResolver;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaResolveResult;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.wrapper.GenericUnwrapTool;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.util.Processor;

/**
 * @author VISTALL
 * @since 27.07.2015
 */
public class MethodLikeKindProcessor implements KindProcessor
{
	@RequiredReadAction
	@Override
	public void process(@NotNull CSharpResolveOptions options,
			@NotNull DotNetGenericExtractor defaultExtractor,
			@Nullable PsiElement forceQualifierElement,
			@NotNull Processor<ResolveResult> processor)
	{
		final CSharpCallArgumentListOwner callArgumentListOwner = options.getCallArgumentListOwner();
		final PsiElement element = options.getElement();
		final CSharpReferenceExpression.ResolveToKind kind = options.getKind();
		if(callArgumentListOwner == null)
		{
			return;
		}

		final List<MethodResolveResult> methodResolveResults = new ArrayList<MethodResolveResult>();

		CSharpReferenceExpressionImplUtil.processAnyMember(options, defaultExtractor, forceQualifierElement, new Processor<ResolveResult>()
		{
			@Override
			@RequiredReadAction
			public boolean process(final ResolveResult result)
			{
				PsiElement maybeElementGroup = result.getElement();
				if(maybeElementGroup instanceof CSharpElementGroup)
				{
					//noinspection unchecked
					((CSharpElementGroup<PsiElement>) maybeElementGroup).process(new Processor<PsiElement>()
					{
						@Override
						@RequiredReadAction
						public boolean process(PsiElement psiElement)
						{
							if(psiElement instanceof DotNetLikeMethodDeclaration)
							{
								GenericInferenceUtil.GenericInferenceResult inferenceResult = psiElement.getUserData(GenericInferenceUtil
										.INFERENCE_RESULT);

								if(inferenceResult == null && !CSharpReferenceExpressionImplUtil.isConstructorKind(kind))
								{
									inferenceResult = GenericInferenceUtil.inferenceGenericExtractor(element, callArgumentListOwner,
											(DotNetLikeMethodDeclaration) psiElement);
									psiElement = GenericUnwrapTool.extract((DotNetNamedElement) psiElement, inferenceResult.getExtractor());
								}

								MethodCalcResult calcResult = MethodResolver.calc(callArgumentListOwner, (DotNetLikeMethodDeclaration) psiElement,
										element);

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
						}
					});
				}
				else if(maybeElementGroup instanceof DotNetVariable)
				{
					DotNetTypeRef dotNetTypeRef = ((DotNetVariable) maybeElementGroup).toTypeRef(true);

					DotNetTypeResolveResult maybeLambdaResolveResult = dotNetTypeRef.resolve();

					if(maybeLambdaResolveResult instanceof CSharpLambdaResolveResult)
					{
						CSharpLambdaResolveResult lambdaTypeResolveResult = (CSharpLambdaResolveResult) maybeLambdaResolveResult;

						MethodCalcResult calcResult = MethodResolver.calc(callArgumentListOwner, lambdaTypeResolveResult.getParameterInfos(),
								element);

						methodResolveResults.add(MethodResolveResult.createResult(calcResult, maybeElementGroup, result));
					}
				}
				return true;
			}
		});

		WeightUtil.sortAndProcess(methodResolveResults, processor, options.getElement());
	}
}
