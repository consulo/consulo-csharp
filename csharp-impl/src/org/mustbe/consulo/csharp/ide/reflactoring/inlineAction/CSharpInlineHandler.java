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

package org.mustbe.consulo.csharp.ide.reflactoring.inlineAction;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpFileFactory;
import org.mustbe.consulo.csharp.lang.psi.CSharpLocalVariable;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetReferenceExpression;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import com.intellij.lang.refactoring.InlineHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.containers.MultiMap;

/**
 * @author VISTALL
 * @since 12.06.14
 */
public class CSharpInlineHandler implements InlineHandler
{
	@Nullable
	@Override
	public Settings prepareInlineElement(@NotNull PsiElement element, @Nullable Editor editor, boolean invokedOnReference)
	{
		if(element instanceof CSharpLocalVariable)
		{
			DotNetExpression initializer = ((CSharpLocalVariable) element).getInitializer();
			if(initializer != null)
			{
				return new Settings()
				{
					@Override
					public boolean isOnlyOneReferenceToInline()
					{
						return true;
					}
				};
			}
		}
		return null;
	}

	@Override
	public void removeDefinition(@NotNull PsiElement element, @NotNull Settings settings)
	{
		element.delete();
	}

	@Nullable
	@Override
	public Inliner createInliner(@NotNull PsiElement element, @NotNull Settings settings)
	{
		return new Inliner()
		{
			@Nullable
			@Override
			public MultiMap<PsiElement, String> getConflicts(@NotNull PsiReference reference, @NotNull PsiElement referenced)
			{
				return MultiMap.emptyInstance();
			}

			@Override
			public void inlineUsage(@NotNull UsageInfo usage, @NotNull PsiElement referenced)
			{
				DotNetVariable variable = (DotNetVariable) referenced;
				PsiElement ref = usage.getElement();
				if(ref instanceof DotNetReferenceExpression)
				{
					DotNetExpression expression = CSharpFileFactory.createExpression(usage.getProject(), variable.getInitializer().getText());

					ref.replace(expression);
				}
			}
		};
	}
}
