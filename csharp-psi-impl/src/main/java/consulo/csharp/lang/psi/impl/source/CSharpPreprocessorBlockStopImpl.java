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

import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import consulo.csharp.lang.psi.CSharpMacroElementVisitor;
import consulo.csharp.lang.psi.CSharpPreprocesorTokens;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 18.12.13.
 */
public class CSharpPreprocessorBlockStopImpl extends CSharpPreprocessorElementImpl
{
	public CSharpPreprocessorBlockStopImpl(IElementType type)
	{
		super(type);
	}

	public PsiElement getKeywordElement()
	{
		TokenSet tokenSet = TokenSet.create(CSharpPreprocesorTokens.MACRO_ENDIF_KEYWORD, CSharpPreprocesorTokens.MACRO_ENDREGION_KEYWORD);
		return findNotNullChildByType(tokenSet);
	}

	@Nullable
	public PsiElement getStopElement()
	{
		return findPsiChildByType(CSharpPreprocesorTokens.MACRO_STOP);
	}

	@Override
	public void accept(@Nonnull CSharpMacroElementVisitor visitor)
	{
		visitor.visitMacroBlockStop(this);
	}
}
