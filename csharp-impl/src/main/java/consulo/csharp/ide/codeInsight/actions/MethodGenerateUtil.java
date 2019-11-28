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

package consulo.csharp.ide.codeInsight.actions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpTypeRefPresentationUtil;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.resolve.DotNetTypeRefUtil;
import consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 01.07.14
 */
public class MethodGenerateUtil
{
	@Nullable
	@RequiredReadAction
	public static String getDefaultValueForType(@Nonnull DotNetTypeRef typeRef, @Nonnull PsiElement scope)
	{
		DotNetTypeResolveResult typeResolveResult = typeRef.resolve();

		if(typeResolveResult.isNullable())
		{
			return "null";
		}
		else
		{
			if(DotNetTypeRefUtil.isVmQNameEqual(typeRef, scope, DotNetTypes.System.Void))
			{
				return null;
			}
			else if(DotNetTypeRefUtil.isVmQNameEqual(typeRef, scope, DotNetTypes.System.Byte))
			{
				return "0";
			}
			else if(DotNetTypeRefUtil.isVmQNameEqual(typeRef, scope, DotNetTypes.System.SByte))
			{
				return "0";
			}
			else if(DotNetTypeRefUtil.isVmQNameEqual(typeRef, scope, DotNetTypes.System.Int16))
			{
				return "0";
			}
			else if(DotNetTypeRefUtil.isVmQNameEqual(typeRef, scope, DotNetTypes.System.UInt16))
			{
				return "0";
			}
			else if(DotNetTypeRefUtil.isVmQNameEqual(typeRef, scope, DotNetTypes.System.Int32))
			{
				return "0";
			}
			else if(DotNetTypeRefUtil.isVmQNameEqual(typeRef, scope, DotNetTypes.System.UInt32))
			{
				return "0";
			}
			else if(DotNetTypeRefUtil.isVmQNameEqual(typeRef, scope, DotNetTypes.System.Int64))
			{
				return "0";
			}
			else if(DotNetTypeRefUtil.isVmQNameEqual(typeRef, scope, DotNetTypes.System.UInt64))
			{
				return "0";
			}
			else if(DotNetTypeRefUtil.isVmQNameEqual(typeRef, scope, DotNetTypes.System.Decimal))
			{
				return "0";
			}
			else if(DotNetTypeRefUtil.isVmQNameEqual(typeRef, scope, DotNetTypes.System.Single))
			{
				return "0";
			}
			else if(DotNetTypeRefUtil.isVmQNameEqual(typeRef, scope, DotNetTypes.System.Double))
			{
				return "0";
			}
			else if(DotNetTypeRefUtil.isVmQNameEqual(typeRef, scope, DotNetTypes.System.Boolean))
			{
				return "false";
			}
			return "default(" + CSharpTypeRefPresentationUtil.buildShortText(typeRef, scope) + ")";
		}
	}
}
