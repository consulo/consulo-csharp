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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.psi.PsiElement;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.ide.highlight.CSharpHighlightContext;
import consulo.csharp.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.impl.source.CSharpBlockStatementImpl;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetStatement;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.resolve.DotNetTypeRefUtil;

/**
 * @author VISTALL
 * @since 16-Nov-17
 *
 * TODO [VISTALL] need rewrite this check, after introducing control flow
 */
public class CS0161 extends CompilerCheck<CSharpMethodDeclaration>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpHighlightContext highlightContext, @NotNull CSharpMethodDeclaration element)
	{
		DotNetTypeRef returnTypeRef = element.getReturnTypeRef();
		if(DotNetTypeRefUtil.isVmQNameEqual(returnTypeRef, element, DotNetTypes.System.Void))
		{
			return null;
		}

		PsiElement codeBlock = element.getCodeBlock();
		if(codeBlock instanceof CSharpBlockStatementImpl)
		{
			DotNetStatement[] statements = ((CSharpBlockStatementImpl) codeBlock).getStatements();
			if(statements.length == 0)
			{
				return newBuilder(getNameIdentifier(element), formatElement(element));
			}
		}

		return null;
	}
}
