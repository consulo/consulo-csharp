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

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.lang.psi.CSharpNamedElement;
import consulo.language.psi.ElementDescriptionLocation;
import consulo.language.psi.ElementDescriptionProvider;
import consulo.language.psi.PsiElement;
import consulo.usage.UsageViewShortNameLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 14-Nov-17
 */
@ExtensionImpl
public class CSharpElementDescriptionProvider implements ElementDescriptionProvider
{
	@Nullable
	@Override
	@RequiredReadAction
	public String getElementDescription(@Nonnull PsiElement element, @Nonnull ElementDescriptionLocation location)
	{
		if(location == UsageViewShortNameLocation.INSTANCE && element instanceof CSharpNamedElement)
		{
			return ((CSharpNamedElement) element).getNameWithAt();
		}
		return null;
	}
}
