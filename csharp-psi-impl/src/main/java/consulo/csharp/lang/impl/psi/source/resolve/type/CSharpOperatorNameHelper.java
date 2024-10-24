/*
 * Copyright 2013-2017 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License",
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

package consulo.csharp.lang.impl.psi.source.resolve.type;

import consulo.csharp.lang.psi.CSharpTokens;
import consulo.language.ast.IElementType;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * @author VISTALL
 * @since 15.03.14
 */
public class CSharpOperatorNameHelper
{
	private static final Map<IElementType, String> ourOperatorNames = new HashMap<>();

	static
	{
		ourOperatorNames.put(CSharpTokens.EQEQ, "==");
		ourOperatorNames.put(CSharpTokens.NTEQ, "!=");
		ourOperatorNames.put(CSharpTokens.LT, "<");
		ourOperatorNames.put(CSharpTokens.LTEQ, "<=");
		ourOperatorNames.put(CSharpTokens.GT, ">");
		ourOperatorNames.put(CSharpTokens.GTEQ, ">=");
		ourOperatorNames.put(CSharpTokens.MUL, "*");
		ourOperatorNames.put(CSharpTokens.MULEQ, "*");
		ourOperatorNames.put(CSharpTokens.DIV, "/");
		ourOperatorNames.put(CSharpTokens.DIVEQ, "/");
		ourOperatorNames.put(CSharpTokens.PLUS, "+");
		ourOperatorNames.put(CSharpTokens.PLUSEQ, "+");
		ourOperatorNames.put(CSharpTokens.PLUSPLUS, "++");
		ourOperatorNames.put(CSharpTokens.MINUS, "-");
		ourOperatorNames.put(CSharpTokens.MINUSEQ, "-");
		ourOperatorNames.put(CSharpTokens.MINUSMINUS, "--");
		ourOperatorNames.put(CSharpTokens.LTLT, "<<");
		ourOperatorNames.put(CSharpTokens.LTLTEQ, "<<");
		ourOperatorNames.put(CSharpTokens.GTGT, ">>");
		ourOperatorNames.put(CSharpTokens.GTGTEQ, ">>");
		ourOperatorNames.put(CSharpTokens.EXCL, "!");
		ourOperatorNames.put(CSharpTokens.AND, "&");
		ourOperatorNames.put(CSharpTokens.ANDEQ, "&");
		ourOperatorNames.put(CSharpTokens.OR, "|");
		ourOperatorNames.put(CSharpTokens.OREQ, "|");
		ourOperatorNames.put(CSharpTokens.XOR, "^");
		ourOperatorNames.put(CSharpTokens.XOREQ, "^");
		ourOperatorNames.put(CSharpTokens.TILDE, "~");
		ourOperatorNames.put(CSharpTokens.PERC, "%");
		ourOperatorNames.put(CSharpTokens.PERCEQ, "%");
	}

	@Nullable
	public static String getOperatorName(@Nonnull IElementType elementType)
	{
		return ourOperatorNames.get(elementType);
	}
}
