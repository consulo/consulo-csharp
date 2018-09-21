/*
 * Copyright 2013-2018 consulo.io
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

package consulo.csharp.lang.psi.impl.source.resolve.genericInference;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.intellij.psi.PsiElement;
import consulo.dotnet.resolve.DotNetTypeRef;

/**
* @author VISTALL
* @since 2018-07-23
*/
public class InferenceSessionData
{
	final Map<PsiElement, DotNetTypeRef> myData = new HashMap<>();

	public InferenceSessionData append(@Nonnull PsiElement element, @Nonnull DotNetTypeRef typeRef)
	{
		myData.put(element, typeRef);
		return this;
	}

	public boolean finish(@Nonnull PsiElement element)
	{
		myData.remove(element);
		return myData.isEmpty();
	}

	@Nullable
	public DotNetTypeRef getTypeRef(@Nonnull PsiElement element)
	{
		return myData.get(element);
	}
}
