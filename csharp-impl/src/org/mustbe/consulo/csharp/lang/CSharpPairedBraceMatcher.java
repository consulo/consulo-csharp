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

package org.mustbe.consulo.csharp.lang;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokenSets;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 28.11.13.
 */
public class CSharpPairedBraceMatcher implements PairedBraceMatcher
{
	private static BracePair[] ourPairs = new BracePair[]{
			new BracePair(CSharpTokens.LBRACE, CSharpTokens.RBRACE, true),
			new BracePair(CSharpTokens.LPAR, CSharpTokens.RPAR, false),
			new BracePair(CSharpTokens.LBRACKET, CSharpTokens.RBRACKET, false)
	};

	@Override
	public BracePair[] getPairs()
	{
		return ourPairs;
	}

	@Override
	public boolean isPairedBracesAllowedBeforeType(@NotNull IElementType elementType, @Nullable IElementType contextElement)
	{
		return contextElement != null && (
				CSharpTokenSets.WHITE_SPACE == contextElement ||
				CSharpTokenSets.COMMENTS.contains(contextElement) ||
				contextElement == CSharpTokens.SEMICOLON ||
				contextElement == CSharpTokens.COMMA ||
				contextElement == CSharpTokens.RBRACKET ||
				contextElement == CSharpTokens.RPAR ||
				contextElement == CSharpTokens.RBRACE ||
				contextElement == CSharpTokens.LBRACE);
	}

	@Override
	public int getCodeConstructStart(PsiFile psiFile, int i)
	{
		return i;
	}
}
