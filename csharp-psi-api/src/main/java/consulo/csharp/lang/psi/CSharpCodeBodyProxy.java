/*
 * Copyright 2013-2019 consulo.io
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

package consulo.csharp.lang.psi;

import com.intellij.psi.PsiElement;
import consulo.annotation.access.RequiredReadAction;
import consulo.dotnet.psi.DotNetCodeBodyProxy;
import consulo.dotnet.psi.DotNetElement;

import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2019-10-28
 */
public interface CSharpCodeBodyProxy extends DotNetCodeBodyProxy
{
	CSharpCodeBodyProxy EMPTY = new CSharpCodeBodyProxy()
	{
		@RequiredReadAction
		@Override
		public boolean isSemicolonOrEmpty()
		{
			return false;
		}

		@RequiredReadAction
		@Override
		public boolean isNotSemicolonAndNotEmpty()
		{
			return false;
		}

		@Override
		public void replaceBySemicolon()
		{
			throw new UnsupportedOperationException();
		}

		@RequiredReadAction
		@Nullable
		@Override
		public DotNetElement getElement()
		{
			return null;
		}

		@Override
		public void replace(@Nullable PsiElement newElement)
		{
			throw new UnsupportedOperationException();
		}
	};

	@RequiredReadAction
	boolean isSemicolonOrEmpty();

	@RequiredReadAction
	default boolean isNotSemicolonAndNotEmpty()
	{
		return !isSemicolonOrEmpty();
	}

	void replaceBySemicolon();
}
