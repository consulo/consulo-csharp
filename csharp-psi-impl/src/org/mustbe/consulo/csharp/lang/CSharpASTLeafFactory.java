/*
 * Copyright 2013-2015 must-be.org
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
import org.mustbe.consulo.csharp.lang.psi.CSharpPreprocessorLazyTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpPreprocessorDirectiveImpl;
import com.intellij.lang.ASTLeafFactory;
import com.intellij.lang.LanguageVersion;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 31.10.2015
 */
public class CSharpASTLeafFactory implements ASTLeafFactory
{
	@NotNull
	@Override
	public LeafElement createLeaf(@NotNull IElementType type, @NotNull LanguageVersion<?> languageVersion, @NotNull CharSequence text)
	{
		return new CSharpPreprocessorDirectiveImpl(type, text);
	}

	@Override
	public boolean apply(@Nullable IElementType elementType)
	{
		return elementType == CSharpPreprocessorLazyTokens.PREPROCESSOR_DIRECTIVE;
	}
}
