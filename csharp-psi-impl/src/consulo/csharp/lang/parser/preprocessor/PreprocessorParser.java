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

package consulo.csharp.lang.parser.preprocessor;

import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.tree.IElementType;
import consulo.csharp.lang.lexer._CSharpMacroLexer;
import consulo.csharp.lang.psi.CSharpPreprocesorTokens;

/**
 * @author VISTALL
 * @since 02.03.2016
 */
public class PreprocessorParser
{
	public enum State
	{
		NONE,
		DIRECTIVE,
		VALUE
	}

	public enum Directive
	{
		DEFINE,
		UNDEF,
		IF,
		ELIF,
		ELSE,
		ENDIF,
		REGION,
		ENDREGION
	}

	public static void main(String[] args)
	{
		System.out.println(parse("#if TEST && TEST2"));
	}

	@Nullable
	public static PreprocessorDirective parse(String text)
	{
		_CSharpMacroLexer lexer = new _CSharpMacroLexer();
		lexer.start(text);

		State state = State.NONE;
		Directive directive = null;
		IElementType elementType = null;
		String value = "";

		loop:
		while((elementType = lexer.getTokenType()) != null)
		{
			switch(state)
			{
				case NONE:
					if(elementType == CSharpPreprocesorTokens.MACRO_DEFINE_KEYWORD || elementType == CSharpPreprocesorTokens.MACRO_UNDEF_KEYWORD)
					{
						state = State.DIRECTIVE;
						directive = elementType == CSharpPreprocesorTokens.MACRO_DEFINE_KEYWORD ? Directive.DEFINE : Directive.UNDEF;
					}
					else if(elementType == CSharpPreprocesorTokens.MACRO_IF_KEYWORD || elementType == CSharpPreprocesorTokens.MACRO_ELIF_KEYWORD)
					{
						state = State.DIRECTIVE;
						directive = elementType == CSharpPreprocesorTokens.MACRO_IF_KEYWORD ? Directive.IF : Directive.ELIF;
					}
					else if(elementType == CSharpPreprocesorTokens.MACRO_ENDIF_KEYWORD)
					{
						state = State.DIRECTIVE;
						directive = Directive.ENDIF;
					}
					else if(elementType == CSharpPreprocesorTokens.MACRO_REGION_KEYWORD)
					{
						state = State.DIRECTIVE;
						directive = Directive.REGION;
					}
					else if(elementType == CSharpPreprocesorTokens.MACRO_ENDREGION_KEYWORD)
					{
						state = State.DIRECTIVE;
						directive = Directive.ENDREGION;
					}
					else if(elementType == CSharpPreprocesorTokens.MACRO_ELSE_KEYWORD)
					{
						state = State.DIRECTIVE;
						directive = Directive.ELSE;
					}
					else if(elementType != CSharpPreprocesorTokens.WHITE_SPACE)
					{
						break loop;
					}
					break;
				case DIRECTIVE:
					switch(directive)
					{
						case DEFINE:
						case UNDEF:
							if(elementType == CSharpPreprocesorTokens.IDENTIFIER)
							{
								value = lexer.getTokenText();
								state = State.VALUE;
							}
							else if(elementType != CSharpPreprocesorTokens.WHITE_SPACE)
							{
								break loop;
							}
							break;
						case ELIF:
						case IF:
						case REGION:
							value += lexer.getTokenText();
							break;
						default:
							break loop;
					}
					break;
				default:
					if(elementType != CSharpPreprocesorTokens.WHITE_SPACE)
					{
						break loop;
					}
			}

			lexer.advance();
		}

		if(directive != null)
		{
			switch(directive)
			{
				case DEFINE:
				case UNDEF:
					if(StringUtil.isEmpty(value))
					{
						return null;
					}
					return new DefinePreprocessorDirective(value, directive == Directive.UNDEF);
				case IF:
				case ELIF:
					return new IfPreprocessorDirective(value, directive == Directive.ELIF);
				case REGION:
					return new RegionPreprocessorDirective(text);
				case ELSE:
					return ElsePreprocessorDirective.INSTANCE;
				case ENDIF:
					return EndIfPreprocessorDirective.INSTANCE;
				case ENDREGION:
					return EndRegionPreprocessorDirective.INSTANCE;
			}
		}

		return null;
	}
}
