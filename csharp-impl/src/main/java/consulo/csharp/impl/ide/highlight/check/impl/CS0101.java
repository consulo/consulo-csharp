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

package consulo.csharp.impl.ide.highlight.check.impl;

import java.util.Collections;
import java.util.List;

import jakarta.annotation.Nonnull;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.impl.ide.highlight.CSharpHighlightContext;
import consulo.csharp.impl.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpFile;
import consulo.csharp.lang.psi.CSharpNamespaceDeclaration;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetMemberOwner;

/**
 * @author VISTALL
 * @since 10.06.14
 */
public class CS0101 extends CompilerCheck<DotNetMemberOwner>
{
	@RequiredReadAction
	@Nonnull
	@Override
	public List<CompilerCheckBuilder> check(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull DotNetMemberOwner element)
	{
		if(element instanceof CSharpFile || element instanceof CSharpNamespaceDeclaration)
		{
			return CS0102.doCheck(this, element);
		}
		return Collections.emptyList();
	}
}
