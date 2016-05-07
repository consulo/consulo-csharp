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
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.highlight.CSharpHighlightContext;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.ide.highlight.quickFix.ReplaceTypeQuickFix;
import org.mustbe.consulo.csharp.lang.psi.CSharpLocalVariable;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpCatchStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpThrowStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetElement;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 15.05.14
 */
public class CS0155 extends CompilerCheck<DotNetElement>
{
	private static final DotNetTypeRef ourExceptionTypeRef = new CSharpTypeRefByQName(DotNetTypes.System.Exception);

	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpHighlightContext highlightContext, @NotNull DotNetElement element)
	{
		if(element instanceof DotNetExpression)
		{
			PsiElement parent = element.getParent();
			if(!(parent instanceof CSharpThrowStatementImpl))
			{
				return null;
			}

			DotNetExpression expression = (DotNetExpression) element;

			DotNetTypeRef expressionTypeRef = expression.toTypeRef(true);
			if(expressionTypeRef == DotNetTypeRef.ERROR_TYPE)
			{
				return null;
			}

			if(!CSharpTypeUtil.isInheritable(ourExceptionTypeRef, expressionTypeRef, expression))
			{
				return newBuilder(element);
			}
		}

		if(element instanceof DotNetType)
		{
			PsiElement parent = element.getParent();
			if(!(parent instanceof CSharpLocalVariable) || !(parent.getParent() instanceof CSharpCatchStatementImpl))
			{
				return null;
			}

			if(!CSharpTypeUtil.isInheritable(ourExceptionTypeRef, ((CSharpLocalVariable) parent).toTypeRef(true), element))
			{
				return newBuilder(element).addQuickFix(new ReplaceTypeQuickFix((DotNetType) element, ourExceptionTypeRef));
			}
		}
		return null;
	}
}
