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

package org.mustbe.consulo.csharp.ide.completion.weigher;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterListOwner;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.ProximityLocation;
import com.intellij.psi.util.proximity.ProximityWeigher;

/**
 * @author VISTALL
 * @since 17.11.2015
 */
public class CSharpByGenericParameterWeigher extends ProximityWeigher
{
	@Override
	@RequiredReadAction
	public Comparable weigh(@NotNull PsiElement element, @NotNull ProximityLocation location)
	{
		if(element instanceof DotNetGenericParameterListOwner && element.getLanguage() == CSharpLanguage.INSTANCE)
		{
			return -((DotNetGenericParameterListOwner) element).getGenericParametersCount();
		}
		return null;
	}
}
