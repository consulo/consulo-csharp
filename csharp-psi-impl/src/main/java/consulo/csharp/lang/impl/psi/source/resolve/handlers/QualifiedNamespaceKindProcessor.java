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
import consulo.csharp.lang.impl.psi.source.CSharpUsingNamespaceStatementImpl;
import consulo.csharp.lang.impl.psi.source.resolve.CSharpResolveOptions;
import consulo.csharp.lang.impl.psi.source.resolve.CSharpResolveResult;
import consulo.csharp.lang.impl.psi.source.resolve.StubScopeProcessor;
import consulo.csharp.lang.psi.CSharpNamespaceDeclaration;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpUsingNamespaceStatement;
import consulo.dotnet.psi.DotNetReferenceExpression;
import consulo.dotnet.psi.impl.BaseDotNetNamespaceAsElement;
import consulo.dotnet.psi.impl.stub.DotNetNamespaceStubUtil;
import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.dotnet.psi.resolve.DotNetNamespaceAsElement;
import consulo.dotnet.psi.resolve.DotNetPsiSearcher;
import consulo.language.psi.PsiElement;
import consulo.language.psi.ResolveResult;
import consulo.language.psi.resolve.ResolveState;
import consulo.util.lang.StringUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 27.07.2015
 */
public class QualifiedNamespaceKindProcessor implements KindProcessor
{
	@RequiredReadAction
	@Override
	public void process(@Nonnull CSharpResolveOptions options,
			@Nonnull DotNetGenericExtractor defaultExtractor,
			@Nullable PsiElement forceQualifierElement,
			@Nonnull final Processor<ResolveResult> processor)
	{
		PsiElement element = options.getElement();

		String qName = StringUtil.strip(element.getText(), CSharpReferenceExpression.DEFAULT_REF_FILTER);
		// this is correct? we need always remove it? 
		qName = StringUtil.trimStart(qName, CSharpUsingNamespaceStatementImpl.GLOBAL_PREFIX);

		DotNetNamespaceAsElement namespace = null;

		PsiElement qualifier = options.getQualifier();

		PsiElement parent = element.getParent();
		if(!options.isCompletion())
		{
			if(parent instanceof CSharpUsingNamespaceStatement)
			{
				DotNetNamespaceAsElement namespaceAsElement = ((CSharpUsingNamespaceStatement) parent).resolve();
				if(namespaceAsElement != null)
				{
					processor.process(new CSharpResolveResult(namespaceAsElement));
				}
			}
			else
			{
				namespace = DotNetPsiSearcher.getInstance(element.getProject()).findNamespace(qName, element.getResolveScope());

				if(namespace != null)
				{
					processor.process(new CSharpResolveResult(namespace));
				}
			}
		}
		else
		{
			processDefaultCompletion(processor, element, qualifier);

			if(parent instanceof CSharpUsingNamespaceStatement)
			{
				PsiElement parentOfStatement = parent.getParent();
				if(parentOfStatement instanceof CSharpNamespaceDeclaration)
				{
					DotNetReferenceExpression namespaceReference = ((CSharpNamespaceDeclaration) parentOfStatement).getNamespaceReference();
					if(namespaceReference != null)
					{
						PsiElement resolvedElement = namespaceReference.resolve();
						if(resolvedElement instanceof DotNetNamespaceAsElement)
						{
							processNamespaceChildren(processor, element, (DotNetNamespaceAsElement) resolvedElement);
						}
					}
				}
			}
		}
	}

	@RequiredReadAction
	private void processDefaultCompletion(@Nonnull Processor<ResolveResult> processor, PsiElement element, PsiElement qualifier)
	{
		DotNetNamespaceAsElement namespace;
		String qualifiedText = "";
		if(qualifier != null)
		{
			qualifiedText = StringUtil.strip(qualifier.getText(), CSharpReferenceExpression.DEFAULT_REF_FILTER);
		}

		namespace = DotNetPsiSearcher.getInstance(element.getProject()).findNamespace(qualifiedText, element.getResolveScope());

		if(namespace == null)
		{
			return;
		}

		processNamespaceChildren(processor, element, namespace);
	}

	@RequiredReadAction
	private void processNamespaceChildren(@Nonnull final Processor<ResolveResult> processor, PsiElement element, DotNetNamespaceAsElement namespace)
	{
		StubScopeProcessor scopeProcessor = new StubScopeProcessor()
		{
			@RequiredReadAction
			@Override
			public boolean execute(@Nonnull PsiElement element, ResolveState state)
			{
				ProgressManager.checkCanceled();

				if(element instanceof DotNetNamespaceAsElement)
				{
					if(StringUtil.equals(((DotNetNamespaceAsElement) element).getPresentableQName(), DotNetNamespaceStubUtil.ROOT_FOR_INDEXING))
					{
						return true;
					}
					processor.process(new CSharpResolveResult(element));
				}
				return true;
			}
		};

		ResolveState state = ResolveState.initial();
		state = state.put(BaseDotNetNamespaceAsElement.FILTER, DotNetNamespaceAsElement.ChildrenFilter.NONE);
		state = state.put(BaseDotNetNamespaceAsElement.RESOLVE_SCOPE, element.getResolveScope());
		namespace.processDeclarations(scopeProcessor, state, null, element);
	}
}
