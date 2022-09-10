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

package consulo.csharp.lang.impl.psi.light;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpNamedElement;
import consulo.language.psi.PsiNamedElement;

import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 13.01.14
 */
public abstract class CSharpLightNamedElement<S extends PsiNamedElement> extends CSharpLightElement<S>  implements CSharpNamedElement
{
	protected CSharpLightNamedElement(S original)
	{
		super(original);
	}

	@Override
	public String getName()
	{
		return myOriginal.getName();
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getNameWithAt()
	{
		if(myOriginal instanceof CSharpNamedElement)
		{
			return ((CSharpNamedElement) myOriginal).getNameWithAt();
		}
		return getName();
	}
}
