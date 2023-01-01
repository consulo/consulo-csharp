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

package consulo.csharp.impl.ide.liveTemplates.macro;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.source.CSharpReferenceExpressionImplUtil;
import consulo.csharp.lang.impl.psi.source.resolve.AsPsiElementProcessor;
import consulo.csharp.lang.impl.psi.source.resolve.CSharpResolveOptions;
import consulo.csharp.lang.impl.psi.source.resolve.CompletionResolveScopeProcessor;
import consulo.csharp.lang.impl.psi.source.resolve.ExecuteTarget;
import consulo.csharp.lang.impl.psi.source.resolve.SimpleNamedScopeProcessor;
import consulo.csharp.lang.impl.psi.source.resolve.StubScopeProcessor;
import consulo.csharp.lang.impl.psi.source.resolve.util.CSharpResolveUtil;
import consulo.dotnet.psi.DotNetVariable;
import consulo.language.psi.PsiElement;
import consulo.util.lang.Couple;
import consulo.language.psi.resolve.ResolveState;

/**
 * @author VISTALL
 * @since 29.12.14
 */
public class CSharpLiveTemplateMacroUtil
{
	@Nonnull
	@RequiredReadAction
	public static List<DotNetVariable> resolveAllVariables(PsiElement scope)
	{
		Couple<PsiElement> resolveLayers = CSharpReferenceExpressionImplUtil.getResolveLayers(scope, false);

		AsPsiElementProcessor psiElementProcessor = new AsPsiElementProcessor();
		StubScopeProcessor processor = new SimpleNamedScopeProcessor(psiElementProcessor, true, ExecuteTarget.LOCAL_VARIABLE_OR_PARAMETER_OR_LOCAL_METHOD);
		CSharpResolveUtil.treeWalkUp(processor, scope, scope, resolveLayers.getFirst());

		CSharpResolveOptions options = CSharpResolveOptions.build();
		options.element(scope);

		processor = new CompletionResolveScopeProcessor(options, psiElementProcessor, new ExecuteTarget[]{
				ExecuteTarget.FIELD,
				ExecuteTarget.PROPERTY,
				ExecuteTarget.EVENT
		});

		CSharpResolveUtil.walkChildren(processor, resolveLayers.getSecond(), true, false, ResolveState.initial());

		List<DotNetVariable> list = new LinkedList<DotNetVariable>();
		for(PsiElement element : psiElementProcessor.getElements())
		{
			if(element instanceof DotNetVariable)
			{
				list.add((DotNetVariable) element);
			}
		}
		return list;
	}
}
