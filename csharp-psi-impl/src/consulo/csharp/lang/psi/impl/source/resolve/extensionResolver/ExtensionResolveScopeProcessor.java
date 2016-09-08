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

package consulo.csharp.lang.psi.impl.source.resolve.extensionResolver;

import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpCallArgument;
import consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import consulo.csharp.lang.psi.impl.light.CSharpLightMethodDeclaration;
import consulo.csharp.lang.psi.impl.light.CSharpLightParameterList;
import consulo.csharp.lang.psi.impl.resolve.CSharpElementGroupImpl;
import consulo.csharp.lang.psi.impl.resolve.CSharpResolveContextUtil;
import consulo.csharp.lang.psi.impl.source.resolve.CSharpResolveResult;
import consulo.csharp.lang.psi.impl.source.resolve.StubScopeProcessor;
import consulo.csharp.lang.psi.impl.source.resolve.genericInference.GenericInferenceUtil;
import consulo.csharp.lang.psi.impl.source.resolve.type.wrapper.GenericUnwrapTool;
import consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import consulo.csharp.lang.psi.resolve.CSharpResolveSelector;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetParameterList;
import consulo.dotnet.psi.DotNetParameterListOwner;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.util.Processor;
import com.intellij.util.SmartList;

/**
 * @author VISTALL
 * @since 26.03.14
 */
public class ExtensionResolveScopeProcessor extends StubScopeProcessor
{
	private final CSharpReferenceExpression myExpression;
	private final boolean myCompletion;
	private final StubScopeProcessor myProcessor;
	@Nullable
	private final CSharpCallArgumentListOwner myCallArgumentListOwner;
	private final DotNetTypeRef myQualifierTypeRef;

	private final List<CSharpMethodDeclaration> myResolvedElements = new SmartList<CSharpMethodDeclaration>();

	private ExtensionQualifierAsCallArgumentWrapper myArgumentWrapper;

	public ExtensionResolveScopeProcessor(@NotNull DotNetTypeRef qualifierTypeRef,
			@NotNull CSharpReferenceExpression expression,
			boolean completion,
			@NotNull StubScopeProcessor processor,
			@Nullable CSharpCallArgumentListOwner callArgumentListOwner)
	{
		myQualifierTypeRef = qualifierTypeRef;
		myExpression = expression;
		myCompletion = completion;
		myProcessor = processor;
		myCallArgumentListOwner = callArgumentListOwner;
		myArgumentWrapper = new ExtensionQualifierAsCallArgumentWrapper(expression.getProject(), qualifierTypeRef);
	}

	@RequiredReadAction
	@Override
	public boolean execute(@NotNull final PsiElement element, ResolveState state)
	{
		if(myCompletion)
		{
			DotNetGenericExtractor extractor = state.get(CSharpResolveUtil.EXTRACTOR);
			assert extractor != null;

			CSharpResolveContext context = CSharpResolveContextUtil.createContext(extractor, myExpression.getResolveScope(), element);

			context.processExtensionMethodGroups(new Processor<CSharpElementGroup<CSharpMethodDeclaration>>()
			{
				@Override
				@RequiredReadAction
				public boolean process(CSharpElementGroup<CSharpMethodDeclaration> elementGroup)
				{
					Collection<CSharpMethodDeclaration> elements = elementGroup.getElements();
					for(CSharpMethodDeclaration psiElement : elements)
					{
						GenericInferenceUtil.GenericInferenceResult inferenceResult = inferenceGenericExtractor(psiElement);

						DotNetTypeRef firstParameterTypeRef = getFirstTypeRefOrParameter(psiElement, inferenceResult.getExtractor());

						if(!CSharpTypeUtil.isInheritableWithImplicit(firstParameterTypeRef, myQualifierTypeRef, myExpression))
						{
							continue;
						}

						myProcessor.pushResultExternally(new CSharpResolveResult(transform(psiElement, inferenceResult, null)));
					}
					return true;
				}
			});
		}
		else
		{
			CSharpResolveSelector selector = state.get(CSharpResolveUtil.SELECTOR);
			if(selector == null)
			{
				return true;
			}

			DotNetGenericExtractor extractor = state.get(CSharpResolveUtil.EXTRACTOR);
			assert extractor != null;

			CSharpResolveContext context = CSharpResolveContextUtil.createContext(extractor, myExpression.getResolveScope(), element);

			PsiElement[] psiElements = selector.doSelectElement(context, false);

			for(PsiElement e : psiElements)
			{
				CSharpElementGroup<?> elementGroup = (CSharpElementGroup<?>) e;

				groupIteration:
				for(PsiElement psiElement : elementGroup.getElements())
				{
					CSharpMethodDeclaration methodDeclaration = (CSharpMethodDeclaration) psiElement;

					// dont need add twice or more
					for(CSharpMethodDeclaration resolvedElement : myResolvedElements)
					{
						if(resolvedElement.isEquivalentTo(methodDeclaration))
						{
							continue groupIteration;
						}
					}

					GenericInferenceUtil.GenericInferenceResult inferenceResult = inferenceGenericExtractor(methodDeclaration);

					DotNetTypeRef firstParameterTypeRef = getFirstTypeRefOrParameter(methodDeclaration, inferenceResult.getExtractor());

					if(!CSharpTypeUtil.isInheritableWithImplicit(firstParameterTypeRef, myQualifierTypeRef, myExpression))
					{
						continue;
					}

					myResolvedElements.add(transform(methodDeclaration, inferenceResult, element));
				}
			}
		}

		return true;
	}

