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

import gnu.trove.THashSet;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.tree.IElementType;
import consulo.csharp.lang.lexer._CSharpMacroLexer;
import consulo.csharp.lang.psi.CSharpPreprocesorTokens;

/**
 * @author VISTALL
 * @since 02.03.2016
 */
public class PreprocessorLightParser
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
		ENDREGION,
		PRAGMA,
		WARNING
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
					else if(elementType == CSharpPreprocesorTokens.PRAGMA_KEYWORD)
					{
						state = State.DIRECTIVE;
						directive = Directive.PRAGMA;
					}
					else if(elementType == CSharpPreprocesorTokens.WARNING_KEYWORD)
					{
						state = State.DIRECTIVE;
						directive = Directive.WARNING;
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
					else if(elementType != CSharpPreprocesorTokens.WHITE_SPACE && elementType != CSharpPreprocesorTokens.LINE_COMMENT)
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
						case PRAGMA:
						case WARNING:
							value += lexer.getTokenText();
							break;
						default:
							break loop;
					}
					break;
				default:
					if(elementType != CSharpPreprocesorTokens.WHITE_SPACE && elementType != CSharpPreprocesorTokens.LINE_COMMENT)
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
				case PRAGMA:
					return parsePragma(value);
				case REGION:
					return new RegionPreprocessorDirective(text);
				case ELSE:
					return ElsePreprocessorDirective.INSTANCE;
				case ENDIF:
					return EndIfPreprocessorDirective.INSTANCE;
				case ENDREGION:
					return EndRegionPreprocessorDirective.INSTANCE;
				case WARNING:
					return new WarningDirective(StringUtil.notNullize(value.trim()));
			}
		}

		return null;
	}

	private static PragmaWarningPreprocessorDirective parsePragma(String value)
	{
		if(StringUtil.isEmpty(value))
		{
			return null;
		}

		value = value.trim();

		List<String> split = StringUtil.split(value, " ");
		if(split.size() > 2)
		{
			String type = split.get(0);

			if("warning".equalsIgnoreCase(type))
			{
				String action = split.get(1);

				if("restore".equalsIgnoreCase(action) || "disable".equalsIgnoreCase(action))
				{
					// cut warning
					value = value.substring(type.length() + 1, value.length());
					// cut action
					value = value.substring(action.length() + 1, value.length());

					List<String> args = StringUtil.split(value, ",", true, true);

					return new PragmaWarningPreprocessorDirective(type, action, formatCheck(args));
				}
			}
		}
		return null;
	}

	private static Set<String> formatCheck(List<String> args)
	{
		Set<String> list = new THashSet<>(args.size());
		for(String arg : args)
		{
			arg = arg.trim();

			if(arg.startsWith("CS"))
			{
				list.add(arg);
			}
			else
			{
				try
				{
					int i = Integer.parseInt(arg);

					list.add(String.format("CS%04d", i));
				}
				catch(NumberFormatException ignored)
				{
				}
			}
		}

		return list;
	}
}
