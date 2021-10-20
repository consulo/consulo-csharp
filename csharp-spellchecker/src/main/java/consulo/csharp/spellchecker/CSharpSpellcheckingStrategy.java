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

package consulo.csharp.spellchecker;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy;
import com.intellij.spellchecker.tokenizer.Tokenizer;
import consulo.csharp.lang.psi.CSharpUserType;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 10.03.14
 */
public class CSharpSpellcheckingStrategy extends SpellcheckingStrategy
{
	@Nonnull
	@Override
	public Tokenizer getTokenizer(PsiElement element)
	{
		if(element instanceof PsiNameIdentifierOwner)
		{
			return CSharpNameIdentifierOwnerTokenizer.INSTANCE;
		}

		if(element instanceof CSharpUserType)
		{
			return CSharpUserTypeTokenizer.INSTANCE;
		}
		return super.getTokenizer(element);
	}
}
