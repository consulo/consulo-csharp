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

import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpReferenceExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.AbstractScopeProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.CompletionResolveScopeProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.ExecuteTarget;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.SimpleNamedScopeProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.codeInsight.template.Expression;
import com.intellij.codeInsight.template.ExpressionContext;
import com.intellij.openapi.util.Couple;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.ResolveState;
import lombok.val;

/**
 * @author VISTALL
 * @since 11.06.14
 */
public class ForeachVariableMacro extends VariableTypeMacroBase
{
	@Nullable
	@Override
	protected PsiElement[] getVariables(Expression[] params, ExpressionContext context)
	{
		val psiElementAtStartOffset = context.getPsiElementAtStartOffset();
		if(psiElementAtStartOffset == null)
		{
			return PsiElement.EMPTY_ARRAY;
		}

		Couple<PsiElement> resolveLayers = CSharpReferenceExpressionImpl.getResolveLayers(psiElementAtStartOffset, false);

		AbstractScopeProcessor processor = new SimpleNamedScopeProcessor(true, ExecuteTarget.LOCAL_VARIABLE_OR_PARAMETER);
		CSharpResolveUtil.treeWalkUp(processor, psiElementAtStartOffset, psiElementAtStartOffset, resolveLayers.getFirst());

		processor = new CompletionResolveScopeProcessor(psiElementAtStartOffset.getResolveScope(), processor.toResolveResults(), new ExecuteTarget[]{
				ExecuteTarget.FIELD,
				ExecuteTarget.PROPERTY
		});

		CSharpResolveUtil.walkChildren(processor, resolveLayers.getSecond(), true, false, ResolveState.initial());

		ResolveResult[] resolveResults = processor.toResolveResults();
		List<PsiElement> list = new ArrayList<PsiElement>(resolveResults.length);
		for(ResolveResult resolveResultWithWeight : resolveResults)
		{
			PsiElement element = resolveResultWithWeight.getElement();

			DotNetTypeRef elementTypeRef = CSharpReferenceExpressionImpl.toTypeRef(element);

			DotNetTypeRef iterableTypeRef = CSharpResolveUtil.resolveIterableType(psiElementAtStartOffset, elementTypeRef);

			if(iterableTypeRef == DotNetTypeRef.ERROR_TYPE)
			{
				continue;
			}
			list.add(element);
		}
		return list.toArray(new PsiElement[list.size()]);
	}

	@Override
	public String getName()
	{
		return "csharpForeachVariable";
	}

	@Override
	public String getPresentableName()
	{
		return "foreach variable";
	}
}
