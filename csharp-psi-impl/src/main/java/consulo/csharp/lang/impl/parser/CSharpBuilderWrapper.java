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

package consulo.csharp.lang.impl.parser;

import consulo.csharp.lang.impl.CSharpLanguageVersionWrapper;
import consulo.csharp.lang.impl.parser.preprocessor.*;
import consulo.csharp.lang.impl.psi.CSharpPreprocessorElements;
import consulo.csharp.lang.impl.psi.CSharpTemplateTokens;
import consulo.csharp.lang.impl.psi.stub.elementTypes.CSharpFileStubElementType;
import consulo.csharp.lang.impl.psi.stub.elementTypes.macro.MacroEvaluator;
import consulo.csharp.lang.psi.CSharpSoftTokens;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;
import consulo.language.parser.PsiBuilder;
import consulo.language.parser.PsiBuilderAdapter;
import consulo.language.version.LanguageVersion;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author VISTALL
 * @since 28.11.13.
 */
public class CSharpBuilderWrapper extends PsiBuilderAdapter
{
	private static Map<String, IElementType> ourIdentifierToSoftKeywords = new HashMap<>();
	private static Map<IElementType, String> ourSoftKeywordsToIdentifiers = new HashMap<>();

	static
	{
		for(IElementType o : CSharpSoftTokens.ALL.getTypes())
		{
			String keyword = o.toString().replace("_KEYWORD", "").toLowerCase(Locale.US);
			ourIdentifierToSoftKeywords.put(keyword, o);
			ourSoftKeywordsToIdentifiers.put(o, keyword);
		}
	}

	private TokenSet mySoftSet = TokenSet.EMPTY;
	private LanguageVersion myLanguageVersion;

	private PreprocessorState myState = new PreprocessorState();

	public CSharpBuilderWrapper(PsiBuilder delegate, LanguageVersion languageVersion)
	{
		super(delegate);
		myLanguageVersion = languageVersion;

		Set<String> variables = delegate.getUserData(CSharpFileStubElementType.PREPROCESSOR_VARIABLES);
		putUserData(CSharpFileStubElementType.PREPROCESSOR_VARIABLES, variables == null ? Collections.<String>emptySet() : variables);
	}

	@Nonnull
	public CSharpLanguageVersion getVersion()
	{
		if(myLanguageVersion instanceof CSharpLanguageVersionWrapper)
		{
			return ((CSharpLanguageVersionWrapper) myLanguageVersion).getLanguageVersion();
		}
		throw new UnsupportedOperationException(myLanguageVersion.toString());
	}

	public void enableSoftKeywords(@Nonnull TokenSet tokenSet)
	{
		mySoftSet = TokenSet.orSet(mySoftSet, tokenSet);
	}

	public void disableSoftKeywords(@Nonnull TokenSet tokenSet)
	{
		mySoftSet = TokenSet.andNot(mySoftSet, tokenSet);
	}

	public boolean enableSoftKeyword(@Nonnull IElementType elementType)
	{
		if(mySoftSet.contains(elementType))
		{
			return false;
		}
		mySoftSet = TokenSet.orSet(mySoftSet, TokenSet.create(elementType));
		return true;
	}

	public void disableSoftKeyword(@Nonnull IElementType elementType)
	{
		mySoftSet = TokenSet.andNot(mySoftSet, TokenSet.create(elementType));
	}

