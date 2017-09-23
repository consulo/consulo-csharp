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

package consulo.csharp.lang.psi.impl.source.resolve.handlers;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.util.text.CharFilter;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.ResolveState;
import com.intellij.util.Processor;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpNamespaceDeclaration;
import consulo.csharp.lang.psi.CSharpUsingNamespaceStatement;
import consulo.csharp.lang.psi.impl.source.resolve.CSharpResolveOptions;
import consulo.csharp.lang.psi.impl.source.resolve.CSharpResolveResult;
import consulo.csharp.lang.psi.impl.source.resolve.StubScopeProcessor;
import consulo.dotnet.lang.psi.impl.BaseDotNetNamespaceAsElement;
import consulo.dotnet.lang.psi.impl.stub.DotNetNamespaceStubUtil;
import consulo.dotnet.psi.DotNetReferenceExpression;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import consulo.dotnet.resolve.DotNetNamespaceAsElement;
import consulo.dotnet.resolve.DotNetPsiSearcher;

/**
 * @author VISTALL
 * @since 27.07.2015
 */
public class QualifiedNamespaceKindProcessor implements KindProcessor
{
	@RequiredReadAction
	@Override
	public void process(@NotNull CSharpResolveOptions options,
			@NotNull DotNetGenericExtractor defaultExtractor,
			@Nullable PsiElement forceQualifierElement,
			@NotNull final Processor<ResolveResult> processor)
	{
		PsiElement element = options.getElement();

		String qName = StringUtil.strip(element.getText(), CharFilter.NOT_WHITESPACE_FILTER);

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
	private void processDefaultCompletion(@NotNull Processor<ResolveResult> processor, PsiElement element, PsiElement qualifier)
	{
		DotNetNamespaceAsElement namespace;
		String qualifiedText = "";
		if(qualifier != null)
		{
			qualifiedText = StringUtil.strip(qualifier.getText(), CharFilter.NOT_WHITESPACE_FILTER);
		}

		namespace = DotNetPsiSearcher.getInstance(element.getProject()).findNamespace(qualifiedText, element.getResolveScope());

		if(namespace == null)
		{
			return;
		}

		processNamespaceChildren(processor, element, namespace);
	}

	@RequiredReadAction
	private void processNamespaceChildren(@NotNull final Processor<ResolveResult> processor, PsiElement element, DotNetNamespaceAsElement namespace)
	{
		StubScopeProcessor scopeProcessor = new StubScopeProcessor()
		{
			@RequiredReadAction
			@Override
			public boolean execute(@NotNull PsiElement element, ResolveState state)
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
