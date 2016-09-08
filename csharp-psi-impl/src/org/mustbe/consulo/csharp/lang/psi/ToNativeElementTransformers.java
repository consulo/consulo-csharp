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

package org.mustbe.consulo.csharp.lang.psi;

import org.jetbrains.annotations.NotNull;
import consulo.annotations.RequiredReadAction;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 18.12.2015
 */
public class ToNativeElementTransformers
{
	@NotNull
	@RequiredReadAction
	public static PsiElement transform(@NotNull PsiElement element)
	{
		PsiElement transform = ToNativeElementTransformer.EP_NAME.composite().transform(element);
		if(transform != null)
		{
			return transform;
		}
		return element;
	}
}
