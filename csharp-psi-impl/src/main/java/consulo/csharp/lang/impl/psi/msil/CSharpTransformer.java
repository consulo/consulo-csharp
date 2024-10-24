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

package consulo.csharp.lang.impl.psi.msil;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.ToNativeElementTransformers;
import consulo.language.psi.PsiElement;

import jakarta.annotation.Nonnull;
import java.util.function.Function;

/**
 * @author VISTALL
 * @since 23.10.14
 */
public class CSharpTransformer implements Function<PsiElement, PsiElement>
{
	public static final CSharpTransformer INSTANCE = new CSharpTransformer();

	@Nonnull
	@Override
	@RequiredReadAction
	public PsiElement apply(PsiElement element)
	{
		return ToNativeElementTransformers.transform(element);
	}
}
