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

package consulo.csharp.ide.highlight.check.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.util.Pair;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.ide.highlight.CSharpHighlightContext;
import consulo.csharp.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import consulo.csharp.lang.psi.impl.resolve.CSharpResolveContextUtil;
import consulo.csharp.lang.psi.impl.source.CSharpTypeDeclarationImplUtil;
import consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.resolve.DotNetGenericExtractor;

/**
 * @author VISTALL
 * @since 01-Nov-17
 */
public class CS1729 extends CompilerCheck<CSharpConstructorDeclaration>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpHighlightContext highlightContext, @NotNull CSharpConstructorDeclaration element)
	{
		if(element.isDeConstructor())
		{
			return null;
		}

		if(element.getConstructorSuperCall() != null)
		{
			return null;
		}

		Pair<DotNetTypeDeclaration, DotNetGenericExtractor> type = CSharpTypeDeclarationImplUtil.resolveBaseType((DotNetTypeDeclaration) element.getParent(), element);
		if(type == null)
		{
			return null;
		}

		CSharpResolveContext context = CSharpResolveContextUtil.createContext(type.getSecond(), element.getResolveScope(), type.getFirst());

		boolean emptyConstructorFind = false;
		CSharpElementGroup<CSharpConstructorDeclaration> group = context.constructorGroup();
		if(group != null)
		{
			for(CSharpConstructorDeclaration declaration : group.getElements())
			{
				if(declaration.getParameters().length == 0)
				{
					emptyConstructorFind = true;
					break;
				}
			}
		}

		if(!emptyConstructorFind)
		{
			return newBuilder(getNameIdentifier(element), formatElement(type.getFirst()), "0");
		}

		return null;
	}
}
