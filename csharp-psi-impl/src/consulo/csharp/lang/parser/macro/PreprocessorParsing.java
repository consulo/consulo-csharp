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

package consulo.csharp.lang.parser.macro;

import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;
import consulo.csharp.lang.psi.CSharpPreprocesorTokens;
import consulo.csharp.lang.psi.CSharpPreprocessorElements;

/**
 * @author VISTALL
 * @since 18.12.13.
 */
public class PreprocessorParsing implements CSharpPreprocesorTokens, CSharpPreprocessorElements
{
	public static boolean parse(PsiBuilder builder)
	{
		PsiBuilder.Marker mark = builder.mark();

		IElementType token = builder.getTokenType();
		if(token == MACRO_DEFINE_KEYWORD || token == MACRO_UNDEF_KEYWORD)
		{
			builder.advanceLexer();

			if(builder.getTokenType() == IDENTIFIER)
			{
				builder.advanceLexer();
			}
			else
			{
				builder.error("Identifier expected");
			}
			mark.done(token == MACRO_UNDEF_KEYWORD ? MACRO_UNDEF : MACRO_DEFINE);
			return true;
		}
		else if(token == MACRO_IF_KEYWORD || token == MACRO_ELIF_KEYWORD)
		{
			builder.advanceLexer();

			PsiBuilder.Marker parse = PreprocessorExpressionParsing.parse(builder);
			if(parse == null)
			{
				builder.error("Expression expected");
			}

			mark.done(MACRO_IF);

			return true;
		}
		else if(token == MACRO_ENDIF_KEYWORD || token == MACRO_ELSE_KEYWORD)
		{
			builder.advanceLexer();

			mark.done(MACRO_BLOCK_STOP);
			return true;
		}
		else if(token == MACRO_REGION_KEYWORD || token == MACRO_ENDREGION_KEYWORD)
		{
			builder.advanceLexer();

			while(!builder.eof())
			{
				builder.advanceLexer();
			}

			mark.done(token == MACRO_REGION_KEYWORD ? CSharpPreprocessorElements.REGION_DIRECTIVE : CSharpPreprocessorElements.ENDREGION_DIRECTIVE);
			return true;
		}
		else
		{
			builder.advanceLexer();

			mark.error("Expected directive");
			return false;
		}
	}
}