	public boolean isSoftKeyword(@Nonnull IElementType elementType)
	{
		IElementType tokenType = getTokenType();
		if(tokenType == CSharpTokens.IDENTIFIER)
		{
			String keywordText = Objects.requireNonNull(ourSoftKeywordsToIdentifiers.get(elementType), "not soft keyword");
			return Objects.equals(keywordText, getTokenText());
		}

		return false;
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
			if(tokenType == CSharpTokens.NON_ACTIVE_SYMBOL || tokenType == CSharpPreprocessorElements.PREPROCESSOR_DIRECTIVE || tokenType == CSharpPreprocessorElements
					.DISABLED_PREPROCESSOR_DIRECTIVE)
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

		if(tokenType == CSharpTemplateTokens.PREPROCESSOR_FRAGMENT)
		{
			IElementType toRemapTokenType = CSharpPreprocessorElements.PREPROCESSOR_DIRECTIVE;

			boolean disabledToken = processState(myState, () -> getUserData(CSharpFileStubElementType.PREPROCESSOR_VARIABLES), it -> putUserData(CSharpFileStubElementType.PREPROCESSOR_VARIABLES, it)
					, super.getTokenSequence());

			if(disabledToken)
			{
				toRemapTokenType = CSharpPreprocessorElements.DISABLED_PREPROCESSOR_DIRECTIVE;
			}

			remapCurrentToken(toRemapTokenType);
			return toRemapTokenType;
		}

		if(myState.isDisabled(false))
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

	public static boolean processState(PreprocessorState preprocessorState, Supplier<Set<String>> getVars, Consumer<Set<String>> setVars, CharSequence tokenSequence)
	{
		Set<String> variables = getVars.get();
		assert variables != null;
		PreprocessorDirective directive = PreprocessorLightParser.parse(tokenSequence);

		boolean disabledToken = preprocessorState.isDisabled(false);

		if(directive instanceof DefinePreprocessorDirective)
		{
			// if code is disabled - dont handle define
			if(!disabledToken)
			{
				Set<String> newVariables = new HashSet<>(variables);

				if(((DefinePreprocessorDirective) directive).isUndef())
				{
					newVariables.remove(((DefinePreprocessorDirective) directive).getVariable());
				}
				else
				{
					newVariables.add(((DefinePreprocessorDirective) directive).getVariable());
				}

				setVars.accept(newVariables);
			}
		}
		else if(directive instanceof IfPreprocessorDirective)
		{
			if(((IfPreprocessorDirective) directive).isElseIf())
			{
				PreprocessorState.SubState state = preprocessorState.last();
				if(state == null)
				{
					boolean evaluate = MacroEvaluator.evaluate(((IfPreprocessorDirective) directive).getValue(), variables);

					preprocessorState.newState(disabledToken ? Boolean.FALSE : evaluate);
				}
				else if(state.haveActive()) // if we already have active - disable it
				{
					state.ifDirectives.addLast(Boolean.FALSE);
				}
				else
				{
					boolean evaluate = MacroEvaluator.evaluate(((IfPreprocessorDirective) directive).getValue(), variables);

					state.ifDirectives.addLast(disabledToken ? Boolean.FALSE : evaluate);
				}
			}
			else
			{
				boolean evaluate = MacroEvaluator.evaluate(((IfPreprocessorDirective) directive).getValue(), variables);
				preprocessorState.newState(disabledToken ? Boolean.FALSE : evaluate);
			}

			// refresh current #if state
			disabledToken = preprocessorState.isDisabled(false);
		}
		else if(directive instanceof ElsePreprocessorDirective)
		{
			PreprocessorState.SubState state = preprocessorState.last();
			if(state == null)
			{
				preprocessorState.newState(Boolean.FALSE);
			}
			else if(state.haveActive())
			{
				state.ifDirectives.addLast(Boolean.FALSE);
			}
			else
			{
				disabledToken = preprocessorState.isDisabled(true);

				state.ifDirectives.addLast(disabledToken ? Boolean.FALSE : Boolean.TRUE);
			}
		}
		else if(directive instanceof EndIfPreprocessorDirective)
		{
			preprocessorState.removeLast();
		}
		else if(directive instanceof WarningDirective)
		{
			// nothing?
		}

		return disabledToken;
	}

	@Override
	public void advanceLexer()
	{
		getTokenType();  // remap if getTokenType not called

		super.advanceLexer();
	}
}
