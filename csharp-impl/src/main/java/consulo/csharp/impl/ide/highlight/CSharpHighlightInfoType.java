/*
 * Copyright 2013-2023 consulo.io
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

import consulo.codeEditor.CodeInsightColors;
import consulo.language.editor.rawHighlight.HighlightDisplayKey;
import consulo.language.editor.rawHighlight.HighlightInfoType;

/**
 * @author VISTALL
 * @since 17/08/2023
 */
public interface CSharpHighlightInfoType
{
	HighlightInfoType UNUSED =
			new HighlightInfoType.HighlightInfoTypeSeverityByKey(HighlightDisplayKey.findOrRegister("CSharpUnused", "C# Unused Declaration", "CSharpUnused"), CodeInsightColors
					.NOT_USED_ELEMENT_ATTRIBUTES);
}
