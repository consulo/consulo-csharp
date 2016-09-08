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

package consulo.csharp.lang.psi.impl.source;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpTokens;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import consulo.annotations.RequiredReadAction;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetStatement;

/**
 * @author VISTALL
 * @since 06.01.14.
 */
public class CSharpGotoStatementImpl extends CSharpElementImpl implements DotNetStatement
{
	private static final TokenSet ourCaseOrDefaultSet = TokenSet.create(CSharpTokens.DEFAULT_KEYWORD, CSharpTokens.CASE_KEYWORD);

	public CSharpGotoStatementImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@RequiredReadAction
	public boolean isCaseOrDefault()
	{
		return findChildByType(ourCaseOrDefaultSet) != null;
	}

	@Nullable
	@RequiredReadAction
	public DotNetExpression getExpression()
	{
		return findChildByClass(DotNetExpression.class);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitGotoStatement(this);
	}
}
