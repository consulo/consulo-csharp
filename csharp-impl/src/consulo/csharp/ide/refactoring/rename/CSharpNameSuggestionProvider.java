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

package consulo.csharp.ide.refactoring.rename;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jetbrains.annotations.Nullable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.SuggestedNameInfo;
import com.intellij.refactoring.rename.NameSuggestionProvider;
import com.intellij.util.ArrayUtil;
import consulo.csharp.ide.refactoring.util.CSharpNameSuggesterUtil;
import consulo.csharp.lang.CSharpLanguage;
import consulo.dotnet.psi.DotNetVariable;

/**
 * @author VISTALL
 * @since 14-Nov-17
 */
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

		Set<String> oldValidResult = new LinkedHashSet<>();
		for(String name : result)
		{
			if(!CSharpNameSuggesterUtil.isKeyword(name))
			{
				oldValidResult.add(name);
			}
		}

		result.clear();

		if(element instanceof DotNetVariable)
		{
			Collection<String> names = CSharpNameSuggesterUtil.getSuggestedVariableNames((DotNetVariable) element);
			result.addAll(names);
			result.addAll(oldValidResult);

			return new SuggestedNameInfo(ArrayUtil.toStringArray(names))
			{
			};
		}
		return null;
	}
}
