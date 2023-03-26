/*
 * Copyright 2013-2021 consulo.io
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

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpUserType;
import consulo.document.util.TextRange;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetTypeList;
import consulo.language.psi.PsiElement;
import consulo.language.spellcheker.tokenizer.TokenConsumer;
import consulo.language.spellcheker.tokenizer.Tokenizer;
import consulo.language.spellcheker.tokenizer.splitter.IdentifierTokenSplitter;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 20/10/2021
 */
public class CSharpUserTypeTokenizer extends Tokenizer<CSharpUserType>
{
	public static final CSharpUserTypeTokenizer INSTANCE = new CSharpUserTypeTokenizer();

	@Override
	@RequiredReadAction
	public void tokenize(@Nonnull CSharpUserType element, TokenConsumer consumer)
	{
		consume(element, consumer);
	}

	@RequiredReadAction
	private void consume(DotNetType type, TokenConsumer consumer)
	{
		if(type instanceof CSharpUserType userType)
		{
			CSharpReferenceExpression referenceExpression = userType.getReferenceExpression();

			PsiElement referenceElement = referenceExpression.getReferenceElement();
			if(referenceElement != null)
			{
				String refName = referenceElement.getText();
				TextRange textRange = new TextRange(0, refName.length());
				if(refName.charAt(0) == '@')
				{
					textRange = new TextRange(1, textRange.getEndOffset());
				}

				consumer.consumeToken(referenceElement, refName, true, 0, textRange, IdentifierTokenSplitter.getInstance());
			}

			DotNetTypeList typeArgumentList = referenceExpression.getTypeArgumentList();

			if(typeArgumentList != null)
			{
				DotNetType[] types = typeArgumentList.getTypes();
				for(DotNetType typeArgument : types)
				{
					consume(typeArgument, consumer);
				}
			}
		}
	}
}
