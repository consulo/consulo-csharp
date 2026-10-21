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
import consulo.csharp.lang.impl.psi.CSharpElementCompareUtil;
import consulo.csharp.lang.impl.psi.source.CSharpReferenceExpressionImplUtil;
import consulo.csharp.lang.impl.psi.source.resolve.CSharpResolveOptions;
import consulo.csharp.lang.impl.psi.source.resolve.MethodResolveResult;
import consulo.csharp.lang.impl.psi.source.resolve.WeightUtil;
import consulo.csharp.lang.impl.psi.source.resolve.genericInference.GenericInferenceUtil;
import consulo.csharp.lang.impl.psi.source.resolve.methodResolving.CandidateRules;
import consulo.csharp.lang.impl.psi.source.resolve.methodResolving.MethodResolvePriorityInfo;
import consulo.csharp.lang.impl.psi.source.resolve.methodResolving.NCallArgumentBuilder;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpLambdaResolveResult;
import consulo.csharp.lang.impl.psi.source.resolve.type.wrapper.GenericUnwrapTool;
import consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeResolveResult;
import consulo.language.psi.PsiElement;
import consulo.language.psi.ResolveResult;

import org.jspecify.annotations.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author VISTALL
 * @since 27.07.2015
 */
public class MethodLikeKindProcessor implements KindProcessor
{
	@RequiredReadAction
	@Override
	public void process(CSharpResolveOptions options,
			DotNetGenericExtractor defaultExtractor,
			@Nullable PsiElement forceQualifierElement,
			Processor<ResolveResult> processor)
	{
		final CSharpCallArgumentListOwner callArgumentListOwner = options.getCallArgumentListOwner();
		final PsiElement element = options.getElement();
		final CSharpReferenceExpression.ResolveToKind kind = options.getKind();
		if(callArgumentListOwner == null)
		{
			return;
		}

		final List<MethodResolveResult> methodResolveResults = new ArrayList<>();
		final List<Candidate> candidates = new ArrayList<>();

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
						DotNetLikeMethodDeclaration declared = (DotNetLikeMethodDeclaration) psiElement;
						GenericInferenceUtil.GenericInferenceResult inferenceResult = psiElement.getUserData(GenericInferenceUtil.INFERENCE_RESULT);

						if(inferenceResult == null && !CSharpReferenceExpressionImplUtil.isConstructorKind(kind))
						{
							inferenceResult = GenericInferenceUtil.inferenceGenericExtractor(element, callArgumentListOwner, (DotNetLikeMethodDeclaration) psiElement);
							psiElement = GenericUnwrapTool.extract((DotNetNamedElement) psiElement, inferenceResult.getExtractor());
						}

						MethodResolvePriorityInfo calcResult = NCallArgumentBuilder.calc(callArgumentListOwner, (DotNetLikeMethodDeclaration) psiElement, element.getResolveScope());

						// a static method through an instance, an instance one through a type, unsatisfied constraints
						if(kind == CSharpReferenceExpression.ResolveToKind.METHOD && calcResult.isValidResult()
								&& !CandidateRules.isCandidate(element, declared, inferenceResult == null ? DotNetGenericExtractor.EMPTY : inferenceResult.getExtractor(),
								forceQualifierElement))
						{
							calcResult = calcResult.dupNoResult(-4000000);
						}

						if(inferenceResult != null && !inferenceResult.isSuccess())
						{
							calcResult = calcResult.dupNoResult(-4000000);
						}
						candidates.add(new Candidate(calcResult, psiElement, result, declared));
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

		if(kind == CSharpReferenceExpression.ResolveToKind.METHOD)
		{
			removeHiddenByDerived(candidates);
		}
		for(Candidate candidate : candidates)
		{
			methodResolveResults.add(MethodResolveResult.createResult(candidate.calcResult, candidate.element, candidate.result));
		}

		WeightUtil.sortAndProcess(methodResolveResults, processor, options.getElement());
	}

	private static final class Candidate
	{
		private MethodResolvePriorityInfo calcResult;
		private final PsiElement element;
		private final ResolveResult result;
		private final DotNetLikeMethodDeclaration declared;

		private Candidate(MethodResolvePriorityInfo calcResult, PsiElement element, ResolveResult result, DotNetLikeMethodDeclaration declared)
		{
			this.calcResult = calcResult;
			this.element = element;
			this.result = result;
			this.declared = declared;
		}
	}

	/**
	 * C# spec 12.8.10.2: of the applicable methods, those declared in a base type of the type which declares another one
	 * are removed - an override counts as declared where the method it overrides is.
	 */
	@RequiredReadAction
	private static void removeHiddenByDerived(List<Candidate> candidates)
	{
		if(candidates.size() < 2)
		{
			return;
		}

		Map<Candidate, DotNetTypeDeclaration> declaringTypes = new IdentityHashMap<>();
		for(Candidate candidate : candidates)
		{
			if(!candidate.calcResult.isValidResult() || !(candidate.declared instanceof CSharpMethodDeclaration method) || method.isExtension()
					|| method.isDelegate())
			{
				continue;
			}
			DotNetTypeDeclaration type = declaringType(method);
			if(type != null && type.getVmQName() != null)
			{
				declaringTypes.put(candidate, type);
			}
		}
		if(declaringTypes.size() < 2)
		{
			return;
		}

		for(Map.Entry<Candidate, DotNetTypeDeclaration> entry : declaringTypes.entrySet())
		{
			String baseQName = entry.getValue().getVmQName();
			for(DotNetTypeDeclaration other : declaringTypes.values())
			{
				if(!baseQName.equals(other.getVmQName()) && other.isInheritor(baseQName, true))
				{
					entry.getKey().calcResult = entry.getKey().calcResult.dupNoResult(-4000000);
					break;
				}
			}
		}
	}

	/**
	 * The type which declares the method - for an override, the base type which declares the method it overrides.
	 */
	@RequiredReadAction
	@Nullable
	private static DotNetTypeDeclaration declaringType(CSharpMethodDeclaration method)
	{
		if(!(method.getParent() instanceof DotNetTypeDeclaration type))
		{
			return null;
		}
		if(!method.hasModifier(CSharpModifier.OVERRIDE))
		{
			return type;
		}

		DotNetTypeDeclaration current = type;
		DotNetTypeDeclaration declaring = null;
		Set<String> visited = new HashSet<>();
		while((current = superClass(current)) != null && visited.add(String.valueOf(current.getVmQName())))
		{
			for(DotNetNamedElement member : current.getMembers())
			{
				if(member instanceof CSharpMethodDeclaration baseMethod && CSharpElementCompareUtil.isEqual(baseMethod, method, 0))
				{
					declaring = current;
					if(!baseMethod.hasModifier(CSharpModifier.OVERRIDE))
					{
						return declaring;
					}
				}
			}
		}
		return declaring;
	}

	@RequiredReadAction
	@Nullable
	private static DotNetTypeDeclaration superClass(DotNetTypeDeclaration type)
	{
		for(DotNetTypeRef typeRef : type.getExtendTypeRefs())
		{
			if(typeRef.resolve().getElement() instanceof DotNetTypeDeclaration superType && !superType.isInterface())
			{
				return superType;
			}
		}
		return null;
	}
}
