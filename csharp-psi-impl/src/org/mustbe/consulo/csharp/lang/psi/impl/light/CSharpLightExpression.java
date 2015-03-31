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

package org.mustbe.consulo.csharp.lang.psi.impl.light;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.light.LightElement;

/**
 * @author VISTALL
 * @since 31.03.2015
 */
public class CSharpLightExpression extends LightElement implements DotNetExpression
{
	private final DotNetTypeRef myTypeRef;

	public CSharpLightExpression(@NotNull PsiManager manager, @NotNull DotNetTypeRef typeRef)
	{
		super(manager, CSharpLanguage.INSTANCE);
		myTypeRef = typeRef;
	}

	@NotNull
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
