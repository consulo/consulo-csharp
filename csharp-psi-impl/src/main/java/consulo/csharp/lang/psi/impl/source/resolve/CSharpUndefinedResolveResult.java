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

package consulo.csharp.lang.psi.impl.source.resolve;

import javax.annotation.Nullable;

import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;

/**
 * @author VISTALL
 * @since 04-Nov-17
 */
public class CSharpUndefinedResolveResult implements ResolveResult
{
	public static final CSharpUndefinedResolveResult INSTANCE = new CSharpUndefinedResolveResult();

	@Nullable
	@Override
	public PsiElement getElement()
	{
		return null;
	}

	@Override
	public boolean isValidResult()
	{
		return true;
	}
}
