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

package consulo.csharp.lang.doc.inspection;

import javax.annotation.Nonnull;
import com.intellij.codeInsight.highlighting.HighlightErrorFilter;
import com.intellij.psi.PsiErrorElement;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.doc.CSharpDocUtil;

/**
 * @author VISTALL
 * @since 20.03.2016
 */
public class CSharpDocHighlightErrorFilter extends HighlightErrorFilter
{
	@Override
	@RequiredReadAction
	public boolean shouldHighlightErrorElement(@Nonnull PsiErrorElement element)
	{
		return !CSharpDocUtil.isInsideDoc(element);
	}
}
