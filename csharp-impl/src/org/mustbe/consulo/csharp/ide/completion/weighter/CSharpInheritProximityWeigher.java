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

package org.mustbe.consulo.csharp.ide.completion.weighter;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.ProximityLocation;
import com.intellij.psi.util.proximity.ProximityWeigher;

/**
 * @author VISTALL
 * @since 27.07.2015
 */
public class CSharpInheritProximityWeigher extends ProximityWeigher
{
	@Override
	public Comparable weigh(@NotNull PsiElement element, @NotNull ProximityLocation location)
	{
		PsiElement position = location.getPosition();
		if(position == null || !(position.getContainingFile() instanceof CSharpFile))
		{
			return null;
		}
		return null;
	}
}
