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

import com.intellij.psi.PsiElement;
import com.intellij.util.ObjectUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.ide.highlight.CSharpHighlightContext;
import consulo.csharp.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.impl.source.CSharpBlockStatementImpl;
import consulo.csharp.lang.psi.impl.source.CSharpCheckedStatementImpl;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetStatement;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.resolve.DotNetTypeRefUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 16-Nov-17
 * <p>
 * TODO [VISTALL] need rewrite this check, after introducing control flow
 */
public class CS0161 extends CompilerCheck<CSharpMethodDeclaration>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull CSharpMethodDeclaration element)
	{
		DotNetTypeRef returnTypeRef = element.getReturnTypeRef();
		if(DotNetTypeRefUtil.isVmQNameEqual(returnTypeRef, DotNetTypes.System.Void))
		{
			return null;
		}

		PsiElement codeBlock = element.getCodeBlock().getElement();
		if(codeBlock instanceof CSharpBlockStatementImpl)
		{
			DotNetStatement[] statements = ((CSharpBlockStatementImpl) codeBlock).getStatements();
			if(statements.length == 0)
			{
				return newBuilder(ObjectUtil.chooseNotNull(((CSharpBlockStatementImpl) codeBlock).getRightBrace(), getNameIdentifier(element)), formatElement(element));
			}
			else if(statements.length == 1)
			{
				DotNetStatement statement = statements[0];
				if(statement instanceof CSharpCheckedStatementImpl)
				{
					DotNetStatement[] childStatements = ((CSharpCheckedStatementImpl) statement).getStatements();
					if(childStatements.length == 0)
					{
						return newBuilder(ObjectUtil.chooseNotNull(((CSharpBlockStatementImpl) codeBlock).getRightBrace(), getNameIdentifier(element)), formatElement(element));
					}
				}
			}
		}

		return null;
	}
}
