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

package consulo.csharp.lang.parser;

import gnu.trove.THashMap;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.csharp.lang.CSharpLanguageVersionWrapper;
import consulo.csharp.lang.parser.preprocessor.DefinePreprocessorDirective;
import consulo.csharp.lang.parser.preprocessor.ElsePreprocessorDirective;
import consulo.csharp.lang.parser.preprocessor.EndIfPreprocessorDirective;
import consulo.csharp.lang.parser.preprocessor.IfPreprocessorDirective;
import consulo.csharp.lang.parser.preprocessor.PreprocessorDirective;
import consulo.csharp.lang.parser.preprocessor.PreprocessorParser;
import consulo.csharp.lang.psi.CSharpSoftTokens;
import consulo.csharp.lang.psi.CSharpTemplateTokens;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.psi.impl.stub.elementTypes.CSharpFileStubElementType;
import consulo.csharp.lang.psi.impl.stub.elementTypes.macro.MacroEvaluator;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.lang.LanguageVersion;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.impl.PsiBuilderAdapter;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

/**
 * @author VISTALL
 * @since 28.11.13.
 */
public class CSharpBuilderWrapper extends PsiBuilderAdapter
{
	private static Map<String, IElementType> ourIdentifierToSoftKeywords = new THashMap<String, IElementType>();

	static
	{
		for(IElementType o : CSharpSoftTokens.ALL.getTypes())
		{
			String keyword = o.toString().replace("_KEYWORD", "").toLowerCase(Locale.US);
			ourIdentifierToSoftKeywords.put(keyword, o);
		}
	}

	static class PreprocessorState
	{
		Deque<Boolean> ifDirectives = new ArrayDeque<Boolean>();

		public PreprocessorState(@NotNull Boolean initialValue)
		{
			ifDirectives.add(initialValue);
		}

		boolean haveActive()
		{
			for(Boolean state : ifDirectives)
			{
				if(state)
				{
					return true;
				}
			}
			return false;
		}

		boolean isActive()
		{
			Boolean value = ifDirectives.peekLast();
			return value != null && value;
		}
	}

	private TokenSet mySoftSet = TokenSet.EMPTY;
	private LanguageVersion myLanguageVersion;

	private Deque<PreprocessorState> myStates = new ArrayDeque<PreprocessorState>();

	public CSharpBuilderWrapper(PsiBuilder delegate, LanguageVersion languageVersion)
	{
		super(delegate);
		myLanguageVersion = languageVersion;

		Set<String> variables = delegate.getUserData(CSharpFileStubElementType.PREPROCESSOR_VARIABLES);
		putUserData(CSharpFileStubElementType.PREPROCESSOR_VARIABLES, variables == null ? Collections.<String>emptySet() : variables);
	}

	@NotNull
	public CSharpLanguageVersion getVersion()
	{
		if(myLanguageVersion instanceof CSharpLanguageVersionWrapper)
		{
			return ((CSharpLanguageVersionWrapper) myLanguageVersion).getLanguageVersion();
		}
		throw new UnsupportedOperationException(myLanguageVersion.toString());
	}

	public void enableSoftKeywords(@NotNull TokenSet tokenSet)
	{
		mySoftSet = TokenSet.orSet(mySoftSet, tokenSet);
	}

	public void disableSoftKeywords(@NotNull TokenSet tokenSet)
	{
		mySoftSet = TokenSet.andNot(mySoftSet, tokenSet);
	}

	public boolean enableSoftKeyword(@NotNull IElementType elementType)
	{
		if(mySoftSet.contains(elementType))
		{
			return false;
		}
		mySoftSet = TokenSet.orSet(mySoftSet, TokenSet.create(elementType));
		return true;
	}

	public void disableSoftKeyword(@NotNull IElementType elementType)
	{
		mySoftSet = TokenSet.andNot(mySoftSet, TokenSet.create(elementType));
	}

	@Nullable
	public IElementType getTokenTypeGGLL()
	{
		IElementType tokenType = getTokenType();
		if(tokenType == CSharpTokens.LT)
		{
			if(lookAhead(1) == CSharpTokens.LT)
			{
				return CSharpTokens.LTLT;
			}
		}
		else if(tokenType == CSharpTokens.GT)
		{
			if(lookAhead(1) == CSharpTokens.GT)
			{
				return CSharpTokens.GTGT;
			}
		}
		return tokenType;
	}

	public void advanceLexerGGLL()
	{
		IElementType tokenTypeGGLL = getTokenTypeGGLL();
		if(tokenTypeGGLL == CSharpTokens.GTGT || tokenTypeGGLL == CSharpTokens.LTLT)
		{
			Marker mark = mark();
			advanceLexer();
			advanceLexer();
			mark.collapse(tokenTypeGGLL);
		}
		else
		{
			advanceLexer();
		}
	}

