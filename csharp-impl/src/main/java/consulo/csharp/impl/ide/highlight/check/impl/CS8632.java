/*
 * Copyright 2013-2020 consulo.io
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

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.impl.ide.highlight.CSharpHighlightContext;
import consulo.csharp.impl.ide.highlight.check.CSharpNullableContext;
import consulo.csharp.impl.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpNullableType;
import consulo.csharp.module.CSharpNullableOption;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.csharp.module.extension.CSharpSimpleModuleExtension;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeResolveResult;
import consulo.language.psi.PsiElement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2020-07-27
 */
public class CS8632 extends CompilerCheck<CSharpNullableType>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull CSharpNullableType element)
	{
		DotNetType innerType = element.getInnerType();
		if(innerType == null)
		{
			return null;
		}
		DotNetTypeRef dotNetTypeRef = innerType.toTypeRef();

		DotNetTypeResolveResult typeResolveResult = dotNetTypeRef.resolve();

		if(!typeResolveResult.isNullable())
		{
			return null;
		}

		CSharpSimpleModuleExtension<?> extension = highlightContext.getCSharpModuleExtension();
		if(extension == null)
		{
			return null;
		}

		CSharpNullableOption nullable = CSharpNullableContext.get(highlightContext.getFile()).getNullable(element, extension.getNullableOption());
		if(nullable == CSharpNullableOption.UNSPECIFIED || nullable == CSharpNullableOption.DISABLE || nullable == CSharpNullableOption.WARNINGS)
		{
			PsiElement questElement = element.getQuestElement();
			return newBuilder(questElement, formatTypeRef(dotNetTypeRef)).withQuickFix(new CS0453.DeleteQuestMarkQuickFix(element));
		}
		return null;
	}
}