	@NotNull
	public GenericInferenceUtil.GenericInferenceResult inferenceGenericExtractor(CSharpMethodDeclaration methodDeclaration)
	{
		CSharpCallArgument[] arguments = myCallArgumentListOwner == null ? CSharpCallArgument.EMPTY_ARRAY : myCallArgumentListOwner.getCallArguments();

		CSharpCallArgument[] newArguments = new CSharpCallArgument[arguments.length + 1];
		System.arraycopy(arguments, 0, newArguments, 1, arguments.length);

		newArguments[0] = myArgumentWrapper;

		DotNetTypeRef[] typeArgumentRefs = myExpression.getTypeArgumentListRefs();
		return GenericInferenceUtil.inferenceGenericExtractor(newArguments, typeArgumentRefs, myExpression, methodDeclaration);
	}

	public void consumeAsMethodGroup()
	{
		if(myResolvedElements.isEmpty())
		{
			return;
		}

		CSharpMethodDeclaration methodDeclaration = myResolvedElements.get(0);
		assert methodDeclaration != null;
		CSharpElementGroupImpl element = new CSharpElementGroupImpl<CSharpMethodDeclaration>(myExpression.getProject(), methodDeclaration.getName(), myResolvedElements);
		myProcessor.pushResultExternally(new CSharpResolveResult(element, true));
	}

	@NotNull
	@RequiredReadAction
	private DotNetTypeRef getFirstTypeRefOrParameter(DotNetParameterListOwner owner, DotNetGenericExtractor extractor)
	{
		DotNetParameter[] parameters = owner.getParameters();
		assert parameters.length != 0;
		assert parameters[0].hasModifier(CSharpModifier.THIS);
		return GenericUnwrapTool.exchangeTypeRef(parameters[0].toTypeRef(false), extractor, myExpression);
	}

	private static CSharpMethodDeclaration transform(final CSharpMethodDeclaration methodDeclaration, @NotNull GenericInferenceUtil.GenericInferenceResult inferenceResult,
			@Nullable PsiElement providerElement)
	{
		DotNetParameterList parameterList = methodDeclaration.getParameterList();
		assert parameterList != null;
		DotNetParameter[] oldParameters = methodDeclaration.getParameters();

		DotNetParameter[] parameters = new DotNetParameter[oldParameters.length - 1];
		System.arraycopy(oldParameters, 1, parameters, 0, parameters.length);

		CSharpLightParameterList lightParameterList = new CSharpLightParameterList(parameterList, parameters);
		CSharpLightMethodDeclaration declaration = new CSharpLightMethodDeclaration(methodDeclaration, lightParameterList)
		{
			@Override
			public boolean canNavigate()
			{
				return true;
			}

			@Override
			public void navigate(boolean requestFocus)
			{
				((Navigatable) methodDeclaration).navigate(requestFocus);
			}
		};

		CSharpMethodDeclaration extractedMethod = GenericUnwrapTool.extract(declaration, inferenceResult.getExtractor());
		extractedMethod.putUserData(CSharpResolveUtil.EXTENSION_METHOD_WRAPPER, methodDeclaration);
		extractedMethod.putUserData(GenericInferenceUtil.INFERENCE_RESULT, inferenceResult);
		if(providerElement != null)
		{
			extractedMethod.putUserData(CSharpResolveResult.FORCE_PROVIDER_ELEMENT, providerElement);
		}
		return extractedMethod;
	}
}
