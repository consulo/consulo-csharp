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

package consulo.csharp.impl.ide.highlight.check.impl;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.util.CachedValueProvider;
import consulo.csharp.impl.ide.highlight.CSharpHighlightContext;
import consulo.csharp.impl.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.impl.psi.CSharpRecursiveElementVisitor;
import consulo.csharp.lang.impl.psi.source.CSharpBlockStatementImpl;
import consulo.csharp.lang.impl.psi.source.CSharpLambdaParameterImpl;
import consulo.csharp.lang.psi.CSharpLocalVariable;
import consulo.csharp.lang.psi.CSharpSimpleLikeMethodAsElement;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetVariable;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiModificationTracker;
import consulo.language.psi.util.LanguageCachedValueUtil;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.util.collection.MultiMap;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Collection;

/**
 * @author VISTALL
 * @since 20.05.14
 * <p>
 * 0 - var name
 * 1 - var name
 * 2 - 'child' | 'parent or current'
 */
public class CS0136 extends CompilerCheck<DotNetVariable>
{
	private static class AnalyzeContext extends CSharpRecursiveElementVisitor
	{
		private MultiMap<String, DotNetVariable> myVariables = MultiMap.createLinkedSet();

		private AnalyzeContext(PsiElement e)
		{
			e.accept(this);
		}

		@Override
		@RequiredReadAction
		public void visitParameter(DotNetParameter parameter)
		{
			addIfNotNull(parameter);
		}

		@Override
		@RequiredReadAction
		public void visitLambdaParameter(CSharpLambdaParameterImpl parameter)
		{
			addIfNotNull(parameter);
		}

		@Override
		@RequiredReadAction
		public void visitLocalVariable(CSharpLocalVariable variable)
		{
			addIfNotNull(variable);
		}

		@RequiredReadAction
		private void addIfNotNull(DotNetVariable variable)
		{
			String name = variable.getName();
			if(name != null)
			{
				myVariables.putValue(name, variable);

				DotNetExpression initializer = variable.getInitializer();
				if(initializer != null)
				{
					initializer.accept(this);
				}
			}
		}
	}

	private static final String ourParentOrCurrent = "parent or current";
	private static final String ourChild = "child";

	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull DotNetVariable element)
	{
		String name = element.getName();
		if(name == null)
		{
			return null;
		}

		CSharpSimpleLikeMethodAsElement method = PsiTreeUtil.getTopmostParentOfType(element, CSharpSimpleLikeMethodAsElement.class);
		if(method == null)
		{
			return null;
		}

		AnalyzeContext context = LanguageCachedValueUtil.getCachedValue(method, () -> CachedValueProvider.Result.create(new AnalyzeContext(method), PsiModificationTracker.MODIFICATION_COUNT));

		Collection<DotNetVariable> variables = context.myVariables.get(name);
		if(variables.size() <= 1)
		{
			return null;
		}

		DotNetVariable prevVariable = null;
		for(DotNetVariable variable : variables)
		{
			if(variable == element)
			{
				break;
			}

			prevVariable = variable;
		}

		if(prevVariable != null)
		{
			//return newBuilder(getNameIdentifier(element), name, name, getScope(prevVariable, element));
		}
		return null;
	}

	private static String getScope(PsiElement mainDeclaration, PsiElement redeclaration)
	{
		CSharpBlockStatementImpl mainBlock = PsiTreeUtil.getParentOfType(mainDeclaration, CSharpBlockStatementImpl.class);
		CSharpBlockStatementImpl reBlock = PsiTreeUtil.getParentOfType(redeclaration, CSharpBlockStatementImpl.class);
		if(mainBlock != null && mainBlock == reBlock)
		{
			return ourParentOrCurrent;
		}

		if(mainDeclaration instanceof DotNetParameter)
		{
			CSharpSimpleLikeMethodAsElement mainMethod = PsiTreeUtil.getParentOfType(mainDeclaration, CSharpSimpleLikeMethodAsElement.class);
			CSharpSimpleLikeMethodAsElement reMethod = PsiTreeUtil.getParentOfType(redeclaration, CSharpSimpleLikeMethodAsElement.class);
			if(mainMethod != null && mainMethod == reMethod)
			{
				return ourParentOrCurrent;
			}
		}

		return ourChild;
	}
}