	public void remapBackIfSoft()
	{
		IElementType tokenType = getTokenType();
		if(ourIdentifierToSoftKeywords.containsValue(tokenType))
		{
			remapCurrentToken(CSharpTokens.IDENTIFIER);
		}
	}

	public void skipNonInterestItems()
	{
		while(!super.eof())
		{
			IElementType tokenType = getTokenTypeImpl();
			if(tokenType == CSharpTokens.NON_ACTIVE_SYMBOL || tokenType == CSharpTokens.PREPROCESSOR_DIRECTIVE)
			{
				super.advanceLexer();
			}
			else
			{
				break;
			}
		}
	}

	@Nullable
	@Override
	public IElementType getTokenType()
	{
		skipNonInterestItems();
		return super.getTokenType();
	}

	@Nullable
	public IElementType getTokenTypeImpl()
	{
		IElementType tokenType = super.getTokenType();
		if(tokenType == null)
		{
			return null;
		}

		if(tokenType == CSharpTemplateTokens.MACRO_FRAGMENT)
		{
			Set<String> variables = getUserData(CSharpFileStubElementType.PREPROCESSOR_VARIABLES);

			PreprocessorDirective directive = PreprocessorParser.parse(super.getTokenText());
			if(directive instanceof DefinePreprocessorDirective)
			{
				// if code is disabled - dont handle define
				PreprocessorState preprocessorState = myStates.peekLast();
				if(preprocessorState != null && !preprocessorState.isActive())
				{
					remapCurrentToken(CSharpTokens.NON_ACTIVE_SYMBOL);
					return CSharpTokens.NON_ACTIVE_SYMBOL;
				}

				Set<String> newVariables = new HashSet<String>(variables);

				if(((DefinePreprocessorDirective) directive).isUndef())
				{
					newVariables.remove(((DefinePreprocessorDirective) directive).getVariable());
				}
				else
				{
					newVariables.add(((DefinePreprocessorDirective) directive).getVariable());
				}
				putUserData(CSharpFileStubElementType.PREPROCESSOR_VARIABLES, newVariables);
			}
			else if(directive instanceof IfPreprocessorDirective)
			{
				if(((IfPreprocessorDirective) directive).isElseIf())
				{
					PreprocessorState state = myStates.peekLast();
					if(state == null)
					{
						boolean evaluate = MacroEvaluator.evaluate(((IfPreprocessorDirective) directive).getValue(), variables);

						myStates.add(new PreprocessorState(evaluate));
					}
					else if(state.haveActive()) // if we already have active - disable it
					{
						state.ifDirectives.addLast(Boolean.FALSE);
					}
					else
					{
						boolean evaluate = MacroEvaluator.evaluate(((IfPreprocessorDirective) directive).getValue(), variables);

						state.ifDirectives.addLast(evaluate);
					}
				}
				else
				{
					boolean evaluate = MacroEvaluator.evaluate(((IfPreprocessorDirective) directive).getValue(), variables);
					myStates.addLast(new PreprocessorState(evaluate));
				}
			}
			else if(directive instanceof ElsePreprocessorDirective)
			{
				PreprocessorState state = myStates.peekLast();
				if(state == null)
				{
					myStates.add(new PreprocessorState(Boolean.FALSE));
				}
				else if(state.haveActive())
				{
					state.ifDirectives.addLast(Boolean.FALSE);
				}
				else
				{
					state.ifDirectives.addLast(Boolean.TRUE);
				}
			}
			else if(directive instanceof EndIfPreprocessorDirective)
			{
				myStates.pollLast();
			}

			remapCurrentToken(CSharpTokens.PREPROCESSOR_DIRECTIVE);
			return CSharpTokens.PREPROCESSOR_DIRECTIVE;
		}

		PreprocessorState preprocessorState = myStates.peekLast();
		if(preprocessorState != null && !preprocessorState.isActive())
		{
			remapCurrentToken(CSharpTokens.NON_ACTIVE_SYMBOL);
			return CSharpTokens.NON_ACTIVE_SYMBOL;
		}

		if(tokenType == CSharpTokens.IDENTIFIER)
		{
			IElementType elementType = ourIdentifierToSoftKeywords.get(getTokenText());
			if(elementType != null && mySoftSet.contains(elementType))
			{
				remapCurrentToken(elementType);
				return elementType;
			}
		}
		return tokenType;
	}

	@Override
	public void advanceLexer()
	{
		getTokenType();  // remap if getTokenType not called

		super.advanceLexer();
	}
}
