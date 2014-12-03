/*
 * Copyright 2013-2014 must-be.org
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

package org.mustbe.consulo.csharp.ide.highlight.check.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraintTypeValue;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.util.Pair;

/**
 * @author VISTALL
 * @since 04.12.14
 */
public class CS0702 extends CompilerCheck<CSharpGenericConstraintTypeValue>
{
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpGenericConstraintTypeValue element)
	{
		DotNetTypeRef typeRef = element.toTypeRef();
		Pair<String,DotNetTypeDeclaration> pair = CSharpTypeUtil.resolveTypeElement(typeRef, element);
		if(pair == null)
		{
			return null;
		}
		if(DotNetTypes.System.Object.equals(pair.getFirst()) || DotNetTypes.System.ValueType.equals(pair.getFirst()))
		{
			DotNetType type = element.getType();
			assert type != null;
			return newBuilder(type, pair.getFirst());
		}
		return null;
	}
}
