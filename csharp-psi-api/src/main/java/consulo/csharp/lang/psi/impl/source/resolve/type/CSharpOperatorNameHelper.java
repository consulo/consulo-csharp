/*
 * Copyright 2013-2017 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
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

package consulo.csharp.lang.psi.impl.source.resolve.type;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.intellij.psi.tree.IElementType;
import consulo.csharp.lang.psi.CSharpTokens;

/**
 * @author VISTALL
 * @since 15.03.14
 */
public class CSharpOperatorNameHelper
{
	private static final ImmutableMap<IElementType, String> ourOperatorNames = ImmutableMap.<IElementType, String>builder().
			put(CSharpTokens.EQEQ, "==").
			put(CSharpTokens.NTEQ, "!=").
			put(CSharpTokens.LT, "<").
			put(CSharpTokens.LTEQ, "<=").
			put(CSharpTokens.GT, ">").
			put(CSharpTokens.GTEQ, ">=").
			put(CSharpTokens.MUL, "*").
			put(CSharpTokens.MULEQ, "*").
			put(CSharpTokens.DIV, "/").
			put(CSharpTokens.DIVEQ, "/").
			put(CSharpTokens.PLUS, "+").
			put(CSharpTokens.PLUSEQ, "+").
			put(CSharpTokens.PLUSPLUS, "++").
			put(CSharpTokens.MINUS, "-").
			put(CSharpTokens.MINUSEQ, "-").
			put(CSharpTokens.MINUSMINUS, "--").
			put(CSharpTokens.LTLT, "<<").
			put(CSharpTokens.LTLTEQ, "<<").
			put(CSharpTokens.GTGT, ">>").
			put(CSharpTokens.GTGTEQ, ">>").
			put(CSharpTokens.EXCL, "!").
			put(CSharpTokens.AND, "&").
			put(CSharpTokens.ANDEQ, "&").
			put(CSharpTokens.OR, "|").
			put(CSharpTokens.OREQ, "|").
			put(CSharpTokens.XOR, "^").
			put(CSharpTokens.XOREQ, "^").
			put(CSharpTokens.TILDE, "~").
			put(CSharpTokens.PERC, "%").
			put(CSharpTokens.PERCEQ, "%").build();

	@Nullable
	public static String getOperatorName(@Nonnull IElementType elementType)
	{
		return ourOperatorNames.get(elementType);
	}
}
