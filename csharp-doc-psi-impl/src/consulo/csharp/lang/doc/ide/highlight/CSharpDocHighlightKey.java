/*
 * Copyright 2013-2015 must-be.org
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

package consulo.csharp.lang.doc.ide.highlight;

import consulo.csharp.lang.doc.CSharpDocLanguage;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;

/**
 * @author VISTALL
 * @since 03.03.2015
 */
public interface CSharpDocHighlightKey
{
	TextAttributesKey DOC_COMMENT = TextAttributesKey.createTextAttributesKey(CSharpDocLanguage.INSTANCE,
			DefaultLanguageHighlighterColors.DOC_COMMENT);
	TextAttributesKey DOC_COMMENT_TAG = TextAttributesKey.createTextAttributesKey(CSharpDocLanguage.INSTANCE,
			DefaultLanguageHighlighterColors.DOC_COMMENT_MARKUP);
	TextAttributesKey DOC_COMMENT_ATTRIBUTE = TextAttributesKey.createTextAttributesKey(CSharpDocLanguage.INSTANCE,
			DefaultLanguageHighlighterColors.DOC_COMMENT_MARKUP);
}
