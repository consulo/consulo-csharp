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

package consulo.csharp.lang.psi;

import consulo.annotation.access.RequiredReadAction;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.navigation.NavigationItem;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 14-Nov-17
 */
public interface CSharpNamedElement extends DotNetNamedElement, NavigationItem
{
	@Contract("null -> null")
	@RequiredReadAction
	static String getEscapedName(@Nullable DotNetNamedElement element)
	{
		if(element == null)
		{
			return null;
		}

		if(element instanceof CSharpNamedElement)
		{
			return ((CSharpNamedElement) element).getNameWithAt();
		}
		return element.getName();
	}

	@Nullable
	@RequiredReadAction
	default String getNameWithAt()
	{
		return getName();
	}
}
