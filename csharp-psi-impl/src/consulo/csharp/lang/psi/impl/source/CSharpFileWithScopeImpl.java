/*
 * Copyright 2013-2016 must-be.org
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

package consulo.csharp.lang.psi.impl.source;

import org.jetbrains.annotations.NotNull;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiCodeFragment;
import com.intellij.psi.search.GlobalSearchScope;

/**
 * @author VISTALL
 * @since 12-May-16
 */
public class CSharpFileWithScopeImpl extends CSharpFileImpl implements PsiCodeFragment
{
	private GlobalSearchScope myScope;

	public CSharpFileWithScopeImpl(@NotNull FileViewProvider viewProvider)
	{
		super(viewProvider);
	}

	@Override
	public void forceResolveScope(GlobalSearchScope scope)
	{
		myScope = scope;
	}

	@Override
	public GlobalSearchScope getForcedResolveScope()
	{
		return myScope;
	}
}
