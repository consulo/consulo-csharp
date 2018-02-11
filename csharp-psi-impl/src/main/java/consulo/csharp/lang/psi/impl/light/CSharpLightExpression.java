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

package consulo.csharp.lang.psi.impl.light;

import javax.annotation.Nonnull;

import consulo.csharp.lang.CSharpLanguage;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.light.LightElement;

/**
 * @author VISTALL
 * @since 31.03.2015
 */
public class CSharpLightExpression extends LightElement implements DotNetExpression
{
	private final DotNetTypeRef myTypeRef;

	public CSharpLightExpression(@Nonnull PsiManager manager, @Nonnull DotNetTypeRef typeRef)
	{
		super(manager, CSharpLanguage.INSTANCE);
		myTypeRef = typeRef;
	}

	@Nonnull
	@Override
	public DotNetTypeRef toTypeRef(boolean b)
	{
		return myTypeRef;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + ":" + myTypeRef;
	}
}
