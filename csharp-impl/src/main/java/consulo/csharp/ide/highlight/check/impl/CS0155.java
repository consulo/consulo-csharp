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

package consulo.csharp.ide.highlight.check.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import consulo.annotations.RequiredReadAction;
import consulo.csharp.ide.highlight.CSharpHighlightContext;
import consulo.csharp.ide.highlight.check.CompilerCheck;
import consulo.csharp.ide.highlight.quickFix.ReplaceTypeQuickFix;
import consulo.csharp.lang.psi.CSharpLocalVariable;
import consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import consulo.csharp.lang.psi.impl.source.CSharpCatchStatementImpl;
import consulo.csharp.lang.psi.impl.source.CSharpThrowStatementImpl;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetElement;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 15.05.14
 */
public class CS0155 extends CompilerCheck<DotNetElement>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull DotNetElement element)
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

			if(!CSharpTypeUtil.isInheritable(new CSharpTypeRefByQName(element, DotNetTypes.System.Exception), expressionTypeRef, expression))
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

			DotNetTypeRef exceptionTypeRef = new CSharpTypeRefByQName(element, DotNetTypes.System.Exception);

			if(!CSharpTypeUtil.isInheritable(exceptionTypeRef, ((CSharpLocalVariable) parent).toTypeRef(true), element))
			{
				return newBuilder(element).addQuickFix(new ReplaceTypeQuickFix((DotNetType) element, exceptionTypeRef));
			}
		}
		return null;
	}
}
