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

package consulo.csharp.lang.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.psi.PsiElement;
import consulo.annotations.RequiredReadAction;
import consulo.extensions.CompositeExtensionPointName;

/**
 * @author VISTALL
 * @since 18.12.2015
 */
public interface ToNativeElementTransformer
{
	CompositeExtensionPointName<ToNativeElementTransformer> EP_NAME = CompositeExtensionPointName.applicationPoint("consulo.csharp.toNativeElementTransformer",
			ToNativeElementTransformer.class);

	@Nullable
	@RequiredReadAction
	PsiElement transform(@NotNull PsiElement element);
}
