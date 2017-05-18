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

package consulo.csharp.lang.psi.impl.source;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.csharp.lang.psi.CSharpMacroElementVisitor;
import consulo.csharp.lang.psi.CSharpPreprocesorTokens;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;

/**
 * @author VISTALL
 * @since 18.12.13.
 */
public class CSharpMacroBlockStartImpl extends CSharpMacroElementImpl
{
	public CSharpMacroBlockStartImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@Nullable
	public CSharpMacroExpression getValue()
	{
		return findChildByClass(CSharpMacroExpression.class);
	}

	@NotNull
	public PsiElement getKeywordElement()
	{
		TokenSet tokenSet = TokenSet.create(CSharpPreprocesorTokens.MACRO_IF_KEYWORD, CSharpPreprocesorTokens.MACRO_REGION_KEYWORD,
				CSharpPreprocesorTokens.MACRO_ELIF_KEYWORD, CSharpPreprocesorTokens.MACRO_ELSE_KEYWORD);
		return findNotNullChildByType(tokenSet);
	}

	public boolean isElse()
	{
		return findChildByType(CSharpPreprocesorTokens.MACRO_ELSE_KEYWORD) != null;
	}

	@Nullable
	public PsiElement getStopElement()
	{
		return findChildByType(CSharpPreprocesorTokens.MACRO_STOP);
	}

	@Override
	public void accept(@NotNull CSharpMacroElementVisitor visitor)
	{
		visitor.visitMacroBlockStart(this);
	}
}
