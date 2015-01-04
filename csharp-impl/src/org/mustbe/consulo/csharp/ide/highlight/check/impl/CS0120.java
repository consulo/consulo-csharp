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

package org.mustbe.consulo.csharp.ide.highlight.check.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpContextUtil;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpressionEx;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.psi.DotNetModifierListOwner;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 31.12.14
 */
public class CS0120 extends CompilerCheck<CSharpReferenceExpressionEx>
{
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpReferenceExpressionEx element)
	{
		PsiElement referenceElement = element.getReferenceElement();
		if(referenceElement == null)
		{
			return null;
		}

		PsiElement resolvedElement = element.resolve();
		if(!(resolvedElement instanceof DotNetModifierListOwner))
		{
			return null;
		}
		CSharpContextUtil.ContextType parentContextType = CSharpContextUtil.getParentContextTypeForReference(element);

		CSharpContextUtil.ContextType contextForResolved = CSharpContextUtil.getContextForResolved(resolvedElement);
		if(parentContextType == CSharpContextUtil.ContextType.STATIC && contextForResolved.isAllowInstance())
		{
			return newBuilder(referenceElement, formatElement(resolvedElement));
		}
		else if(contextForResolved == CSharpContextUtil.ContextType.STATIC && parentContextType.isAllowInstance())
		{
			return newBuilderImpl(CS0176.class, referenceElement, formatElement(resolvedElement));
		}
		return null;
	}
}
