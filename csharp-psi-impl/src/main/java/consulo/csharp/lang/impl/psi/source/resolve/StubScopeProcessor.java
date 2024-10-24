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

package consulo.csharp.lang.impl.psi.source.resolve;

import consulo.annotation.access.RequiredReadAction;
import consulo.language.psi.PsiElement;
import consulo.language.psi.ResolveResult;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.resolve.ResolveState;
import consulo.util.dataholder.Key;
import consulo.util.dataholder.UserDataHolderBase;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 27.07.2015
 */
public class StubScopeProcessor extends UserDataHolderBase implements PsiScopeProcessor
{
	public void pushResultExternally(@Nonnull ResolveResult resolveResult)
	{

	}

	@Override
	@RequiredReadAction
	public boolean execute(@Nonnull PsiElement element, ResolveState state)
	{
		return false;
	}

	@Nullable
	@Override
	public <T> T getHint(@Nonnull Key<T> tKey)
	{
		return getUserData(tKey);
	}

	@Override
	public void handleEvent(Event event, @Nullable Object o)
	{
	}
}
