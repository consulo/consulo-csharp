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

package consulo.csharp.impl.ide.refactoring.rename;

import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.impl.ide.refactoring.util.CSharpNameSuggesterUtil;
import consulo.csharp.lang.CSharpLanguage;
import consulo.dotnet.psi.DotNetVariable;
import consulo.language.editor.refactoring.rename.NameSuggestionProvider;
import consulo.language.editor.refactoring.rename.SuggestedNameInfo;
import consulo.language.psi.PsiElement;
import consulo.util.collection.ArrayUtil;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * @author VISTALL
 * @since 14-Nov-17
 */
@ExtensionImpl
public class CSharpNameSuggestionProvider implements NameSuggestionProvider
{
	@Nullable
	@Override
	public SuggestedNameInfo getSuggestedNames(PsiElement element, @Nullable PsiElement nameSuggestionContext, Set<String> result)
	{
		if(element.getLanguage() != CSharpLanguage.INSTANCE)
		{
			
			return null;
		}

		Iterator<String> iterator = result.iterator();
		while(iterator.hasNext())
		{
			String next = iterator.next();

			if(CSharpNameSuggesterUtil.isKeyword(next))
			{
				iterator.remove();
			}
		}

		if(element instanceof DotNetVariable)
		{
			Collection<String> names = CSharpNameSuggesterUtil.getSuggestedVariableNames((DotNetVariable) element);
			result.addAll(names);
			return new SuggestedNameInfo(ArrayUtil.toStringArray(names))
			{
			};
		}
		return null;
	}
}
