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

package consulo.csharp.lang.psi.impl.msil;

import com.intellij.psi.PsiElement;
import com.intellij.util.NotNullFunction;
import consulo.annotation.DeprecationInfo;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.ToNativeElementTransformers;
import consulo.dotnet.psi.DotNetTypeDeclaration;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 13.07.14
 */
@Deprecated
@DeprecationInfo("Class is deprecated, but it required to use with DotNetPsiSearcher, problem with it - that delegates are ignored. Need rework in " +
		"DotNetPsiSearcher")
public class CSharpTransform implements NotNullFunction<DotNetTypeDeclaration, DotNetTypeDeclaration>
{
	public static final CSharpTransform INSTANCE = new CSharpTransform();

	@Nonnull
	@Override
	@RequiredReadAction
	public DotNetTypeDeclaration fun(DotNetTypeDeclaration typeDeclaration)
	{
		PsiElement wrap = ToNativeElementTransformers.transform(typeDeclaration);
		if(wrap instanceof DotNetTypeDeclaration)
		{
			return (DotNetTypeDeclaration) wrap;
		}
		return typeDeclaration;
	}
}
