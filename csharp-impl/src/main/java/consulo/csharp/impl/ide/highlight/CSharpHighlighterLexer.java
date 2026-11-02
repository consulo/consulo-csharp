/*
 * Copyright 2013-2020 consulo.io
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

package consulo.csharp.impl.ide.highlight;

import consulo.csharp.lang.impl.lexer.CSharpLexer;
import consulo.csharp.lang.impl.lexer._CSharpLexer;
import consulo.language.lexer.RestartableLexer;
import consulo.language.lexer.TokenIterator;

/**
 * The lexer of the editor highlighter - restartable at the tokens out of any string interpolation: the editor relexes
 * from the nearest such token before a change, not from the start of the file.
 *
 * @author VISTALL
 * @since 2020-08-08
 */
public class CSharpHighlighterLexer extends CSharpLexer implements RestartableLexer {
    /**
     * Set in a state of a token started inside a string interpolation - the flex state alone does not tell it. Within
     * 16 bits: the editor keeps no more of a state (IntBasedStorage)
     */
    private static final int INSIDE_INTERPOLATION = 1 << 14;

    private static class HighlightingFlexLexer extends _CSharpLexer {
        private boolean myCleanAtTokenStart = true;

        private HighlightingFlexLexer() {
            super(true);
        }

        @Override
        public void start(CharSequence buffer, int startOffset, int endOffset, int initialState) {
            // a restore inside an interpolation (LexerBase#restore) keeps it - a restart is out of any
            boolean inside = (initialState & INSIDE_INTERPOLATION) != 0;
            if (!inside) {
                clear();
            }
            myCleanAtTokenStart = !inside;
            super.start(buffer, startOffset, endOffset, initialState & ~INSIDE_INTERPOLATION);
        }

        @Override
        public void advance() {
            // lex the current token - the lexer is clean after it if it is clean at the next one
            getTokenType();
            myCleanAtTokenStart = isClean();
            super.advance();
        }

        @Override
        public int getState() {
            int state = super.getState();
            return myCleanAtTokenStart ? state : state | INSIDE_INTERPOLATION;
        }
    }

    public CSharpHighlighterLexer() {
        super(new HighlightingFlexLexer());
    }

    @Override
    public int getStartState() {
        // not the current state - it is where the editor restarts the lexing of a whole text
        return _CSharpLexer.YYINITIAL;
    }

    @Override
    public boolean isRestartableState(int state) {
        return state == _CSharpLexer.YYINITIAL;
    }

    @Override
    public void start(CharSequence buffer, int startOffset, int endOffset, int initialState, TokenIterator tokenIterator) {
        start(buffer, startOffset, endOffset, initialState);
    }
}
