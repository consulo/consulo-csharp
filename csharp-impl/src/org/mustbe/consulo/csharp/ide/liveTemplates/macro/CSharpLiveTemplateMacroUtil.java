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

package org.mustbe.consulo.csharp.ide.liveTemplates.macro;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpReferenceExpressionImplUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.AbstractScopeProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.CompletionResolveScopeProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.ExecuteTarget;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.SimpleNamedScopeProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import com.intellij.openapi.util.Couple;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.ResolveState;

/**
 * @author VISTALL
 * @since 29.12.14
 */
public class CSharpLiveTemplateMacroUtil
{
	@NotNull
	public static List<DotNetVariable> resolveAllVariables(PsiElement scope)
	{
		Couple<PsiElement> resolveLayers = CSharpReferenceExpressionImplUtil.getResolveLayers(scope, false);

		AbstractScopeProcessor processor = new SimpleNamedScopeProcessor(true, ExecuteTarget.LOCAL_VARIABLE_OR_PARAMETER);
		CSharpResolveUtil.treeWalkUp(processor, scope, scope, resolveLayers.getFirst());

		processor = new CompletionResolveScopeProcessor(scope, processor.toResolveResults(), new ExecuteTarget[]{
				ExecuteTarget.FIELD,
				ExecuteTarget.PROPERTY,
				ExecuteTarget.EVENT
		});

		CSharpResolveUtil.walkChildren(processor, resolveLayers.getSecond(), true, false, ResolveState.initial());

		ResolveResult[] resolveResults = processor.toResolveResults();
		List<DotNetVariable> list = new ArrayList<DotNetVariable>(resolveResults.length);
		for(ResolveResult resolveResultWithWeight : resolveResults)
		{
			PsiElement element = resolveResultWithWeight.getElement();

			if(element instanceof DotNetVariable)
			{
				list.add((DotNetVariable) element);
			}
		}
		return list;
	}
}
