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

package consulo.csharp.lang.psi.impl.resolve;

import org.jetbrains.annotations.NotNull;
import consulo.annotations.RequiredReadAction;
import consulo.dotnet.psi.DotNetElement;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.psi.PsiElement;
import com.intellij.util.Consumer;

/**
 * @author VISTALL
 * @since 26.10.14
 */
public interface CSharpAdditionalMemberProvider
{
	ExtensionPointName<CSharpAdditionalMemberProvider> EP_NAME = ExtensionPointName.create("consulo.csharp.additionalMemberProvider");

	enum Target
	{
		CONSTRUCTOR,
		DE_CONSTRUCTOR,
		INDEX_METHOD,
		CONVERSION_METHOD,
		OPERATOR_METHOD,
		OTHER
	}

	@RequiredReadAction
	void processAdditionalMembers(@NotNull DotNetElement element, @NotNull DotNetGenericExtractor extractor, @NotNull Consumer<PsiElement> consumer);

	@NotNull
	Target getTarget();
}