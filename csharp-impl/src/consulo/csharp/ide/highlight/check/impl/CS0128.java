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
import com.intellij.psi.util.PsiTreeUtil;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.ide.highlight.CSharpHighlightContext;
import consulo.csharp.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpLocalVariable;
import consulo.csharp.lang.psi.CSharpLocalVariableDeclarationStatement;
import consulo.csharp.lang.psi.impl.source.CSharpBlockStatementImpl;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetStatement;

/**
 * @author VISTALL
 * @since 20.05.14
 */
public class CS0128 extends CompilerCheck<CSharpLocalVariable>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpHighlightContext highlightContext, @NotNull CSharpLocalVariable element)
	{
		CSharpBlockStatementImpl blockStatement = PsiTreeUtil.getParentOfType(element, CSharpBlockStatementImpl.class);
		if(blockStatement == null)
		{
			return null;
		}

		String name = element.getName();
		if(name == null)
		{
			return null;
		}

		for(DotNetStatement statement : blockStatement.getStatements())
		{
			if(statement instanceof CSharpLocalVariableDeclarationStatement)
			{
				CSharpLocalVariable[] variables = ((CSharpLocalVariableDeclarationStatement) statement).getVariables();
				for(CSharpLocalVariable variable : variables)
				{
					if(name.equals(variable.getName()) && variable != element)
					{
						return newBuilder(getNameIdentifier(element), name);
					}

					if(variable == element)
					{
						return null;
					}
				}
			}
		}
		return null;
	}
}
