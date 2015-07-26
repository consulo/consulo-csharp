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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.CSharpResolveOptions;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.StubScopeProcessor;
import org.mustbe.consulo.dotnet.lang.psi.impl.BaseDotNetNamespaceAsElement;
import org.mustbe.consulo.dotnet.lang.psi.impl.stub.DotNetNamespaceStubUtil;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetNamespaceAsElement;
import org.mustbe.consulo.dotnet.resolve.DotNetPsiSearcher;
import com.intellij.openapi.util.text.CharFilter;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.ResolveState;
import com.intellij.util.Processor;

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

		if(!options.isCompletion())
		{
			namespace = DotNetPsiSearcher.getInstance(element.getProject()).findNamespace(qName, element.getResolveScope());

			if(namespace == null)
			{
				return;
			}
			processor.process(new PsiElementResolveResult(namespace, true));
		}
		else
		{
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

			StubScopeProcessor scopeProcessor = new StubScopeProcessor()
			{
				@RequiredReadAction
				@Override
				public boolean execute(@NotNull PsiElement element, ResolveState state)
				{
					if(element instanceof DotNetNamespaceAsElement)
					{
						if(StringUtil.equals(((DotNetNamespaceAsElement) element).getPresentableQName(),
								DotNetNamespaceStubUtil.ROOT_FOR_INDEXING))
						{
							return true;
						}
						processor.process(new PsiElementResolveResult(element, true));
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
}
