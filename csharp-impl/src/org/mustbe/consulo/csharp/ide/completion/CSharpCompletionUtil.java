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

package org.mustbe.consulo.csharp.ide.completion;

import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.NotNullPairFunction;
import com.intellij.util.containers.ConcurrentFactoryMap;

/**
 * @author VISTALL
 * @since 12.06.14
 */
public class CSharpCompletionUtil
{
	private static Map<IElementType, String[]> ourCache = new ConcurrentFactoryMap<IElementType, String[]>()
	{
		@Nullable
		@Override
		protected String[] create(IElementType elementType)
		{
			if(elementType == CSharpTokens.BOOL_LITERAL)
			{
				return new String[]{
						"true",
						"false"
				};
			}
			return new String[]{elementType.toString().replace("_KEYWORD", "").toLowerCase()};
		}
	};

	public static void tokenSetToLookup(
			CompletionResultSet resultSet,
			TokenSet tokenSet,
			NotNullPairFunction<LookupElementBuilder, IElementType, LookupElementBuilder> decorator,
			Condition<IElementType> condition)
	{
		for(IElementType elementType : tokenSet.getTypes())
		{
			if(condition != null && !condition.value(elementType))
			{
				continue;
			}

			for(String keyword : ourCache.get(elementType))
			{
				LookupElementBuilder builder = LookupElementBuilder.create(keyword);
				builder = builder.bold();
				if(decorator != null)
				{
					builder = decorator.fun(builder, elementType);
				}
				resultSet.addElement(builder);
			}
		}
	}

}
